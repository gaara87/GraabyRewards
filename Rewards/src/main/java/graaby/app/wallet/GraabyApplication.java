package graaby.app.wallet;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

import graaby.app.wallet.dagger.components.ApiServicesComponent;
import graaby.app.wallet.dagger.components.DaggerApiServicesComponent;
import graaby.app.wallet.dagger.components.DaggerGraabyAppComponent;
import graaby.app.wallet.dagger.components.DaggerOpenApiServicesComponent;
import graaby.app.wallet.dagger.components.GraabyAppComponent;
import graaby.app.wallet.dagger.components.OpenApiServicesComponent;
import graaby.app.wallet.dagger.modules.AndroidModule;
import io.fabric.sdk.android.Fabric;

/**
 * Created by gaara on 1/13/15.
 * Make some impeccable shyte
 */
public class GraabyApplication extends Application {
    private static ContainerHolder containerHolder;
    private static GraabyApplication application;
    private GraabyAppComponent mComponent;
    private ApiServicesComponent mApiComp;
    private OpenApiServicesComponent mOpenApiComp;

    public static ContainerHolder getContainerHolder() {
        return containerHolder;
    }

    public static GraabyApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        PendingResult<ContainerHolder> pendingResult = TagManager.getInstance(this).loadContainerPreferNonDefault(getString(R.string.gtm_tag_id), R.raw.gtm);
        pendingResult.setResultCallback(containerHolder1 -> {
            containerHolder = containerHolder1;
            containerHolder1.getContainer();
        });
        TagManager.getInstance(this).setVerboseLoggingEnabled(true);

        setupDaggerGraph();

        mComponent.userAuthenticationHandler().login(this);

        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
        if (!BuildConfig.DEBUG) {
            new ANRWatchDog().start();
        }
    }

    public GraabyAppComponent getComponent() {
        return mComponent;
    }

    public ApiServicesComponent getApiComponent() {
        return mApiComp;
    }

    public OpenApiServicesComponent getOpenApiComponent() {
        return mOpenApiComp;
    }

    private void setupDaggerGraph() {
        mComponent = DaggerGraabyAppComponent.builder().androidModule(new AndroidModule(this)).build();
        mOpenApiComp = DaggerOpenApiServicesComponent.builder().graabyAppComponent(mComponent).build();
        mApiComp = DaggerApiServicesComponent.builder().graabyAppComponent(mComponent).build();
    }
}
