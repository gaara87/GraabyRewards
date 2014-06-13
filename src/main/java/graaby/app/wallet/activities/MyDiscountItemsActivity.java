package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;

import graaby.app.wallet.Helper;
import graaby.app.wallet.Helper.DiscountItemType;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.MarketFragment;

public class MyDiscountItemsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_my_cpn_vcr);

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        Fragment fragment = new MarketFragment();
        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());
        args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.TRUE);
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

    public void showProgress(Boolean show) {
        setProgressBarIndeterminateVisibility(show);
    }
}