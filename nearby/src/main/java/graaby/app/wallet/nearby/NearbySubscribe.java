package graaby.app.wallet.nearby;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;

/**
 * Created by Akash.
 */
public class NearbySubscribe extends NearbyBase {

    MessageListener mMessageListener;
    Runnable onSuccessRunnableCallback;
    MessageFilter mCustomMessageFilter;

    private NearbySubscribe(Activity context, Runnable runnable) {
        super.initialize(context);
        onSuccessRunnableCallback = runnable;
    }

    public NearbySubscribe(Activity context, MessageListener messageListener, int step, @Nullable Runnable runnable) {
        this(context, runnable);
        this.mMessageListener = messageListener;
        if (step == 2)
            mCustomMessageFilter = new MessageFilter.Builder().includeNamespacedType("", MESSAGE_FILTER_TYPE_STEP_2_DO_I_HAVE_UNLOCK).build();
        else if (step == 3)
            mCustomMessageFilter = new MessageFilter.Builder().includeNamespacedType("", MESSAGE_FILTER_TYPE_STEP_3_I_AM_ME_DUH).build();
        else
            mCustomMessageFilter = new MessageFilter.Builder().includeNamespacedType("", MESSAGE_FILTER_TYPE_STEP_1_WHO_AM_I).build();
    }

    @Override
    void publishOrSubscribe() {

        ErrorCheckingCallback errorCheckingCallback = (onSuccessRunnableCallback != null) ?
                new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "subscribe()", onSuccessRunnableCallback) :
                new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "subscribe()");
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_SCAN)
                        .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build(),
                mCustomMessageFilter)
                .setResultCallback(errorCheckingCallback);
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mMessageListener != null) {
            // Clean up when the user leaves the activity.
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "unsubscribe()"));
        }
        super.onStop();
    }
}
