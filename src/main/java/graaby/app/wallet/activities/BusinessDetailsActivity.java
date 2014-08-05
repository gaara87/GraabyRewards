package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import graaby.app.wallet.Helper;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.BusinessDetailFragment;
import graaby.app.wallet.fragments.MarketFragment;

public class BusinessDetailsActivity extends ActionBarActivity implements ActionBar.TabListener, BusinessDetailFragment.BusinessDetailFragmentCallback {

    private ViewPager mViewPager;
    private BusinessDetailsPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);


        mPagerAdapter = new BusinessDetailsPagerAdapter(getSupportFragmentManager(), info);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        getSupportActionBar().setTitle(R.string.title_activity_business_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);


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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onPunchesLoaded(JSONArray punches) {
        mPagerAdapter.onPunchesLoaded(punches);
    }


    private class BusinessDetailsPagerAdapter extends FragmentPagerAdapter implements BusinessDetailFragment.BusinessDetailFragmentCallback {

        private int brandID = -1;
        String jsonData;
        private BusinessDetailFragment.PunchCardsListFragment frag;

        public BusinessDetailsPagerAdapter(FragmentManager fm, String jsonStringContainingData) {
            super(fm);
            jsonData = jsonStringContainingData;
            try {
                JSONObject placeNode = new JSONObject(jsonData);
                brandID = placeNode.getInt(getString(R.string.field_business_outlet_id));
            } catch (JSONException e) {
            }
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return BusinessDetailFragment.newInstance(jsonData);
                case 1:
                    frag = BusinessDetailFragment.PunchCardsListFragment.newInstance();
                    return frag;
                case 2:
                    return MarketFragment.newInstance(Boolean.FALSE, brandID, false);
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
                    return getString(R.string.title_activity_business_outlet_detail);
                case 1:
                    return getString(R.string.title_punch_card);
                case 2:
                    return getString(R.string.title_marketplace);
            }
            return super.getPageTitle(position);
        }

        @Override
        public void onPunchesLoaded(JSONArray punches) {
            if (frag != null) {
                ArrayList<JSONObject> arrayList = new ArrayList<JSONObject>();
                for (int i = 0; i < punches.length(); i++) {
                    arrayList.add(punches.optJSONObject(i));
                }
                frag.setPunchCards(BusinessDetailsActivity.this, arrayList);
            }
        }
    }

}
