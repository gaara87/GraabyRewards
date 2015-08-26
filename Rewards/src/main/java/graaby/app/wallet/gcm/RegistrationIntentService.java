package graaby.app.wallet.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.GCMInfo;

/**
 * Created by Akash.
 */
public class RegistrationIntentService extends IntentService {

    public static final String SENT_TOKEN_TO_SERVER = "gcm_sent_ack";
    private static final String TAG = RegistrationIntentService.class.toString();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            if (!sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

                if (registerWithGraaby(token)) {
                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            try {
                instanceID.deleteInstanceID();
            } catch (IOException ignored) {
            }
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     *
     * @param instanceID send instanceID to server
     */
    private boolean registerWithGraaby(String instanceID) {
        GraabyApplication.getApplication().getComponent().userAuthenticationHandler().login(this);
        BaseResponse response = GraabyApplication.getApplication().getApiComponent().profileServce()
                .registerGCM(new GCMInfo(instanceID));
        if (response.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
            Log.i("GCM Registration", "Your app has been registered successfully:" + instanceID);
            return true;
        } else if (response.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_failure))) {
            Log.e("GCM Registration", "Your app was unable to register:" + instanceID);
            return false;
        }
        return false;
    }
}
