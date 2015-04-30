package graaby.app.wallet;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ThankContactRequest;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.util.Helper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GraabyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_THANK = "thank_action";

    @Inject
    ContactService mContactService;

    public GraabyBroadcastReceiver() {

    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        GraabyApplication.inject(this);
        if (intent.getAction().equals(ACTION_THANK)) {
            {
                NotificationManager mNotificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(intent.getIntExtra(Helper.NOTIFICATIONID, 0));
            }

            final PendingResult async = goAsync();
            Thread thread = new Thread() {
                public void run() {

                    int mActivityID;
                    try {
                        String info = intent.getStringExtra(
                                Helper.INTENT_CONTAINER_INFO);
                        String thankUIDField = context.getString(R.string.field_gcm_thank_id);
                        mActivityID = new JSONObject(info).getInt(thankUIDField);
                    } catch (JSONException e) {
                        Log.e("broadcast received", "Unable to thank");
                        return;
                    }
                    mContactService.thankContact(new ThankContactRequest(mActivityID), new Callback<BaseResponse>() {
                        @Override
                        public void success(BaseResponse baseResponse, Response response) {
                            if (baseResponse.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(context.getString(R.string.gtm_response_success))) {
                                Toast.makeText(context, "Sent", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
                    Log.d("broadcast received", "ActivityID" + String.valueOf(mActivityID));

                    async.finish();
                }
            };
            thread.start();
        }
    }
}
