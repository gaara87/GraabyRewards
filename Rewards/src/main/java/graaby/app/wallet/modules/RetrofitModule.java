package graaby.app.wallet.modules;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.network.RetrofitErrorHandler;
import graaby.app.wallet.util.LoganSquareConverter;
import graaby.app.wallet.util.ServerURL;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by gaara on 1/13/15.
 * Make some impeccable shyte
 */
@Module(
        addsTo = AndroidModule.class,
        library = true,
        complete = false,
        injects = {
                OkHttpClient.class,
                MainActivity.class
        }
)
public class RetrofitModule {
    private static final String TAG = RetrofitModule.class.getSimpleName();

    @Provides
    @Singleton
    public OkHttpClient provideHttpClient(@ForApplication Context application) {
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

        return httpClient;
    }

    @Provides
    @Singleton
    @Named(value = "authenticated")
    public RequestInterceptor provideRequestInterceptorForAuth(final UserAuthenticationHandler authenticationHandler) {
        return request -> {
            request.addHeader("User-Agent", "Android Graaby Rewards App " + BuildConfig.VERSION_CODE);
            request.addHeader("oauth", authenticationHandler.oAuth);
            request.addHeader("uid", authenticationHandler.uid);
        };
    }

    @Provides
    @Singleton
    @Named(value = "open")
    public RequestInterceptor provideRequestInterceptorForUnauth() {
        return request -> request.addHeader("User-Agent", "Android Graaby Rewards App " + BuildConfig.VERSION_CODE);
    }

    @Provides
    @Singleton
    @Named(value = "authenticated")
    public RestAdapter provideRestAdapterForAuthRequests(OkHttpClient httpClient, @Named(value = "authenticated") RequestInterceptor requestInterceptor, ErrorHandler errorHandler) {
        return new RestAdapter.Builder()
                .setEndpoint(ServerURL.url)
                .setClient(new OkClient(httpClient))
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(errorHandler)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(message -> Log.v("RETROFIT", message))
                .setConverter(new LoganSquareConverter())
                .build();
    }

    @Provides
    @Singleton
    @Named(value = "open")
    public RestAdapter provideRestAdapterForUnauthRequests(OkHttpClient httpClient, @Named(value = "open") RequestInterceptor requestInterceptor, ErrorHandler errorHandler) {
        return new RestAdapter.Builder()
                .setEndpoint(ServerURL.url)
                .setClient(new OkClient(httpClient))
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(errorHandler)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(message -> Log.v("RETROFIT", message))
                .setConverter(new LoganSquareConverter())
                .build();
    }

    @Provides
    @Singleton
    public ErrorHandler provideErrorHandler(@ForApplication Context application) {
        return new RetrofitErrorHandler(application);
    }


}
