package graaby.app.wallet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.activities.BaseAppCompatActivity;
import graaby.app.wallet.adapters.HomePagerAdapter;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.events.LocationEvents;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.fragments.NavigationFragment;
import graaby.app.wallet.fragments.ProfileFragment;
import graaby.app.wallet.gcm.RegistrationIntentService;
import graaby.app.wallet.models.android.GraabySearchSuggestionsProvider;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;

public class MainActivity extends BaseAppCompatActivity
        implements ProfileFragment.ViewBusinessesListener, UserAuthenticationHandler.OnUserAuthentication, TabLayout.OnTabSelectedListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = MainActivity.class.toString();
    @Inject
    Lazy<ProfileService> mProfileService;

    @Bind(R.id.pager)
    ViewPager mPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean authorized = false;
    private boolean initialized = false;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wallet);
        setNavigationView();
        authHandler.loginOrAddAccount(this, this);
    }

    private void setNavigationView() {
        NavigationFragment navFrag = (NavigationFragment) getSupportFragmentManager().findFragmentByTag("nav");
        if (navFrag == null) {
            navFrag = NavigationFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(navFrag, "nav").commit();
        }
        navFrag.attachNavigationView((NavigationView) findViewById(R.id.navigation_view));
    }

    @Override
    public void onSuccessfulAuthentication(boolean shouldStart) {
        authorized = true;
        if (shouldStart) {
            initializeAfterResumeIfAllowed();
        }
    }

    @Override
    public void onFailureAuthentication() {
        Toast.makeText(MainActivity.this, "Login cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initialize() {
        ButterKnife.bind(this);
        GraabyOutletDiscoveryService.setupLocationService(this);
        registerGCM();
        setupToolbar();
        setupTabLayout();
        setupDrawerLayout();

        initialized = true;
    }

    private void registerGCM() {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            this.startService(intent);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    private void setupTabLayout() {
        mPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), this));
        mPager.setOffscreenPageLimit(1);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.setOnTabSelectedListener(this);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            int drawableResourceID = 0;
            switch (i) {
                case 0:
                    drawableResourceID = R.drawable.nav_market;
                    break;
                case 1:
                    drawableResourceID = R.drawable.nav_business;
                    break;
                case 2:
                    drawableResourceID = R.drawable.nav_feeds;
                    break;
                case 3:
                    drawableResourceID = R.drawable.nav_contacts;
            }
            if (mTabLayout != null && drawableResourceID != 0) {
                Drawable drawable = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getResources().getDrawable(drawableResourceID, getTheme());
                } else {
                    drawable = getResources().getDrawable(drawableResourceID);
                }
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable.mutate(), getResources().getColor(android.R.color.white));
                mTabLayout.getTabAt(i).setIcon(drawable);
            }
        }
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
//            use to set custom home indicator
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mTitle = getTitle();

    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    public void restoreActionBar() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BusinessesFragment.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        EventBus.getDefault().post(new LocationEvents.LocationEnabled());
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "Please enable location and refresh", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                mToolbar.inflateMenu(R.menu.menu_global);
                if (mNfcAdapter != null) {
                    if (!mNfcAdapter.isEnabled()) {
                        menu.findItem(R.id.action_menu_item_nfc_toggle).setVisible(Boolean.TRUE);
                    }
                }
                restoreActionBar();
                return true;
            }
        } catch (NullPointerException ignored) {
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_menu_item_nfc_toggle:
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                return true;
            case R.id.action_menu_item_clear_search:
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        GraabySearchSuggestionsProvider.AUTHORITY, GraabySearchSuggestionsProvider.MODE);
                suggestions.clearHistory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeAfterResumeIfAllowed();
    }

    private void initializeAfterResumeIfAllowed() {
        if (authorized && !initialized)
            initialize();
    }

    @Override
    public void onViewBusinessesRequest() {
        //TODO: open businesses fragment
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mPager.setCurrentItem(tab.getPosition());
        getSupportActionBar().setTitle(((HomePagerAdapter) mPager.getAdapter()).getTitle(tab.getPosition()));
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

}
