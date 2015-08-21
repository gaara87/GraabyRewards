package graaby.app.wallet.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.gcm.GraabyGCMListenerService;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.util.DiscountItemType;
import graaby.app.wallet.util.Helper;

public class MarketActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);
        if (!TextUtils.isEmpty(info)) {
            try {
                OutletDetail outlet = LoganSquare.parse(info, OutletDetail.class);
                getSupportActionBar().setTitle(outlet.businessName);
                if (getIntent().getAction() != null && getIntent().getAction().equals(GraabyGCMListenerService.NOTIFICATION_ACTION_NEW_DISCOUNT)) {
                    getSupportActionBar().setTitle(outlet.outletName);
                }
                args.putInt(Helper.BRAND_ID_BUNDLE_KEY, outlet.businessID);
                args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.FALSE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            DiscountItemType type = (DiscountItemType) getIntent().getExtras().get(Helper.KEY_TYPE);
            String titleString = getString(R.string.title_activity_my_cpn_vcr);
            switch (type) {
                case COUPONS:
                    titleString = titleString + "coupons";
                    break;
                case VOUCHERS:
                    titleString = titleString + " vouchers";
                    break;
                default:
                    break;
            }
            getSupportActionBar().setTitle(titleString);
            args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.TRUE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        EventBus.getDefault().register(this);

        Fragment fragment = new MarketFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
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