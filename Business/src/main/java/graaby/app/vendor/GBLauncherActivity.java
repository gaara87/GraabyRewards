package graaby.app.vendor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.vendor.activities.GBRedemptionActivity;
import graaby.app.vendor.activities.GBTagInitializerActivity;
import graaby.app.vendor.activities.SettingsActivity;
import graaby.app.vendor.auth.GraabyBusinessUserAuthenticatorActivity;
import graaby.app.vendor.auth.GraabyBusinessUserLogin;
import graaby.app.vendor.util.SystemUiHelper;
import graaby.app.vendor.volley.CustomRequest;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;
import graaby.app.wallet.nearby.NearbyPublish;
import graaby.app.wallet.nearby.NearbySubscribe;

/**
 * Created by gaara on 9/6/13.
 */
public class GBLauncherActivity extends Activity implements GraabyBusinessUserLogin.GraabyBusinessUserLoginEvent, View.OnClickListener {


    private static final String TAG = GBLauncherActivity.class.toString();
    private NetworkImageView imageView;
    private CustomRequest getAdvertisementURLs;
    private Toast welcomeToast;

    private ImageLoader imageLoader;

    private NearbySubscribe mNearbySubscriberStep1;
    private NearbySubscribe mNearbySubscriberStep3;

    private HashMap<Long, NearbyTagHolder> mCurrentlyNearbyTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUiHelper helper = new SystemUiHelper(this, SystemUiHelper.LEVEL_HIDE_STATUS_BAR, 0);
        helper.hide();
        GraabyBusinessUserLogin.login(this, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if (NfcAdapter.getDefaultAdapter(this) != null) {
                NdefMessage msg = new NdefMessage(
                        new NdefRecord[]{NdefRecord
                                .createApplicationRecord(getString(R.string.nfc_beam_application_record))});
                NfcAdapter.getDefaultAdapter(this).setNdefPushMessage(msg, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSuccessfulLogin(final String tabletUID, int numberOfAccounts, String preferID) {
        setBusinessValues(tabletUID, preferID);
        setContentView(R.layout.activity_launcher);
        Button giftVoucher = (Button) findViewById(R.id.btn_gift_voucher);
        giftVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launcherIntent = new Intent(GBLauncherActivity.this, GBRedemptionActivity.class);
                launcherIntent.putExtra("gift", Boolean.TRUE);
                startActivity(launcherIntent);
            }
        });

        RadioGroup r = (RadioGroup) findViewById(R.id.radiogroup);

        if (numberOfAccounts > 1 && r.getChildCount() == 0) {
            new SelectableAccountsAsyncTask(this).execute(AccountManager.get(this));
        }

        imageLoader = VolleySingletonRequestQueue.getInstance(getApplicationContext()).getImageLoader();

        imageView = (NetworkImageView) findViewById(R.id.adImage);

        try {
            getAdvertisementURLs = new CustomRequest(getString(R.string.api_ads), new HashMap<String, Object>(0), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    setAdsRotation(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject response = CustomRequest.getCachedResponse(getAdvertisementURLs.getCacheEntry());
                    setAdsRotation(response);
                }
            }, Boolean.TRUE
            );
            VolleySingletonRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(getAdvertisementURLs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initializeToastView();

        initializeSubscribe();
    }

    private void initializeSubscribe() {
        mNearbySubscriberStep1 = new NearbySubscribe(this, new MessageListener() {
            @Override
            public void onFound(Message message) {
                GraabyTag tag = GraabyTag.parseNearbyInfo(message.getContent());
                Log.d(TAG, "Found user" + tag.getGraabyUserName());
                NearbyPublish tagPublisher = new NearbyPublish(GBLauncherActivity.this, tag.getGraabyIdString(), 2);
                mCurrentlyNearbyTags.put(tag.getGraabyId(), new NearbyTagHolder(tag, tagPublisher));
                tagPublisher.onStart();
            }
        }, 1, new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.nearby_identifier).setVisibility(View.VISIBLE);
            }
        });
        mNearbySubscriberStep1.onStart();

        mNearbySubscriberStep3 = new NearbySubscribe(GBLauncherActivity.this, new MessageListener() {
            @Override
            public void onFound(Message message) {
                String graabyUserID = new String(message.getContent(), Charset.forName("UTF8"));
                Long acceptedUser = Long.valueOf(graabyUserID);
                GraabyTag holder = mCurrentlyNearbyTags.get(acceptedUser).tag;
                onSuccessfulTap(holder, null);
                clearNearbyTagsAndPublishers();
            }
        }, 3, null);

        mNearbySubscriberStep3.onStart();

    }

    private void clearNearbyTagsAndPublishers() {
        for (NearbyTagHolder holder : mCurrentlyNearbyTags.values()) {
            holder.publisher.onStop();
            holder.publisher = null;
        }
        mCurrentlyNearbyTags.clear();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mNearbySubscriberStep1 != null)
            mNearbySubscriberStep1.onStart();
        if (mNearbySubscriberStep3 != null)
            mNearbySubscriberStep3.onStart();
    }

    @Override
    protected void onStop() {
        if (mNearbySubscriberStep1 != null)
            mNearbySubscriberStep1.onStop();
        if (mNearbySubscriberStep3 != null)
            mNearbySubscriberStep3.onStop();

        clearNearbyTagsAndPublishers();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mNearbySubscriberStep1 = null;
        mNearbySubscriberStep3 = null;
        if (mCurrentlyNearbyTags != null && mCurrentlyNearbyTags.size() != 0) {
            mCurrentlyNearbyTags.clear();
        }
        super.onDestroy();
    }

    private void initializeToastView() {
        View layout = getLayoutInflater().inflate(R.layout.toast_hello,
                (ViewGroup) findViewById(R.id.toast_hello_root));
        welcomeToast = new Toast(this);
        welcomeToast.setGravity(Gravity.CENTER, 0, 0);
        welcomeToast.setDuration(Toast.LENGTH_LONG);
        welcomeToast.setView(layout);
    }

    private void setAdsRotation(JSONObject responseObject) {
        try {

            final JSONArray urlArray = responseObject.getJSONArray(getString(R.string.field_url));
            final Runnable r = new Runnable() {
                int stepper = 0;

                public void run() {
                    int index = stepper++ % urlArray.length();
                    try {
                        String url = (String) urlArray.get(index);
                        imageView.setImageUrl(url, imageLoader);
                        imageView.postDelayed(this, 5000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            imageView.postDelayed(r, 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            GraabyTag parsedTag = GraabyTag.parseNDEFInfo(this, (Tag) intent
                    .getParcelableExtra(NfcAdapter.EXTRA_TAG), intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
            if (parsedTag != null) {
                onSuccessfulTap(parsedTag, intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
            } else if (GraabyTag.authenticateBusinessTag(this, intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES))) {
                Intent adminIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(adminIntent, 35);
            } else {
                Toast.makeText(this, "Are you sure you are using a Graaby Tag?", Toast.LENGTH_LONG).show();
            }
        } else if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "Unidentified tag, please retry if its a Graaby Tag", Toast.LENGTH_LONG).show();
        }
        super.onNewIntent(intent);
    }

    private void onSuccessfulTap(GraabyTag parsedTag, Parcelable[] parcelableArrayExtra) {
        if (parsedTag.isTagSemiGraabified()) {
            if (parsedTag.isTagStillValid()) {
                if (welcomeToast == null) {
                    initializeToastView();
                }
                TextView tv = (TextView) welcomeToast.getView().findViewById(
                        R.id.grbyUserName);
                String welcomeText = String.format(getString(R.string.welcome_text), parsedTag.getGraabyUserName());
                tv.setText(welcomeText);
                int graabyheadResourceId = R.drawable.toast_profile_man;

                if (!parsedTag.isMale()) {
                    graabyheadResourceId = R.drawable.toast_profile_woman;
                }
                tv.setCompoundDrawablesWithIntrinsicBounds(graabyheadResourceId, 0, 0, 0);
                welcomeToast.show();

                Intent launcherIntent = new Intent(this, GBRedemptionActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(GraabyTag.GraabyTagParcelKey, parsedTag);
                b.putParcelableArray("ndef", parcelableArrayExtra);
                launcherIntent.putExtras(b);
                startActivity(launcherIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


                {//Send to analytics
//                    EasyTracker easyTracker = EasyTracker.getInstance(this);

                    // MapBuilder.createEvent().build() returns a Map of event fields and values
                    // that are set and sent with the hit.
//                    if (easyTracker != null) {
//                        easyTracker.send(MapBuilder
//                                        .createEvent("ui_action",     // Event category (required)
//                                                "tag_scan",  // Event action (required)
//                                                "graaby_tag",   // Event label
//                                                parsedTag.getGraabyId())            // Event value
//                                        .build()
//                        );
//                    }
                }

            } else {
                Toast.makeText(this, "This tag has expired, please renew it", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Your tag is not initialized yet", Toast.LENGTH_SHORT).show();
            Intent launcherIntent = new Intent(this, GBTagInitializerActivity.class);
            launcherIntent.putExtra(GraabyTag.GraabyTagParcelKey, parsedTag);
            startActivityForResult(launcherIntent, getResources().getInteger(R.integer.request_code_initializer));
        }
    }

    @Override
    public void onFailureLogin() {
        Toast.makeText(this, "There is some problem logging you in", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getResources().getInteger(R.integer.request_code_initializer)) {
            if (resultCode == getResources().getInteger(R.integer.response_code_success)) {
                Toast.makeText(this, "Tag initialized successfully,Tap again to proceed", Toast.LENGTH_LONG).show();
            } else if (resultCode == getResources().getInteger(R.integer.response_code_fail)) {
                Toast.makeText(this, "Error fetching your full details from the server", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 35) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        setBusinessValues((String) view.getTag(), String.valueOf(((TextView) view).getText().toString().hashCode()));
    }

    private void setBusinessValues(String tabletID, String preferenceID) {
        CustomRequest.setTabletUID(tabletID);
        CustomRequest.setPreferenceID(preferenceID);
    }

    class SelectableAccountsAsyncTask extends AsyncTask<AccountManager, Void, HashMap<String, String>> {

        private final View.OnClickListener mClickListener;

        public SelectableAccountsAsyncTask(View.OnClickListener listener) {
            mClickListener = listener;
        }

        @Override
        protected HashMap<String, String> doInBackground(AccountManager... accountManagers) {
            AccountManager acm = accountManagers[0];
            Account[] accounts = GraabyBusinessUserLogin.getGraabyAccounts(acm);
            if (accounts != null) {
                HashMap<String, String> list = new HashMap<>(accounts.length);
                AccountManagerFuture<Bundle> future;
                for (Account account : accounts) {
                    future = acm.getAuthToken(account, GraabyBusinessUserAuthenticatorActivity.AUTHTOKEN_TYPE, null, false, null, null);
                    try {
                        Bundle b = future.getResult();
                        list.put(b.getString(AccountManager.KEY_ACCOUNT_NAME), b.getString(AccountManager.KEY_AUTHTOKEN));
                    } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                        e.printStackTrace();
                    }
                }
                return list;
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> list) {
            if (list != null) {
                RadioGroup group = (RadioGroup) findViewById(R.id.radiogroup);
                if (group != null) {
                    for (Map.Entry<String, String> item : list.entrySet()) {
                        RadioButton rb = new RadioButton(GBLauncherActivity.this);
                        rb.setText(item.getKey());
                        rb.setOnClickListener(mClickListener);
                        rb.setTag(item.getValue());
                        group.addView(rb);
                    }
                    ((RadioButton) group.getChildAt(0)).setChecked(true);
                }
            }
        }
    }

    class NearbyTagHolder {
        GraabyTag tag;
        NearbyPublish publisher;

        public NearbyTagHolder(GraabyTag tag, NearbyPublish publisher) {
            this.tag = tag;
            this.publisher = publisher;
        }
    }
}
