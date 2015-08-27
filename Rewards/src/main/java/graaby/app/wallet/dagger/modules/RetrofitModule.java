package graaby.app.wallet.dagger.modules;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.auth.UserAuthenticationHandler;
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
@Module
public class RetrofitModule {
    protected static final String NAMED_SCOPE_AUTHENTICATED = "authenticated";
    protected static final String NAMED_SCOPE_UNAUTHENTICATED = "open";
    private static final String TAG = RetrofitModule.class.getSimpleName();

    @Provides
    @Named(value = NAMED_SCOPE_AUTHENTICATED)
    public RequestInterceptor provideRequestInterceptorForAuth(@Singleton final UserAuthenticationHandler authenticationHandler) {
        Log.d(TAG, "Providing requestInterceptor");
        return request -> {
            request.addHeader("User-Agent", "Android Graaby Rewards App " + BuildConfig.VERSION_CODE);
            request.addHeader("oauth", authenticationHandler.oAuth);
            request.addHeader("uid", authenticationHandler.uid);
        };
    }

    @Provides
    @Named(value = NAMED_SCOPE_AUTHENTICATED)
    public RestAdapter provideRestAdapterForAuthRequests(OkHttpClient httpClient,
                                                         @Named(value = NAMED_SCOPE_AUTHENTICATED) RequestInterceptor requestInterceptor,
                                                         ErrorHandler errorHandler) {
        Log.d(TAG, "Providing RestAdapter");
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
    @Named(value = NAMED_SCOPE_UNAUTHENTICATED)
    public RequestInterceptor provideRequestInterceptorForUnauth() {
        Log.d(TAG, "Providing openRequestInterceptor");
        return request -> request.addHeader("User-Agent", "Android Graaby Rewards App " + BuildConfig.VERSION_CODE);
    }

    @Provides
    @Named(value = NAMED_SCOPE_UNAUTHENTICATED)
    public RestAdapter provideRestAdapterForUnauthRequests(OkHttpClient httpClient,
                                                           @Named(value = NAMED_SCOPE_UNAUTHENTICATED) RequestInterceptor requestInterceptor,
                                                           ErrorHandler errorHandler) {
        Log.d(TAG, "Providing openRequestInterceptor");
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

}