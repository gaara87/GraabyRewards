package graaby.app.wallet.dagger.components;

import dagger.Component;
import graaby.app.wallet.dagger.modules.ApiServicesModule;
import graaby.app.wallet.dagger.modules.RetrofitModule;
import graaby.app.wallet.dagger.scopes.PerApp;
import graaby.app.wallet.gcm.RegistrationIntentService;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.network.services.FeedService;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.network.services.SearchService;
import graaby.app.wallet.network.services.SettingsService;
import graaby.app.wallet.receivers.GraabyBroadcastReceiver;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;
import graaby.app.wallet.ui.activities.DiscountItemDetailsActivity;
import graaby.app.wallet.ui.activities.ExtraInfoActivity;
import graaby.app.wallet.ui.activities.SearchResultsActivity;
import graaby.app.wallet.ui.activities.SettingsActivity;
import graaby.app.wallet.ui.fragments.BaseFragment;
import graaby.app.wallet.ui.fragments.BusinessDetailFragment;
import graaby.app.wallet.ui.fragments.BusinessesFragment;
import graaby.app.wallet.ui.fragments.ContactsFragment;
import graaby.app.wallet.ui.fragments.FeedFragment;
import graaby.app.wallet.ui.fragments.LoginFragment;
import graaby.app.wallet.ui.fragments.MarketFragment;
import graaby.app.wallet.ui.fragments.NavigationFragment;
import graaby.app.wallet.ui.fragments.PointReceivedFromContactFragment;
import graaby.app.wallet.ui.fragments.PointReceivedFromTransactionFragment;
import graaby.app.wallet.ui.fragments.ProfileFragment;
import graaby.app.wallet.ui.fragments.RegistrationFragment;

/**
 * Created by Akash.
 */
@PerApp
@Component(
        dependencies = GraabyAppComponent.class,
        modules = {
                ApiServicesModule.class,
                RetrofitModule.class
        }
)
public interface ApiServicesComponent {

    void inject(GraabyBroadcastReceiver receiver);

    void inject(ContactsFragment fragment);

    void inject(PointReceivedFromContactFragment fragment);


    ContactService contactService();

    void inject(SettingsActivity activity);

    void inject(ExtraInfoActivity activity);

    void inject(GraabyOutletDiscoveryService service);

    SettingsService settingsService();

    void inject(DiscountItemDetailsActivity activity);

    void inject(MarketFragment fragment);

    MarketService marketService();

    void inject(SearchResultsActivity activity);

    SearchService searchService();

    void inject(BusinessDetailFragment fragment);

    void inject(BusinessesFragment fragment);

    void inject(PointReceivedFromTransactionFragment fragment);

    BusinessService businessService();

    void inject(FeedFragment fragment);

    FeedService feedService();

    void inject(NavigationFragment fragment);

    void inject(ProfileFragment fragment);

    void inject(RegistrationIntentService registrationIntentService);

    ProfileService profileServce();

    void inject(BaseFragment baseFragment);

    void inject(LoginFragment fragment);

    void inject(RegistrationFragment fragment);

    AuthService authService();

}