package graaby.app.wallet.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;

import java.nio.charset.Charset;

import graaby.app.wallet.BuildConfig;


/**
 * Created by Akash.
 */
public class NearbyFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = NearbyFragment.class.toString();
    private static final int REQUEST_RESOLVE_ERROR = 898;
    public static String KEY_ARGUMENT_TYPE_NEARBY_SUB = "key_nearby";
    private GoogleApiClient mGoogleApiClient;
    private Message mMessage;
    private boolean mResolvingError;
    private MessageListener mMessageListener;
    private boolean mIsItSubscribe;

    public static NearbyFragment newInstance(boolean trueToSubscribe) {
        NearbyFragment fragment = new NearbyFragment();
        Bundle b = new Bundle();
        b.putBoolean(KEY_ARGUMENT_TYPE_NEARBY_SUB, trueToSubscribe);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsItSubscribe = getArguments().getBoolean(KEY_ARGUMENT_TYPE_NEARBY_SUB);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Clean up when the user leaves the activity.
            if (mIsItSubscribe)
                Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                        .setResultCallback(new ErrorCheckingCallback("unsubscribe()"));
            else
                Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ErrorCheckingCallback("unpublish()"));
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(
                new ErrorCheckingCallback("getPermissionStatus", this::publishAndSubscribe)
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                // Permission granted or error resolved successfully then we proceed
                // with publish and subscribe..
                publishAndSubscribe();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Toast.makeText(getContext(), "User rejected the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void publishAndSubscribe() {
        if (mIsItSubscribe) {
            mMessageListener = new MessageListener() {
                @Override
                public void onFound(final Message message) {
                    Log.d(TAG, "WOHOOO!! " + new String(message.getContent(), Charset.forName("UTF8")));
                }
            };
            Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_SCAN)
                    .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build())
                    .setResultCallback(new ErrorCheckingCallback("subscribe()"));

        } else {

            mMessage = new Message("I found this message".getBytes(Charset.forName("UTF8")));
            Nearby.Messages.publish(mGoogleApiClient, mMessage, new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST)
                    .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build())
                    .setResultCallback(new ErrorCheckingCallback("publish()"));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (BuildConfig.USE_CRASHLYTICS)
            Crashlytics.log(Log.ERROR, TAG, "Failed to connect to google api client, connection result error code:- " + connectionResult.getErrorCode());
    }


    @Override
    protected void sendRequest() {

    }

    @Override
    void setupInjections() {

    }

    private class ErrorCheckingCallback implements ResultCallback<Status> {
        private final String method;
        private final Runnable runOnSuccess;

        private ErrorCheckingCallback(String method) {
            this(method, null);
        }

        private ErrorCheckingCallback(String method, @Nullable Runnable runOnSuccess) {
            this.method = method;
            this.runOnSuccess = runOnSuccess;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, method + " succeeded.");
                if (runOnSuccess != null) {
                    runOnSuccess.run();
                }
            } else {
                // Currently, the only resolvable error is that the device is not opted
                // in to Nearby. Starting the resolution displays an opt-in dialog.
                if (status.hasResolution()) {
                    if (!mResolvingError) {
                        try {
                            status.startResolutionForResult(getActivity(),
                                    REQUEST_RESOLVE_ERROR);
                            mResolvingError = true;
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(getContext(), method + " failed with exception: " + e, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // This will be encountered on initial startup because we do
                        // both publish and subscribe together.  So having a toast while
                        // resolving dialog is in progress is confusing, so just log it.
                        Log.i(TAG, method + " failed with status: " + status
                                + " while resolving error.");
                    }
                } else {
                    Toast.makeText(getContext(), method + " failed with : " + status
                            + " resolving error: " + mResolvingError, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
