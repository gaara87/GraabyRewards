package graaby.app.wallet.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.util.List;

import graaby.app.wallet.R;
import graaby.app.wallet.fragments.BusinessDetailFragment;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.fragments.RewardDetailsFragment;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.util.Helper;
import graaby.app.wallet.widgets.SlidingTabLayout;

public class BusinessDetailsActivity extends BaseAppCompatActivity implements BusinessDetailFragment.BusinessDetailFragmentCallback {

    private BusinessDetailsPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);

        try {
            mPagerAdapter = new BusinessDetailsPagerAdapter(getSupportFragmentManager(), getIntent().getExtras().getString(
                    Helper.INTENT_CONTAINER_INFO));
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(false);

        tabs.setViewPager(mViewPager);
        tabs.setCustomTabColorizer(position -> getResources().getColor(android.R.color.white));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRewardDetailsLoaded(Integer discounts, List<OutletDetail.Reward> punches) {
        mPagerAdapter.onRewardDetailsLoaded(discounts, punches);
    }


    private class BusinessDetailsPagerAdapter extends FragmentPagerAdapter implements BusinessDetailFragment.BusinessDetailFragmentCallback {

        private final OutletDetail outlet;
        private RewardDetailsFragment frag;

        public BusinessDetailsPagerAdapter(FragmentManager fm, String jsonStringContainingData) throws IOException {
            super(fm);
            outlet = LoganSquare.parse(jsonStringContainingData, OutletDetail.class);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    try {
                        return BusinessDetailFragment.newInstance(outlet);
                    } catch (IOException e) {
                        e.printStackTrace();
                        finish();
                    }
                case 1:
                    frag = RewardDetailsFragment.newInstance();
                    return frag;
                case 2:
                    return MarketFragment.newInstance(Boolean.FALSE, outlet.outletID, false);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_title_outlet_detail);
                case 1:
                    return getString(R.string.title_punch_card);
                case 2:
                    return getString(R.string.title_marketplace);
            }
            return super.getPageTitle(position);
        }

        @Override
        public void onRewardDetailsLoaded(Integer discount, List<OutletDetail.Reward> punches) {
            if (frag != null) {
                frag.setPunchCards(BusinessDetailsActivity.this, discount, punches);
            }
        }
    }

}
