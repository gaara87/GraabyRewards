package graaby.app.vendor.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.path.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.vendor.GraabyRedemptionApplication;
import graaby.app.vendor.R;
import graaby.app.vendor.auth.GraabyBusinessUserAuthenticatorActivity;
import graaby.app.vendor.fragments.BillFragment;
import graaby.app.vendor.fragments.DiscountAndFinalizeFragment;
import graaby.app.vendor.helpers.FixedSpeedScroller;
import graaby.app.vendor.helpers.Waiter;
import graaby.app.vendor.helpers.ZoomOutPageTransformer;
import graaby.app.vendor.jobs.SubmitTransactionJob;
import graaby.app.vendor.util.SystemUiHelper;
import graaby.app.vendor.volley.CustomRequest;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;

public class GBRedemptionActivity extends FragmentActivity implements BillFragment.Callbacks, DiscountAndFinalizeFragment.Callbacks, MediaPlayer.OnCompletionListener {

    public static GraabyTag userTag = null;
    protected boolean isItAGiftVoucher;
    JobManager mJobManager;
    @InjectView(R.id.pager)
    ViewPager mPager;
    @InjectView(R.id.message)
    TextView mMessage;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressbar;
    @InjectView(R.id.tx_image)
    ImageView mTransactionImage;
    private RequestQueue mRequestQ;
    private BillFragment billFragment = null;
    private DiscountAndFinalizeFragment discountInstrumentFragment = null;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private String giftVoucherRedeemerPhoneNumber;
    private boolean shouldActivityBeKilled = true;
    private Toast thankYouToast = null;
    private Waiter waiter;
    private SharedPreferences preferences;
    private MediaPlayer mediaPlayer_successTone, mediaPlayer_failTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mRequestQ = VolleySingletonRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
        mJobManager = GraabyRedemptionApplication.getInstance().getJobManager();

        Bundle b = getIntent().getExtras();

        if (b.containsKey("gift")) {
            isItAGiftVoucher = true;
        } else {
            userTag = (GraabyTag) b.getSerializable(GraabyTag.GraabyTagParcelKey);
        }

        preferences = getSharedPreferences(GraabyBusinessUserAuthenticatorActivity.PREFS_BASE_NAME + CustomRequest.getPreferenceName(), MODE_PRIVATE);

        String defaultGraabyPrefPercentage = preferences.getString(SettingsActivity.GRAABY_DISCOUNT_PERCENTAGE, "10.0");
        Float graabyDiscountPercentage = 0f;
        if (!isItAGiftVoucher) {
            if (userTag == null) {
                Toast.makeText(this, "NFC System Error, please restart the device", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            userTag.initliazeCrypto();
            graabyDiscountPercentage = Float.valueOf(defaultGraabyPrefPercentage);

            MediaPlayer mp = MediaPlayer.create(this, R.raw.nfc_tap_success);
            mp.setOnCompletionListener(this);
            mp.start();

            mediaPlayer_successTone = MediaPlayer.create(this, R.raw.tx_success);
            mediaPlayer_successTone.setOnCompletionListener(this);

            mediaPlayer_failTone = MediaPlayer.create(this, R.raw.tx_success);
            mediaPlayer_successTone.setOnCompletionListener(this);
        }


        SharedPreferences localPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int maxCount = localPrefs.getInt(SettingsActivity.GRAABY_MAX_SELECTION, 6);

        billFragment = BillFragment.newInstance(isItAGiftVoucher);
        discountInstrumentFragment = DiscountAndFinalizeFragment.newInstance(isItAGiftVoucher, graabyDiscountPercentage, maxCount);

        setContentView(R.layout.activity_redemption);
        ButterKnife.inject(this);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        mAdapter.setNdefPushMessage(null, this, this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                GBRedemptionActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        thankYouToast = new Toast(getApplicationContext());
        thankYouToast.setGravity(Gravity.CENTER | Gravity.END, 0, 0);
        thankYouToast.setDuration(Toast.LENGTH_LONG);
        View layout = getLayoutInflater().inflate(R.layout.toast_thankyou,
                (ViewGroup) findViewById(R.id.toast_thankyou_root));
        thankYouToast.setView(layout);

        int timeout = localPrefs.getInt(SettingsActivity.GRAABY_TIMEOUT, 15);
        waiter = new Waiter(timeout * 1000, new InactiveHandler(this));
        waiter.start();

        PagerAdapter mPagerAdapter = new GBRedemptionPagerAdapter(getSupportFragmentManager());
        mPager.setOffscreenPageLimit(3);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mPager.getContext());
            mScroller.set(mPager, scroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SystemUiHelper helper = new SystemUiHelper(this, SystemUiHelper.LEVEL_HIDE_STATUS_BAR, 0);
        helper.hide();
    }

    @Override
    protected void onUserLeaveHint() {
        if (shouldActivityBeKilled) {
            clearBeforeExist();
            super.onUserLeaveHint();
        }
    }

    @Override
    public void onBackPressed() {
        int currentItemIndex = mPager.getCurrentItem();
        if (currentItemIndex != 0) mPager.setCurrentItem(currentItemIndex - 1, Boolean.TRUE);
        else {
            clearBeforeExist();
            super.onBackPressed();
        }
    }

    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        waiter.forceInterrupt();
    }

    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        waiter.touch();
    }

    @Override
    public void onRedeemerPhoneNumberFixed(String phoneNumber) {
        giftVoucherRedeemerPhoneNumber = phoneNumber;
        if (isItAGiftVoucher) {
            HashMap<String, Object> postParameters = new HashMap<String, Object>();
            postParameters.put(getString(R.string.field_phone), giftVoucherRedeemerPhoneNumber);
            CustomRequest getGiftDetails = new CustomRequest(getString(R.string.api_get_gift_details), postParameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    discountInstrumentFragment.hideLoadingStateForDiscountGrid();
                    discountInstrumentFragment.onResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    discountInstrumentFragment.hideLoadingStateForDiscountGrid();
                    discountInstrumentFragment.onErrorResponse(error);
                }
            }
            );
            mRequestQ.add(getGiftDetails);
            discountInstrumentFragment.showLoadingStateForDiscountGrid();
        }
    }

    @Override
    public void onBillAmountFixed(Float amount, Boolean changeScreen) {
        discountInstrumentFragment.setBilledAmountInSummary(amount);
        if (changeScreen)
            mPager.setCurrentItem(1, Boolean.TRUE);

    }

    @Override
    public void onCheckInRequest() {
        final HashMap<String, Object> postParameters = new HashMap<String, Object>();
        postParameters.put(getString(R.string.field_graaby_user_id), userTag.getGraabyId());
        CustomRequest checkin = new CustomRequest(getString(R.string.api_checkin), postParameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("CHECKIN", "Checked in");
                showMessage("You have checked in successfully", true);
                finishWithToast(Boolean.FALSE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mJobManager.addJobInBackground(new SubmitTransactionJob(GBRedemptionActivity.this, postParameters));
                showMessage("Check-in requested, \n \n you will be checked in soon", false);
                finishWithToast(Boolean.FALSE);
            }
        }
        );
        mRequestQ.add(checkin);
        showTransactionSubmissionProcess();
    }

    @Override
    public void onSuccessfulAuthorizationByBusiness(Float graabyDiscountPercentage, Float billAmount, Float netBillAmount, final Integer rewardPoints,
                                                    boolean checkin, ArrayList<String> selectedCouponIDs, ArrayList<String> selectedVoucherIDs,
                                                    String billNo) {
        final HashMap<String, Object> postParameters = new HashMap<String, Object>();

        postParameters.put(getString(R.string.field_gross_bill_amount), billAmount);
        postParameters.put(getString(R.string.field_net_bill_amount), netBillAmount);
        postParameters.put(getString(R.string.field_total_discount), graabyDiscountPercentage);
        postParameters.put(getString(R.string.field_reward_amount), rewardPoints);
        postParameters.put(getString(R.string.field_checkin), checkin);
        {
            JSONArray couponArray = new JSONArray();
            for (String couponID : selectedCouponIDs) {
                couponArray.put(couponID);
            }
            postParameters.put(getString(R.string.field_redeemed_coupons), couponArray);
        }
        {
            JSONArray voucherArray = new JSONArray();
            for (String voucherID : selectedVoucherIDs) {
                voucherArray.put(voucherID);
            }
            postParameters.put(getString(R.string.field_redeemed_vouchers), voucherArray);
        }
        postParameters.put(getString(R.string.field_bill_num), billNo);
        postParameters.put(getString(R.string.field_timestamp), System.currentTimeMillis() / 1000);

        if (isItAGiftVoucher) {
            postParameters.put(getString(R.string.field_phone), giftVoucherRedeemerPhoneNumber);
            postParameters.put(getString(R.string.field_gift), Boolean.TRUE);

        } else {
            {
                String encryptedString = userTag.encryptData(new JSONObject(postParameters).toString());
                postParameters.clear();
                postParameters.put(getString(R.string.field_data), encryptedString);
            }
            postParameters.put(getString(R.string.field_graaby_user_id), userTag.getGraabyId());
        }

        try {
            Long millis = Calendar.getInstance().getTimeInMillis();
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] digest = digester.digest(millis.toString().getBytes());
            postParameters.put(getString(R.string.field_id), Base64.encodeToString(digest, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        CustomRequest submitTransaction = new CustomRequest(getString(R.string.api_submit_transaction), postParameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("TX_SUBMIT", "Transaction confirmed by server");
                String points = "";
                try {
                    points = String.valueOf(response.getInt(getString(R.string.field_point)));
                } catch (JSONException e) {


                }
                showMessage("Transaction processed \n\nYou have been rewarded \n" + points + "\nGraaby Points", true);
                finishWithToast(Boolean.TRUE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mJobManager.addJobInBackground(new SubmitTransactionJob(GBRedemptionActivity.this, postParameters));
                Log.d("TX_SUBMIT", "Transaction cached");
                showMessage("Server could not process transaction right now \n \nYour points will be updated within 48 hrs", false);
                finishWithToast(Boolean.FALSE);
            }
        }
        );
        mRequestQ.add(submitTransaction);
        showTransactionSubmissionProcess();

    }

    @Override
    public void notifyActivityOfAuthorizationCall() {
        shouldActivityBeKilled = false;
    }

    @Override
    public void discountValueSaveToPref(float defaultDiscount) {
        new AsyncTask<Float, Void, Void>() {

            @Override
            protected Void doInBackground(Float... floats) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SettingsActivity.GRAABY_DISCOUNT_PERCENTAGE, String.valueOf(floats[0]));
                editor.apply();
                return null;
            }
        }.execute(defaultDiscount);

    }

    private void finishWithToast(final boolean includeResponseCodeFlag) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                thankYouToast.show();
                if (includeResponseCodeFlag)
                    setResult(getResources().getInteger(R.integer.response_code_success));
                GBRedemptionActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 4000);
    }

    public void clearBeforeExist() {
        userTag = null;
        waiter.forceInterrupt();
        this.setResult(getResources().getInteger(R.integer.response_code_fail));
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showTransactionSubmissionProcess() {
        mPager.setVisibility(View.GONE);
        showMessage("Submitting transaction to Graaby servers...");
    }

    private void showMessage(String msg, boolean flag) {
        showMessage(msg);
        if (flag) {
            mTransactionImage.setImageResource(R.drawable.ic_tick);
            if (mediaPlayer_failTone != null)
                mediaPlayer_successTone.start();
        } else {
            mTransactionImage.setImageResource(R.drawable.ic_exc);
            if (mediaPlayer_failTone != null)
                mediaPlayer_failTone.start();
        }
        YoYo.with(Techniques.FadeInDown)
                .duration(200)
                .playOn(mMessage);

        YoYo.with(Techniques.BounceIn)
                .duration(700)
                .playOn(mTransactionImage);
        mProgressbar.setVisibility(View.GONE);
    }

    private void showMessage(String msg) {
        mMessage.setText(msg);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.release();
    }

    private static class InactiveHandler extends Handler {
        private WeakReference<GBRedemptionActivity> mActivity;

        public InactiveHandler(GBRedemptionActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && mActivity.get() != null) {
                Toast.makeText(mActivity.get(), "User inactive for too long", Toast.LENGTH_SHORT).show();
                mActivity.get().finish();
            }
            super.handleMessage(msg);
        }
    }

    class GBRedemptionPagerAdapter extends FragmentPagerAdapter {

        public GBRedemptionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return billFragment;
                case 1:
                    return discountInstrumentFragment;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}

