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
import graaby.app.wallet.dagger.components.GraabyAppComponent;
import graaby.app.wallet.dagger.modules.AndroidModule;
import io.fabric.sdk.android.Fabric;

/**
 * Created by gaara on 1/13/15.
 * Make some impeccable shyte
 */
public class GraabyApplication extends Application {
    private static ContainerHolder mContainerHolder;
    private static GraabyApplication application;
    private GraabyAppComponent mComponent;
    private ApiServicesComponent mApiComp;

    public static ContainerHolder getContainerHolder() {
        return mContainerHolder;
    }

    public static GraabyApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        PendingResult<ContainerHolder> pendingResult = TagManager.getInstance(this).loadContainerPreferNonDefault(getString(R.string.gtm_tag_id), R.raw.gtm);
        pendingResult.setResultCallback(containerHolder -> {
            mContainerHolder = containerHolder;
            containerHolder.getContainer();
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


    private void setupDaggerGraph() {
        mComponent = DaggerGraabyAppComponent.builder().androidModule(new AndroidModule(this)).build();
        mApiComp = DaggerApiServicesComponent.builder().graabyAppComponent(mComponent).build();
    }
}
