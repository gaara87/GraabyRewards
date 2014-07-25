package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.wallet.Helper;
import graaby.app.wallet.Helper.DiscountItemType;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.MarketFragment;

public class MarketActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);
        if (!TextUtils.isEmpty(info)) {
            int brandID = -1;
            try {
                JSONObject searchedValue = new JSONObject(info);
                brandID = searchedValue.getInt(getString(R.string.field_business_id));
                String brandName = searchedValue.getString(getString(R.string.field_business_title));
                getSupportActionBar().setTitle(brandName);
            } catch (JSONException e1) {
            }
            args.putInt(Helper.BRAND_ID_BUNDLE_KEY, brandID);
            args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.FALSE);
        } else {

            DiscountItemType type = DiscountItemType.getType(getIntent().getExtras().getInt(Helper.KEY_TYPE));
            int logoResId = R.drawable.coupon_nopadding;
            String titleString = getString(R.string.title_activity_my_cpn_vcr);
            switch (type) {
                case Coupons:
                    logoResId = R.drawable.coupon_nopadding;
                    titleString = titleString + "coupons";
                    break;
                case Vouchers:
                    logoResId = R.drawable.voucher_nopadding;
                    titleString = titleString + " vouchers";
                    break;
                default:
                    break;
            }
            getSupportActionBar().setTitle(titleString);
            getSupportActionBar().setLogo(logoResId);
            args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.TRUE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        Fragment fragment = new MarketFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(Helper.createNdefMessage(this), this);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}