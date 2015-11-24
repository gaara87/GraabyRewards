package graaby.app.vendor.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.lib.nfc.activities.GraabyBusinessConfirmationActivity;
import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.vendor.R;
import graaby.app.vendor.activities.GBRedemptionActivity;
import graaby.app.vendor.activities.GBTagInitializerActivity;
import graaby.app.vendor.adapter.DiscountInstrumentAdapter;
import graaby.app.vendor.volley.CustomRequest;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;

/**
 * Created by gaara on 8/27/13.
 */
public class DiscountAndFinalizeFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        Response.ErrorListener, Response.Listener<JSONObject> {

    Float billed = 0.0f, graabyDiscount = 0.0f, userDiscount = 0.0f, netTotal = 0.0f;
    @InjectView(R.id.gross_total)
    TextView mGrossTotal;
    @InjectView(R.id.user_discount)
    TextView mUserDiscounts;
    @InjectView(R.id.net_total)
    TextView mNetTotal;
    @InjectView(R.id.confirm)
    Button mConfirm;
    @InjectView(R.id.checkin)
    CheckBox mCheckin;
    @InjectView(R.id.graaby_discount)
    TextView mGraabyDiscount;
    @InjectView(R.id.max_textView)
    TextView mMaxTextView;
    @InjectView(R.id.tv_empty_grid)
    TextView mTvEmptyGrid;
    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    private int mMaximumSelectableDiscounts;
    private Boolean isItAGiftVoucher = Boolean.FALSE;
    private int rewardAmount = 0;
    private DiscountInstrumentAdapter adapter;
    private ArrayList<String> selectedCouponIDs = new ArrayList<String>(), selectedVoucherIDs = new ArrayList<String>();


    private float mDefaultDiscountPercentage;
    private Callbacks callbacks;
    private AlertDialog.Builder builder;
    private Activity mActivity;

    public DiscountAndFinalizeFragment() {
    }

    public static DiscountAndFinalizeFragment newInstance(boolean isItAGiftVoucher, float graabyDiscountPercentage, int maxSelection) {
        DiscountAndFinalizeFragment fragment = new DiscountAndFinalizeFragment();
        Bundle b = new Bundle();
        b.putBoolean("gift_voucher_flag", isItAGiftVoucher);
        b.putFloat("discount", graabyDiscountPercentage);
        b.putInt("max", maxSelection);
        fragment.setArguments(b);
        return fragment;
    }

    public void setDiscountAmount(float graabyDiscountPercentage) {
        try {
            this.mDefaultDiscountPercentage = graabyDiscountPercentage;
            mGraabyDiscount.setText(mActivity.getString(R.string.summary_graaby_default_discount, (int) this.mDefaultDiscountPercentage));
            recalculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        callbacks = (Callbacks) activity;
        builder = new AlertDialog.Builder(activity).setPositiveButton("Ok", null).setNegativeButton(null, null).setTitle("Surprise Secret Reward").setCancelable(Boolean.TRUE);
        isItAGiftVoucher = getArguments().getBoolean("gift_voucher_flag");
        mDefaultDiscountPercentage = getArguments().getFloat("discount");
        mMaximumSelectableDiscounts = getArguments().getInt("max");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            isItAGiftVoucher = getArguments().getBoolean("gift_voucher_flag");
            mDefaultDiscountPercentage = getArguments().getFloat("discount");
        }

        if (savedInstanceState != null) {
            float[] arr = savedInstanceState.getFloatArray("vals");
            mDefaultDiscountPercentage = arr[0];
            billed = arr[0];
        }
        adapter = new DiscountInstrumentAdapter(mActivity);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_discountinstruments, container, Boolean.FALSE);
        ButterKnife.inject(this, rootView);

        mGrid.setAdapter(adapter);
        mGrid.setOnItemClickListener(this);
        mGrid.setOnItemLongClickListener(this);

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.notifyActivityOfAuthorizationCall();
                Intent intent = new Intent(mActivity, GraabyBusinessConfirmationActivity.class);
                intent.putExtra("b", billed);
                intent.putExtra("n", netTotal);
                intent.putExtra("c", selectedCouponIDs);
                intent.putExtra("v", selectedVoucherIDs);
                intent.putExtra("d", graabyDiscount);
                startActivityForResult(intent, 1010);
            }
        });


        mMaxTextView.setText(String.format(mActivity.getString(R.string.max_count_info), String.valueOf(mMaximumSelectableDiscounts)));
        mGraabyDiscount.setText(mActivity.getString(R.string.summary_graaby_default_discount, (int) mDefaultDiscountPercentage));
        if (isItAGiftVoucher) {
            mCheckin.setChecked(Boolean.FALSE);
            mCheckin.setVisibility(View.INVISIBLE);
            mGraabyDiscount.setVisibility(View.GONE);
        }

        recalculate();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isItAGiftVoucher) {
            HashMap<String, Object> postParameters = new HashMap<String, Object>();
            postParameters.put(mActivity.getString(R.string.field_graaby_user_id), GBRedemptionActivity.userTag.getGraabyId());
            if (!GBRedemptionActivity.userTag.isTagGraabified()) {
                postParameters.put(getString(R.string.field_confirm), false);
            }
            CustomRequest getAllDetailsRequest = new CustomRequest(getString(R.string.api_get_it_all), postParameters, this, this);
            getAllDetailsRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingletonRequestQueue.getInstance(mActivity.getApplicationContext()).getRequestQueue().add(getAllDetailsRequest);
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            String field = mActivity.getString(R.string.response_field_reg_data);
            if (response.has(field) && response.getBoolean(field)) {
                Intent data = new Intent(mActivity, GBTagInitializerActivity.class);
                GBRedemptionActivity.userTag.clearCrypto();
                data.putExtra(GraabyTag.GraabyTagParcelKey, GBRedemptionActivity.userTag);
                startActivityForResult(data, getResources().getInteger(R.integer.request_code_initializer));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (response.has(mActivity.getString(R.string.field_discount_percentage))) {
                mDefaultDiscountPercentage = Float.parseFloat(response.getString(mActivity.getString(R.string.field_discount_percentage)));
                callbacks.discountValueSaveToPref(mDefaultDiscountPercentage);
            } else if (response.has(mActivity.getString(R.string.field_corporate_discount_percentage))) {
                mDefaultDiscountPercentage = Float.parseFloat(response.getString(mActivity.getString(R.string.field_corporate_discount_percentage)));
            }
            setDiscountAmount(mDefaultDiscountPercentage);
            JSONObject decryptedJsonObject;
            if (!isItAGiftVoucher) {
                String encryptedDataString = response.getString(mActivity.getString(R.string.field_data));
                String encodedIVString = response.getString((mActivity.getString(R.string.field_iv)));
                decryptedJsonObject = GBRedemptionActivity.userTag.decryptStringToJsonObject(encryptedDataString, encodedIVString);
            } else {
                decryptedJsonObject = response;
            }
            hideLoadingStateForDiscountGrid();
            try {
                adapter.clear();

                JSONArray itemArray = decryptedJsonObject.getJSONArray(mActivity.getString(R.string.field_items));
                Log.d("COUNT", itemArray.length() + " items loaded");
                for (int itemIndex = 0; itemIndex < itemArray.length(); itemIndex++) {
                    JSONObject item = itemArray.getJSONObject(itemIndex);
                    adapter.add(item);
                    recalculate();
                }
                if (adapter.isEmpty()) {
                    mProgressBar.setVisibility(View.GONE);
                    mTvEmptyGrid.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (response.has(mActivity.getString(R.string.field_confirm))) {
                if (response.getInt(mActivity.getString(R.string.field_confirm)) == 1 && GBRedemptionActivity.userTag.isTagGraabified()) {
                    sendTagWritingConfirmRequest();
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void sendTagWritingConfirmRequest() {
        JSONObject postParameters = new JSONObject();
        try {
            postParameters.put(mActivity.getString(R.string.field_id), GBRedemptionActivity.userTag.getGraabyId());
            postParameters.put(mActivity.getString(R.string.field_confirm), getResources().getInteger(R.integer.confirm_tag_written_value));

            JsonObjectRequest confirmationRequest = new JsonObjectRequest(mActivity.getString(R.string.url) + mActivity.getString(R.string.api_confirm_card), postParameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("TAG_WRITE", "Response Received" + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
            );
            VolleySingletonRequestQueue.getInstance(mActivity.getApplicationContext()).getRequestQueue().add(confirmationRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        hideLoadingStateForDiscountGrid();
        if (error != null) {
            String stringResource = "";
            if (error.getClass() == NoConnectionError.class) {
                stringResource = mActivity.getString(R.string.error_no_internet);
            } else if ((error.getClass() == TimeoutError.class)) {
                stringResource = mActivity.getString(R.string.error_request_timedout);
            } else {
                stringResource = mActivity.getString(R.string.error_no_discount_items_available);
            }
            mProgressBar.setVisibility(View.GONE);
            mTvEmptyGrid.setText(stringResource);
            mTvEmptyGrid.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1010) {
            if (resultCode == getResources().getInteger(R.integer.response_code_success)) {
                ArrayList<String> cpns = new ArrayList<String>();
                for (String item : selectedCouponIDs) {
                    cpns.add(item.split("\\^")[0]);
                }

                ArrayList<String> vcr = new ArrayList<String>();
                for (String item : selectedVoucherIDs) {
                    vcr.add(item.split("\\^")[0]);
                }
                String billNo = data.getStringExtra("bill_number");
                callbacks.onSuccessfulAuthorizationByBusiness(graabyDiscount, billed, netTotal, rewardAmount, mCheckin.isChecked(), cpns, vcr, billNo);
            } else if (resultCode == getResources().getInteger(R.integer.response_code_fail)) {
                try {
                    ((GBRedemptionActivity) mActivity).clearBeforeExist();
                } catch (Exception e) {
                    mActivity.finish();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view.isActivated()) {
            view.setActivated(false);
            TransitionDrawable transition = (TransitionDrawable) view.findViewById(R.id.linearLayout).getBackground();
            transition.reverseTransition(500);
        } else {
            if (selectedCouponIDs.size() + selectedVoucherIDs.size() == mMaximumSelectableDiscounts) {
                Toast.makeText(mActivity, "Maximum selectable discounts reached", Toast.LENGTH_SHORT).show();
                return;
            } else {
                view.setActivated(true);
                TransitionDrawable transition = (TransitionDrawable) view.findViewById(R.id.linearLayout).getBackground();
                transition.startTransition(500);
            }
        }


        JSONObject item = adapter.getItem(position);
        try {
            String discountItemStringValue = "";
            Integer selectedDiscountValue = 0;
            try {
                selectedDiscountValue = item.getInt(mActivity.getString(R.string.field_value));
            } catch (JSONException e) {
                discountItemStringValue = item.getString(mActivity.getString(R.string.field_value));
            }
            String discountItemType = item.getString(mActivity.getString(R.string.field_type_discount_item));
            String itemID = item.getString(mActivity.getString(R.string.field_id));
            String customDiscountItem = itemID + "^" + selectedDiscountValue + discountItemStringValue;

            if (view.isActivated()) {
                //selected
                userDiscount += selectedDiscountValue;
                if (discountItemType.equals("p")) {
                    builder.setMessage(discountItemStringValue).create().show();
                }

                if (discountItemType.equals("c") || discountItemType.equals("p")) {
                    selectedCouponIDs.add(customDiscountItem);
                } else if (discountItemType.equals("v")) {
                    selectedVoucherIDs.add(customDiscountItem);
                }
            } else {
                userDiscount -= selectedDiscountValue;
                if (discountItemType.equals("c") || discountItemType.equals("p")) {
                    selectedCouponIDs.remove(customDiscountItem);
                } else if (discountItemType.equals("v")) {
                    selectedVoucherIDs.remove(customDiscountItem);
                }
            }

            recalculate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        view.findViewById(R.id.tv_discount_item_id).setVisibility(View.VISIBLE);
        return false;
    }

    public void hideLoadingStateForDiscountGrid() {
        mGrid.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void showLoadingStateForDiscountGrid() {
        mGrid.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void recalculate() {
        if (selectedCouponIDs.isEmpty() && selectedVoucherIDs.isEmpty()) {
            //use default graaby discount %
            graabyDiscount = billed * mDefaultDiscountPercentage / 100;
            netTotal = billed - graabyDiscount;
        } else {
            netTotal = billed - userDiscount;
            graabyDiscount = userDiscount;
        }
        if (netTotal < 0) netTotal = 0.0f;
        if (mGrossTotal != null) {

            mGrossTotal.setText(String.format("%.2f", billed));
            mUserDiscounts.setText(String.format("%.2f", graabyDiscount));
            mNetTotal.setText(String.format("%.2f", netTotal));
            rewardAmount = 0;

            if (!isItAGiftVoucher) rewardAmount = Math.round(netTotal / 10);
            if (mCheckin.isChecked()) {
                rewardAmount += 1;
            }
        }
    }

    public void setBilledAmountInSummary(Float grossBillAmount) {
        billed = grossBillAmount;
        recalculate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putFloatArray("vals", new float[]{mDefaultDiscountPercentage, billed});
        super.onSaveInstanceState(outState);
    }

    public interface Callbacks {

        void onSuccessfulAuthorizationByBusiness(Float discountValue, Float billAmount, Float netBillAmount, Integer rewardPoints, boolean checkin, ArrayList<String> selectedCouponIDs, ArrayList<String> selectedVoucherIDs, String billNo);

        void notifyActivityOfAuthorizationCall();

        void discountValueSaveToPref(float mDefaultDiscountPercentage);

    }

}

