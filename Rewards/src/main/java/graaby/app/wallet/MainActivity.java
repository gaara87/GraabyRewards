package graaby.app.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.hdodenhof.circleimageview.CircleImageView;
import graaby.app.wallet.activities.BaseAppCompatActivity;
import graaby.app.wallet.activities.SettingsActivity;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.events.LocationEvents;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.fragments.ContactsFragment;
import graaby.app.wallet.fragments.FeedFragment;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.fragments.ProfileFragment;
import graaby.app.wallet.models.android.GraabySearchSuggestionsProvider;
import graaby.app.wallet.models.realm.ProfileDAO;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.GCMInfo;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;
import graaby.app.wallet.util.Helper;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseAppCompatActivity
        implements ProfileFragment.ViewBusinessesListener, UserAuthenticationHandler.OnUserAuthentication {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = MainActivity.class.toString();

    String SENDER_ID = "416705827603";
    @Inject
    UserAuthenticationHandler authHandler;
    @Inject
    Lazy<ProfileService> mProfileService;

    @Bind(R.id.navigation_drawer_profile_photo)
    CircleImageView navigationDrawerProfilePhoto;
    @Bind(R.id.navigation_drawer_name)
    TextView navigationDrawerName;
    @Bind(R.id.navigation_drawer_email)
    TextView navigationDrawerEmail;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean authorized = false;
    private boolean initialized = false;
    private DrawerLayout mDrawerLayout;

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wallet);
        authHandler.loginOrAddAccount(this, this);
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
        setupDrawerLayout();

        initialized = true;
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

        ProfileDAO profile = GraabyApplication.getORMDbService().getProfileInfo();
        if (profile != null) {
            navigationDrawerName.setText(profile.getFullName());
            navigationDrawerEmail.setText(profile.getEmail());
            Glide.with(this)
                    .load(profile.getPictureURL())
                    .crossFade()
                    .into(navigationDrawerProfilePhoto);
        }

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(menuItem -> {
            Fragment placeHolderFragment = null;
            Bundle args = new Bundle();
            switch (menuItem.getItemId()) {
                case R.id.drawer_profile:
                    placeHolderFragment = new ProfileFragment();
                    break;
                case R.id.drawer_market:
                    placeHolderFragment = new MarketFragment();
                    args.putBoolean(MarketFragment.SEARCHABLE_PARAMETER, true);
                    break;
                case R.id.drawer_business:
                    placeHolderFragment = new BusinessesFragment();
                    break;
                case R.id.drawer_feeds:
                    placeHolderFragment = new FeedFragment();
                    break;
                case R.id.drawer_contacts:
                    placeHolderFragment = new ContactsFragment();
                    break;
                case R.id.drawer_settings:
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    MainActivity.this.startActivityForResult(intent, 10);
                    return true;
            }
            args.putInt(Helper.ARG_SECTION_NUMBER, menuItem.getItemId());
            assert placeHolderFragment != null;
            placeHolderFragment.setArguments(args);


            // update the main content by replacing fragments
            FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, placeHolderFragment, "main")
                    .commit();
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case R.id.drawer_profile:
                mTitle = getString(R.string.title_profile);
                break;
            case R.id.drawer_market:
                mTitle = getString(R.string.title_marketplace);
                break;
            case R.id.drawer_business:
                mTitle = getString(R.string.title_businesses);
                break;
            case R.id.drawer_feeds:
                mTitle = getString(R.string.title_feed);
                break;
            case R.id.drawer_contacts:
                mTitle = getString(R.string.title_contacts);
                break;
        }

    }

    public void restoreActionBar() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    authHandler.logout(this);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                break;
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
        } catch (NullPointerException npe) {
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

    private void registerGCM() {
        if (Helper.checkPlayServices(this)) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            if (EventBus.getDefault().getStickyEvent(ProfileEvents.LoginSuccessfulEvent.class) != null) {
                EventBus.getDefault().removeAllStickyEvents();
                registerInBackground(gcm, "");
            } else {
                String regid = getRegistrationId(getApplicationContext());

                if (regid.isEmpty()) {
                    registerInBackground(gcm, regid);
                }
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     *
     * @param gcm
     * @param regid
     */
    private void registerInBackground(GoogleCloudMessaging gcm, String regid) {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        }

        final StringBuilder regIDBuilder = new StringBuilder(regid);
        // You should send the registration ID to your server over HTTP, so it
        // can use GCM/HTTP or CCS to send messages to your app.

        Observable.just(gcm)
                .flatMap(googleCloudMessaging -> {
                    try {
                        String gcmID = googleCloudMessaging.register(SENDER_ID);
                        regIDBuilder.setLength(0);
                        regIDBuilder.trimToSize();
                        regIDBuilder.append(gcmID);
                        return mProfileService.get()
                                .registerGCM(new GCMInfo(gcmID));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<BaseResponse>() {
                    String msg = "";

                    @Override
                    public void onCompleted() {
                        Log.d("GCM_REG", msg);
                    }

                    @Override
                    public void onError(Throwable e) {
                        msg = "Your app was unable to register :" + regIDBuilder.toString() + " \nError:" + e.getMessage();
                        Log.e("GCM_REG", msg);
                    }

                    @Override
                    public void onNext(BaseResponse baseResponse) {
                        if (baseResponse.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                            msg = "Your app has been registered successfully:" + regIDBuilder.toString();
                            storeRegistrationId(getApplicationContext(), regIDBuilder.toString());
                        } else if (baseResponse.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_failure))) {
                            msg = "Your app was unable to register:" + regIDBuilder.toString();
                        }
                    }
                });
    }

    @Subscribe(sticky = true)
    public void handle(ProfileEvents.NameUpdatedEvent event) {
        ProfileDAO profile = GraabyApplication.getORMDbService().getProfileInfo();
        if (profile != null) {
            navigationDrawerName.setText(profile.getFullName());
            navigationDrawerEmail.setText(profile.getEmail());
        }
    }

    @Subscribe(sticky = true)
    public void handle(ProfileEvents.PictureUpdatedEvent event) {
        Glide.with(this)
                .load(event.getImageURL())
                .crossFade()
                .into(navigationDrawerProfilePhoto);
    }

    @Override
    public void onViewBusinessesRequest() {
        //TODO: open businesses fragment
    }

}
