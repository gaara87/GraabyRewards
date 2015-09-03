package graaby.app.wallet.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bluelinelabs.logansquare.LoganSquare;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;

import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.ui.activities.BusinessDetailsActivity;

public class Helper {

    public static final String NOTIFICATIONID = "noty_id";
    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final int PAGE_SIZE = 20;
    public static final String INTENT_CONTAINER_INFO = "jsonnode_key";
    public static final String KEY_TYPE = "type";
    public static final String MY_DISCOUNT_ITEMS_FLAG = "market";
    public static final String BRAND_ID_BUNDLE_KEY = "bidkey";
    public static final String OUTLET_ID_BUNDLE_KEY = "oidkey";
    public static final String CRASHLYTICS_KEY_NFC = "nfc_phone";
    public static final int DEFAULT_NON_BRAND_RELATED = -1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static void openBusiness(Activity activity, OutletDetail outlet) {
        try {
            Intent intent = new Intent(activity, BusinessDetailsActivity.class);
            intent.putExtra(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(outlet));
            activity.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            activity.finish();
        }
    }

    public static void closeKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean checkPlayServices(Context activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                if (BuildConfig.USE_CRASHLYTICS)
                    Crashlytics.log("Device does not contain play services");
            }
            return false;
        }
        return true;
    }
}