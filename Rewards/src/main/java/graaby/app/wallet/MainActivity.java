package graaby.app.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.activities.BaseAppCompatActivity;
import graaby.app.wallet.activities.SettingsActivity;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.events.LocationEvents;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.fragments.ContactsFragment;
import graaby.app.wallet.fragments.FeedFragment;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.fragments.NavigationDrawerFragment;
import graaby.app.wallet.fragments.ProfileFragment;
import graaby.app.wallet.models.android.GraabySearchSuggestionsProvider;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.GCMInfo;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;
import graaby.app.wallet.util.Helper;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseAppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ProfileFragment.ViewBusinessesListener, UserAuthenticationHandler.OnUserAuthentication {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = MainActivity.class.toString();

    String SENDER_ID = "416705827603";
    @Inject
    UserAuthenticationHandler authHandler;
    @Inject
    Lazy<ProfileService> mProfileService;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean authorized = false;
    private boolean initialized = false;

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

        GraabyOutletDiscoveryService.setupLocationService(this);
        registerGCM();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar, getSupportActionBar());

        initialized = true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment placeHolderFragment = null;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                placeHolderFragment = new ProfileFragment();
                break;
            case 1:
                placeHolderFragment = new MarketFragment();
                args.putBoolean(MarketFragment.SEARCHABLE_PARAMETER, true);
                break;
            case 2:
                placeHolderFragment = new BusinessesFragment();
                break;
            case 3:
                placeHolderFragment = new FeedFragment();
                break;
            case 4:
                placeHolderFragment = new ContactsFragment();
                break;
            case 5:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 10);
                return;
            default:
                break;
        }

        args.putInt(Helper.ARG_SECTION_NUMBER, position);
        assert placeHolderFragment != null;
        placeHolderFragment.setArguments(args);


        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, placeHolderFragment, "main")
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_profile);


//                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.midnightblue)));
                break;
            case 1:
                mTitle = getString(R.string.title_marketplace);

//                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.sunflower)));
                break;
            case 2:
                mTitle = getString(R.string.title_businesses);
                break;
            case 3:
                mTitle = getString(R.string.title_feed);
                break;
            case 4:
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
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_menu_item_nfc_toggle) {
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        } else if (id == R.id.action_menu_item_clear_search) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    GraabySearchSuggestionsProvider.AUTHORITY, GraabySearchSuggestionsProvider.MODE);
            suggestions.clearHistory();
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
     * <p/>
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
     * <p/>
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
        AppObservable.bindActivity(this,
                Observable.just(gcm)
                        .flatMap(new Func1<GoogleCloudMessaging, Observable<BaseResponse>>() {
                            @Override
                            public Observable<BaseResponse> call(GoogleCloudMessaging googleCloudMessaging) {
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
                            }
                        })
                        .observeOn(Schedulers.newThread())
                        .subscribeOn(Schedulers.newThread())
        ).subscribe(new Subscriber<BaseResponse>() {
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

    @Override
    public void onViewBusinessesRequest() {
        mNavigationDrawerFragment.openDrawer();
    }

}
