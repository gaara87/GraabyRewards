package graaby.app.wallet.nearby;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.Strategy;

import java.nio.charset.Charset;

/**
 * Created by Akash.
 */
public class NearbyPublish extends NearbyBase {

    @Nullable
    private Runnable runnableOnSuccess;

    private Message message;

    private int timeoutStrategy = Strategy.TTL_SECONDS_DEFAULT;

    private NearbyPublish(Activity context) {
        super.initialize(context);
    }

    public NearbyPublish(Activity context, byte[] unparceledData) {
        this(context);
        message = new Message(unparceledData, MESSAGE_FILTER_TYPE_STEP_1_WHO_AM_I);
    }

    public NearbyPublish(Activity context, byte[] unparceledData, @Nullable Runnable onSuccessRunnable) {
        this(context, unparceledData);
        runnableOnSuccess = onSuccessRunnable;
    }

    public NearbyPublish(Activity context, String userID, int step) {
        this(context);
        if (step == 2)
            message = new Message(userID.getBytes(Charset.forName("UTF8")), MESSAGE_FILTER_TYPE_STEP_2_DO_I_HAVE_UNLOCK);
        else
            message = new Message(userID.getBytes(Charset.forName("UTF8")), MESSAGE_FILTER_TYPE_STEP_3_I_AM_ME_DUH);
    }

    public NearbyPublish(Activity context, String userID, int step, int timeoutForPublish) {
        this(context, userID, step);
        timeoutStrategy = timeoutForPublish;
    }

    @Override
    void publishOrSubscribe() {
        Nearby.Messages.publish(mGoogleApiClient, message,
                new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST)
                        .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).setTtlSeconds(timeoutStrategy).build())
                .setResultCallback(
                        (runnableOnSuccess != null) ?
                                new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "publish(Unlocker requested)", runnableOnSuccess) :
                                new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "publish(Unlocker requested)")
                );
    }


    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            // Clean up when the user leaves the activity.
            Nearby.Messages.unpublish(mGoogleApiClient, message)
                    .setResultCallback(new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "unpublish()"));
        }
        super.onStop();
    }
}
