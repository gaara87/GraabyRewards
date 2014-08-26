package graaby.app.wallet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.activities.SettingsActivity;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.fragments.ContactsFragment;
import graaby.app.wallet.fragments.FeedFragment;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.fragments.ProfileFragment;
import graaby.app.wallet.model.GraabySearchSuggestionsProvider;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GCM_Messages";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    String regid;
    String SENDER_ID = "416705827603";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private AccountManager acm;

    private LocationManager locationManager;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acm = AccountManager.get(this);
        Account[] accounts = acm
                .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);

        if (accounts.length != 0) {
            acm.getAuthToken(accounts[0], UserLoginActivity.AUTHTOKEN_TYPE, null,
                    this, loginCallback, null);
        } else {
            acm.addAccount(UserLoginActivity.ACCOUNT_TYPE, null, null, null, this,
                    loginCallback, null);
        }

        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private AccountManagerCallback<Bundle> loginCallback = new AccountManagerCallback<Bundle>() {
        @Override
        public void run(AccountManagerFuture<Bundle> arg0) {
            String auth;
            try {
                if (arg0.isDone()) {
                    auth = arg0.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    Assert.assertNotNull(auth);
                    Assert.assertNotSame(auth, "");
                    Helper.initializeAppWorkers(auth, getBaseContext());
                    CustomRequest.initialize(auth, arg0.getResult().getString(AccountManager.KEY_ACCOUNT_NAME));
                    context = getBaseContext();
                    initialize();
                } else if (arg0.isCancelled()) {
                    finish();
                    Toast.makeText(MainActivity.this, "Login cancelled", Toast.LENGTH_SHORT).show();
                }
            } catch (AssertionFailedError afe) {
                Account[] accounts = acm
                        .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);
                acm.getAuthToken(accounts[0], UserLoginActivity.AUTHTOKEN_TYPE, null,
                        MainActivity.this, loginCallback, null);
            } catch (OperationCanceledException opc) {
                finish();
            } catch (Exception e) {
                finish();
            }

        }
    };

    private void initialize() {

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        setContentView(R.layout.activity_main_wallet);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(Helper.createNdefMessage(this), this);
            }
        }
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
        placeHolderFragment.setArguments(args);


        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, placeHolderFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_profile);
                break;
            case 1:
                mTitle = getString(R.string.title_marketplace);
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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) {
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                getMenuInflater().inflate(R.menu.menu_global, menu);
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
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
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
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    if (sendRegistrationIdToBackend(regid)) {
                        msg = "Your app has been registered successfully:" + regid;
                        storeRegistrationId(context, regid);
                    } else {
                        msg = "Your app was unable to register:" + regid;
                    }
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("GCM_REG", msg);
            }
        }.execute(null, null, null);
    }

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
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     *
     * @param regid
     */
    private boolean sendRegistrationIdToBackend(String regid) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(getResources().getString(R.string.field_gcm_reg_id), regid);
        try {
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            CustomRequest gcmRequest = new CustomRequest("gcm", params, future, future);
            Helper.getRQ().add(gcmRequest);
            JSONObject response = future.get();
            if (response.getInt(getString(R.string.response_success)) == 1) {
                return true;
            } else if (response.getInt(getString(R.string.response_success)) == 0) {
                return false;
            }
        } catch (JSONException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
