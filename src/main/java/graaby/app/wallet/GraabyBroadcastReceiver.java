package graaby.app.wallet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.gcm.GcmIntentService;

public class GraabyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_THANK = "thank_action";

    public GraabyBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_THANK)) {
            {
                NotificationManager mNotificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(intent.getIntExtra(Helper.NOTIFICATIONID, GcmIntentService.NOTIFICATION_ID_POINTS));
            }
            AccountManager acm = AccountManager.get(context);
            Account[] accounts = acm
                    .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);

            if (accounts.length != 0) {
                AccountManagerFuture<Bundle> future = acm.getAuthToken(accounts[0], UserLoginActivity.AUTHTOKEN_TYPE, null,
                        true, null, null);
                try {
                    Bundle result = future.getResult();
                    String auth = result.getString(AccountManager.KEY_AUTHTOKEN);
                    Helper.initializeAppWorkers(auth, context);
                    CustomRequest.initialize(auth, result.getString(AccountManager.KEY_ACCOUNT_NAME));
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
            String info = intent.getStringExtra(
                    Helper.INTENT_CONTAINER_INFO);
            try {
                JSONObject pointNode = new JSONObject(info);
                int mActivityID = pointNode.getInt(context.getString(R.string.field_activity_id));
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(context.getResources().getString(R.string.field_activity_id), mActivityID);
                Helper.getRQ().add(new CustomRequest("thank", params, null, null));
                Log.d("broadcast received", "ActivityID" + String.valueOf(mActivityID));
            } catch (JSONException e) {
            } catch (NullPointerException npe) {
            }
        }
    }
}
