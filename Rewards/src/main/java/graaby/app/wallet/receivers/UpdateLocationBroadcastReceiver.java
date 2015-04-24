package graaby.app.wallet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.events.LocationEvents;

public class UpdateLocationBroadcastReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1011;
    private static final String TAG = UpdateLocationBroadcastReceiver.class.toString();

    public UpdateLocationBroadcastReceiver() {
        Log.d(TAG, "Instantiating");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Broadcast received. Sending out event.");
        EventBus.getDefault().post(new LocationEvents.SendUpdate());

    }


}
