package graaby.app.wallet.nearby;

import android.app.Activity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.Strategy;

import java.nio.charset.Charset;

/**
 * Created by Akash.
 */
public class NearbyPublish extends NearbyBase {

    Message myIdentityMessage;

    public NearbyPublish(Activity context, byte[] data) {
        super.initialize(context);
        myIdentityMessage = new Message(data);
    }

    @Override
    void publishOrSubscribe() {
        myIdentityMessage = new Message("I found this message".getBytes(Charset.forName("UTF8")));
        Nearby.Messages.publish(mGoogleApiClient, myIdentityMessage, new Strategy.Builder().setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST)
                .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT).build())
                .setResultCallback(new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "publish()"));
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && myIdentityMessage != null) {
            // Clean up when the user leaves the activity.
            Nearby.Messages.unpublish(mGoogleApiClient, myIdentityMessage)
                    .setResultCallback(new ErrorCheckingCallback(mActivityContext.get(), mResolvingError, "unpublish()"));
        }
        super.onStop();
    }
}
