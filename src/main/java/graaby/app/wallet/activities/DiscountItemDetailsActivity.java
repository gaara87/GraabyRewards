package graaby.app.wallet.activities;

import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.Helper.DiscountItemType;
import graaby.app.wallet.R;

public class DiscountItemDetailsActivity extends ActionBarActivity implements OnClickListener,
        ErrorListener, Listener<JSONObject> {

    private Button graabItButton;
    private Boolean isItemGraabed = Boolean.FALSE;
    private DiscountItemType type = DiscountItemType.Coupons;
    private JSONObject discountItemNode;
    private HashMap<String, Object> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_item_details);

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);
        discountItemNode = new JSONObject();
        try {
            discountItemNode = new JSONObject(info);
        } catch (JSONException e1) {

        }

        isItemGraabed = getIntent().getExtras().getBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG);

        try {
            type = DiscountItemType.getType(discountItemNode.getString(getString(R.string.market_item_type)), this);
        } catch (JSONException e) {

        }
        {
            int logoResId = R.drawable.coupon_nopadding;
            String titleString = getString(R.string.title_activity_discount_item_details), customURL = "";
            switch (type) {
                case Coupons:
                    logoResId = R.drawable.coupon_withpadding;
                    titleString = "Coupon " + titleString;
                    customURL = "c";
                    break;
                case Vouchers:
                    logoResId = R.drawable.voucher_withpadding;
                    titleString = "Voucher " + titleString;
                    customURL = "v";
                    break;
                case Punch:
                    logoResId = R.drawable.punch_withpadding;
                    titleString = "Surprise Gift " + titleString;
                    customURL = "c";
                default:
                    break;
            }
            getSupportActionBar().setTitle(titleString);
            getSupportActionBar().setLogo(logoResId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

            graabItButton = (Button) findViewById(R.id.grab_it_button);

            params = new HashMap<String, Object>();

            try {
                params.put(
                        getResources().getString(R.string.market_id),
                        discountItemNode.getString(getResources().getString(
                                R.string.market_id))
                );
                Helper.getRQ().add(
                        new CustomRequest("market/" + customURL, params, this, this));
            } catch (NotFoundException e1) {
            } catch (JSONException e1) {
            }

        }

        setDetails();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(Helper.createNdefMessage(this), this);
            }
        }
    }

    private void setDetails() {

        String discountValue = null;
        try {
            discountValue = discountItemNode.getString(getString(
                    R.string.market_value));
        } catch (JSONException e) {
        }

        int defaultImageResource = -1, leftDrawable = 0;
        switch (type) {
            case Vouchers:
                defaultImageResource = R.drawable.v_def;
                discountValue = String.format(getString(R.string.discount_item_discount_vale), discountValue);
                leftDrawable = R.drawable.ic_rupee;
                break;
            case Coupons:
                defaultImageResource = R.drawable.c_def;
                leftDrawable = R.drawable.ic_rupee;
                break;
            case Punch:
                defaultImageResource = R.drawable.p_def;
                leftDrawable = R.drawable.ic_surprise;
                break;
            default:
                defaultImageResource = R.drawable.v_def;
        }
        TextView tv = (TextView) findViewById(R.id.item_valueTextView);
        tv.setText(discountValue);
        tv.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);

        tv = (TextView) findViewById(R.id.item_businessNameTextView);
        try {
            tv.setText(discountItemNode.getString(getResources().getString(
                    R.string.field_business_name)));
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }

        tv = (TextView) findViewById(R.id.item_id_textView);
        try {
            tv.setText(discountItemNode.getString(getResources().getString(
                    R.string.market_id)));
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }


        if (!isItemGraabed) {
            graabItButton.setVisibility(View.VISIBLE);
            graabItButton.setOnClickListener(this);
        } else {
            graabItButton.setVisibility(View.GONE);
        }

        changeButtonState();


        ImageView iv = (ImageView) findViewById(R.id.item_businessPicImageView);
        try {
            Helper.getImageLoader().get(discountItemNode.getString(getString(
                    R.string.pic_url)), ImageLoader.getImageListener(iv, defaultImageResource, defaultImageResource));
        } catch (NotFoundException e) {
            iv.setImageResource(defaultImageResource);
        } catch (JSONException e) {
            iv.setImageResource(defaultImageResource);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.item_businessNameTextView:
            case R.id.item_businessPicImageView:
                Intent intent = new Intent(this, BrandDetailsActivity.class);
                intent.putExtra(Helper.INTENT_CONTAINER_INFO, discountItemNode.toString());
                startActivity(intent);
                break;

            case R.id.grab_it_button:
                try {
                    Helper.getRQ().add(
                            new CustomRequest("market/buy", params, this, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(DiscountItemDetailsActivity.this, "Unable to acquire the item", Toast.LENGTH_SHORT).show();
                                    changeButtonState();
                                }
                            })
                    );
                    if (graabItButton != null) {
                        graabItButton.setEnabled(Boolean.FALSE);
                        graabItButton.setText(getString(R.string.button_grab_it_process));
                        graabItButton.setBackgroundColor(getResources().getColor(R.color.orange));
                    }
                } catch (JSONException e) {
                }


                break;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(JSONObject response) {
        if (response.has(getString(R.string.response_success))) {
            try {
                Integer responseSuccess = response.getInt(getString(
                        R.string.response_success));
                isItemGraabed = ((responseSuccess == getResources().getInteger(R.integer.response_success)) ? Boolean.TRUE : Boolean.FALSE);
            } catch (NotFoundException e) {
            } catch (JSONException e) {
            }

            String msg = "";
            if (isItemGraabed) {
                msg = getString(R.string.market_item_successful_purchase);
            } else {
                if (response.has(getString(R.string.response_msg))) {
                    try {
                        msg = response.getString(getString(R.string.response_msg));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            Toast.makeText(this, msg,
                    Toast.LENGTH_LONG).show();
            changeButtonState();
        } else if (response.has(getResources()
                .getString(R.string.market_expiry))) {

            TextView tv = (TextView) findViewById(R.id.item_expiry_textView);
            String text = "";
            try {

                String unformattedString = response.getString(getString(R.string.market_expiry));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = format.parse(unformattedString);
                DateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

                text = String.format(
                        getString(R.string.discount_item_expires_on), targetFormat.format(date));
                tv.setText(text);
            } catch (ParseException e) {
            } catch (JSONException e) {
            }


            text = "";
            tv = (TextView) findViewById(R.id.item_generated_textView);
            try {
                text = String
                        .format(getString(R.string.discount_item_generated_on),
                                response.getString(getString(R.string.market_generated)));
            } catch (JSONException e) {
            }
            tv.setText(text);

        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        changeButtonState();
    }

    private void changeButtonState() {
        if (isItemGraabed == Boolean.TRUE) {
            graabItButton.setText("Congrats, its yours!");
            graabItButton.setBackgroundColor(getResources().getColor(R.color.turquoise));
            graabItButton.setEnabled(Boolean.FALSE);
            graabItButton.setOnClickListener(null);
        } else {
            graabItButton.setBackgroundResource(R.drawable.selector_button_press_orange);
            graabItButton.setEnabled(Boolean.TRUE);
            try {
                String redeemForAmount = String.format(getString(R.string.button_grab_it), discountItemNode.getString(getString(
                        R.string.market_cost)));
                graabItButton.setText(redeemForAmount);
            } catch (NotFoundException e) {
            } catch (JSONException e) {
            }
        }
    }

}
