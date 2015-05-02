package graaby.app.wallet.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;
import graaby.app.wallet.util.Helper;

public class BrandDetailsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        DiscountItemDetailsResponse brand = null;
        try {
            brand = LoganSquare.parse(getIntent().getExtras().getString(
                    Helper.INTENT_CONTAINER_INFO), DiscountItemDetailsResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        getSupportActionBar().setTitle(brand.businessName);
        EventBus.getDefault().register(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = new BusinessesFragment();
        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());
        args.putInt(Helper.BRAND_ID_BUNDLE_KEY, brand.businessId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

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
}
