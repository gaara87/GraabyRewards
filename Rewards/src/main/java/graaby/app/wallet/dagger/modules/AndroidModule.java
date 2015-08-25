package graaby.app.wallet.dagger.modules;


import android.content.Context;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.database.ORMService;
import graaby.app.wallet.network.RetrofitErrorHandler;
import retrofit.ErrorHandler;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module
public class AndroidModule {

    private static final String TAG = AndroidModule.class.toString();
    private final GraabyApplication application;

    public AndroidModule(GraabyApplication application) {
        this.application = application;
    }


    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager() {
        Log.d(TAG, "Providing locationManager");
        return (LocationManager) application.getSystemService(LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    @Nullable
    NfcAdapter provideNfcAdapter() {
        Log.d(TAG, "Providing nfcAdapter");
        NfcManager manager = (NfcManager) application.getSystemService(Context.NFC_SERVICE);
        if (manager != null)
            return manager.getDefaultAdapter();
        return null;
    }

    @Provides
    @Singleton
    public ORMService provideORMService() {
        Log.d(TAG, "Providing ORM");
        return new ORMService(application);
    }

    @Provides
    @Singleton
    public UserAuthenticationHandler provideUserAuthenticationHandler() {
        Log.d(TAG, "Providing authHandler");
        return new UserAuthenticationHandler();
    }

    @Provides
    @Singleton
    public synchronized Tracker provideTracker() {
        Log.d(TAG, "Providing gaTracker");
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        analytics.setDryRun(BuildConfig.DEBUG);
        Tracker t = analytics.newTracker(R.xml.analytics);
        t.enableAdvertisingIdCollection(true);
        return t;
    }

    @Provides
    @Singleton
    public OkHttpClient provideHttpClient() {
        long connectiontimeout = 10, readtimeout = 10;
        try {
            connectiontimeout = GraabyApplication.getContainerHolder().getContainer().getLong(application.getString(R.string.gtm_connection_timeout));
            readtimeout = GraabyApplication.getContainerHolder().getContainer().getLong(application.getString(R.string.gtm_read_timeout));
        } catch (NullPointerException ignored) {
        }
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(connectiontimeout, TimeUnit.SECONDS);
        httpClient.setReadTimeout(readtimeout, TimeUnit.SECONDS);

        long maxSize = 250 * 1024 * 1024;

        File cacheDirectory = new File(application.getCacheDir().getAbsolutePath(), "HttpCache");
        Cache cache;
        try {
            cache = new Cache(cacheDirectory, maxSize);
            httpClient.setCache(cache);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create request cache.", e);
        }

//        httpClient.setSslSockertFactory(reallyReallyBadSslSocketFactory());

        Log.d(TAG, "Providing HttpClient");
        return httpClient;
    }

    @Provides
    @Singleton
    public ErrorHandler provideErrorHandler() {
        Log.d(TAG, "Providing errorHandler");
        return new RetrofitErrorHandler(application);
    }
}
