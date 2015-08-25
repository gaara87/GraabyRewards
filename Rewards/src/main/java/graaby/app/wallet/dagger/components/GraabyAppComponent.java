package graaby.app.wallet.dagger.components;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Component;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.dagger.modules.AndroidModule;
import graaby.app.wallet.database.ORMService;
import graaby.app.wallet.receivers.BootBroadcastReceiver;
import graaby.app.wallet.ui.activities.BaseAppCompatActivity;
import retrofit.ErrorHandler;

/**
 * Created by Akash.
 */
@Singleton
@Component(
        modules = {
                AndroidModule.class
        }
)
public interface GraabyAppComponent {
    void inject(GraabyApplication application);

    void inject(BaseAppCompatActivity appCompatActivity);


    Context context();

    @Nullable
    NfcAdapter nfcAdapter();

    Tracker tracker();

    void inject(ORMService ormService);

    void inject(BootBroadcastReceiver receiver);

    UserAuthenticationHandler userAuthenticationHandler();

    @NonNull
    ORMService oRMService();

    OkHttpClient okHttpClient();

    ErrorHandler errorHandler();

    public static class Initializer {
        public static GraabyAppComponent init(GraabyApplication context) {
            return DaggerGraabyAppComponent.builder().androidModule(new AndroidModule(context)).build();
        }

    }
}
