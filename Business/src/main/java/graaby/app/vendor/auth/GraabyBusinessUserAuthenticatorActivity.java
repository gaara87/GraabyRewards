package graaby.app.vendor.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import graaby.app.vendor.R;
import graaby.app.vendor.activities.SettingsActivity;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;

final public class GraabyBusinessUserAuthenticatorActivity extends
        AccountAuthenticatorActivity implements OnEditorActionListener {
    final public static String ACCOUNT_TYPE = "graaby.app.vendor";
    final public static String AUTHTOKEN_TYPE = "graaby.app.vendor.auth.tokentype";
    final public static String PREFS_BASE_NAME = "graaby.app.vendor.prefs.";
    final public static String PARAM_AUTH_TOKEN = "authTokenParam";
    final public static String PARAM_SUCCESSFUL_ACCOUNT_ADD = "in";
    private AccountManager accManager;
    private EditText passwordEditText;
    private RequestQueue requestQueue;
    private String ANDROID_ID;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        accManager = AccountManager.get(this);
        ANDROID_ID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        setContentView(R.layout.activity_login);
        passwordEditText = (EditText) findViewById(R.id.tv_password);
        passwordEditText.setOnEditorActionListener(this);
        requestQueue = VolleySingletonRequestQueue.getInstance(this.getApplicationContext()).
                getRequestQueue();
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        mAdapter.setNdefPushMessage(null, this, this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                GraabyBusinessUserAuthenticatorActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            passwordEditText.setEnabled(Boolean.FALSE);
            try {
                final String tabID = ANDROID_ID + new BigInteger(10, new SecureRandom()).toString(32);
                JSONObject postParameters = new JSONObject();
                postParameters.put(getString(R.string.field_auth_code), passwordEditText.getText().toString());
                postParameters.put(getString(R.string.field_tab_uid), tabID);
                JsonObjectRequest tablet_auth = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_tablet_auth), postParameters, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            float discountForOutlet = Float.parseFloat(response.getString(getString(R.string.field_discount_percentage)));
                            String name = response.getString(getString(R.string.field_outlet_name));
                            addAccount(discountForOutlet, name, tabID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        passwordEditText.setEnabled(Boolean.TRUE);
                        Toast.makeText(GraabyBusinessUserAuthenticatorActivity.this, "Unsuccessful device registration", Toast.LENGTH_SHORT).show();
                    }
                }
                );
                requestQueue.add(tablet_auth);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    private void addAccount(float discountForOutlet, String name, String tabID)
            throws NoSuchAlgorithmException {
        Account ac = new Account(name, ACCOUNT_TYPE);
        Bundle userDataBundle = new Bundle();
        userDataBundle.putString(GraabyBusinessUserLogin.USER_DATA_SAAVI, tabID);
        accManager.addAccountExplicitly(ac, "", userDataBundle);
        {
            SharedPreferences preference = this.getSharedPreferences(PREFS_BASE_NAME + String.valueOf(name.hashCode()), MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString(SettingsActivity.GRAABY_DISCOUNT_PERCENTAGE, String.valueOf(discountForOutlet));
            editor.putString(SettingsActivity.GRAABY_TAB_ID, tabID);
            editor.putString(SettingsActivity.GRAABY_OUTLET_NAME, name);

            editor.apply();
        }

        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                GraabyBusinessUserAuthenticatorActivity.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_USERDATA,
                PARAM_SUCCESSFUL_ACCOUNT_ADD);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN,
                tabID);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
        Log.i("Graaby Business Account", "Account added successfully,Tablet registered");
    }

    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    public void onNewIntent(Intent intent) {

    }

}
