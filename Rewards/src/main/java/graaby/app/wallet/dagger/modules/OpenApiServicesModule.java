package graaby.app.wallet.dagger.modules;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.dagger.scopes.Unauthenticated;
import graaby.app.wallet.network.services.AuthService;
import retrofit.RestAdapter;

/**
 * Created by Akash.
 */
@Module
public class OpenApiServicesModule {

    @Provides
    public AuthService provideAuthService(@Unauthenticated RestAdapter restAdapter) {
        return restAdapter.create(AuthService.class);
    }
}
