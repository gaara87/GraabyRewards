package graaby.app.wallet.modules;


import android.content.Context;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.database.ORMService;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module(library = true,
        injects = {
                ORMService.class,
                UserAuthenticationHandler.class
        })
public class AndroidModule {
    private final GraabyApplication application;

    public AndroidModule(GraabyApplication application) {
        this.application = application;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager() {
        return (LocationManager) application.getSystemService(LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    NfcAdapter provideNfcAdapter() {
        NfcManager manager = (NfcManager) application.getSystemService(Context.NFC_SERVICE);
        if (manager != null)
            return manager.getDefaultAdapter();
        return null;
    }

    @Provides
    @Singleton
    public ORMService provideORMService(@ForApplication Context application) {
        return new ORMService(application);
    }

    @Provides
    @Singleton
    public UserAuthenticationHandler provideUserAuthenticationHandler() {
        return new UserAuthenticationHandler();
    }
}
