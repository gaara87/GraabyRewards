package graaby.app.wallet.dagger.components;

import dagger.Component;
import graaby.app.wallet.dagger.modules.OpenApiServicesModule;
import graaby.app.wallet.dagger.modules.OpenRetrofitModule;
import graaby.app.wallet.dagger.scopes.Unauthenticated;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.ui.fragments.BaseFragment;
import graaby.app.wallet.ui.fragments.LoginFragment;
import graaby.app.wallet.ui.fragments.RegistrationFragment;

/**
 * Created by Akash.
 */
@Unauthenticated
@Component(
        modules = {
                OpenRetrofitModule.class,
                OpenApiServicesModule.class
        },
        dependencies = GraabyAppComponent.class
)
public interface OpenApiServicesComponent {
    void inject(LoginFragment fragment);

    void inject(RegistrationFragment fragment);

    AuthService authService();

    void inject(BaseFragment fragment);
}
