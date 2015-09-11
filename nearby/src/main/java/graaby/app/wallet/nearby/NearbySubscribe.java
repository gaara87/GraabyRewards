package graaby.app.wallet.nearby;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;

import java.nio.charset.Charset;

/**
 * Created by Akash.
 */
public class NearbySubscribe extends NearbyBase {

    MessageListener mMessageListener;

    public NearbySubscribe(Activity context) {
        super.initialize(context);

    }

    @Override
    void publishOrSubscribe() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                Log.d("NearbySubscribe", "WOHOOO!! " + new String(message.getContent(), Charset.forName("UTF8")));
            }
        };
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_SCAN)
                .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build())
                .setResultCallback(new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "subscribe()"));
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
