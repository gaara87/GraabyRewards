package graaby.app.wallet.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import java.lang.ref.WeakReference;

/**
 * Created by Akash.
 */
abstract class NearbyBase implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = NearbyBase.class.toString();
    WeakReference<Activity> mActivityContext;
    GoogleApiClient mGoogleApiClient;
    ErrorCheckingCallback.BooleanWrapper mResolvingError;


    public void initialize(Activity context) {
        mActivityContext = new WeakReference<>(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mResolvingError = new ErrorCheckingCallback.BooleanWrapper();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(
                new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "getPermissionStatus", this::publishOrSubscribe)
        );
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to google api client, connection result error code:- " + connectionResult.getErrorCode());
    }

    public void onStart() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Clean up when the user leaves the activity.
            mGoogleApiClient.disconnect();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorCheckingCallback.REQUEST_RESOLVE_ERROR) {
            mResolvingError.setResolvingError(false);
            if (resultCode == Activity.RESULT_OK) {
                // Permission granted or error resolved successfully then we proceed
                // with publish and subscribe..
                publishOrSubscribe();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Toast.makeText(mActivityContext.get(), "User rejected the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    abstract void publishOrSubscribe();
}
