package graaby.app.wallet;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

import dagger.ObjectGraph;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.database.ORMService;
import graaby.app.wallet.modules.AndroidModule;
import graaby.app.wallet.modules.ApiServicesModule;
import graaby.app.wallet.modules.RetrofitModule;
import io.fabric.sdk.android.Fabric;

/**
 * Created by gaara on 1/13/15.
 * Make some impeccable shyte
 */
public class GraabyApplication extends Application {
    private static ObjectGraph graph;
    private static ContainerHolder containerHolder;

    public static ORMService getORMDbService() {
        return graph.get(ORMService.class);
    }

    public static ContainerHolder getContainerHolder() {
        return containerHolder;
    }

    public static ObjectGraph getOG() {
        return graph;
    }

    public static void inject(Object object) {
        graph.inject(object);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PendingResult<ContainerHolder> pendingResult = TagManager.getInstance(this).loadContainerPreferNonDefault(getString(R.string.gtm_tag_id), R.raw.gtm);
        pendingResult.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                GraabyApplication.containerHolder = containerHolder;
                containerHolder.getContainer();
            }
        });
        TagManager.getInstance(this).setVerboseLoggingEnabled(true);

        graph = ObjectGraph.create(new AndroidModule(this));
        graph.injectStatics();

        graph = graph.plus(new ApiServicesModule()).plus(new RetrofitModule());

        UserAuthenticationHandler authHandler = graph.get(UserAuthenticationHandler.class);
        authHandler.login(this);

        if (BuildConfig.DEBUG) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        } else {
            Fabric.with(this, new Crashlytics());
        }
    }
}
