package graaby.app.wallet.dagger.modules;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.dagger.scopes.Unauthenticated;
import graaby.app.wallet.util.LoganSquareConverter;
import graaby.app.wallet.util.ServerURL;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Akash.
 */
@Module
public class OpenRetrofitModule {

    private static final String TAG = OpenRetrofitModule.class.getSimpleName();

    @Provides
    @Unauthenticated
    public RequestInterceptor provideRequestInterceptorForUnauth() {
        Log.d(TAG, "Providing openRequestInterceptor");
        return request -> request.addHeader("User-Agent", "Android Graaby Rewards App " + BuildConfig.VERSION_CODE);
    }

    @Provides
    @Unauthenticated
    public RestAdapter provideRestAdapterForUnauthRequests(OkHttpClient httpClient, @Unauthenticated RequestInterceptor requestInterceptor, ErrorHandler errorHandler) {
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
