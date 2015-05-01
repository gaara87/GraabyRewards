package graaby.app.wallet.modules;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.GraabyBroadcastReceiver;
import graaby.app.wallet.activities.BrandDetailsActivity;
import graaby.app.wallet.activities.BusinessDetailsActivity;
import graaby.app.wallet.activities.DiscountItemDetailsActivity;
import graaby.app.wallet.activities.ExtraInfoActivity;
import graaby.app.wallet.activities.MarketActivity;
import graaby.app.wallet.activities.PointReceivedActivity;
import graaby.app.wallet.activities.SearchResultsActivity;
import graaby.app.wallet.activities.SettingsActivity;
import graaby.app.wallet.fragments.BusinessDetailFragment;
import graaby.app.wallet.fragments.BusinessesFragment;
import graaby.app.wallet.fragments.ContactsFragment;
import graaby.app.wallet.fragments.FeedFragment;
import graaby.app.wallet.fragments.LoginFragment;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.fragments.PointReceivedFromContactFragment;
import graaby.app.wallet.fragments.PointReceivedFromTransactionFragment;
import graaby.app.wallet.fragments.ProfileFragment;
import graaby.app.wallet.fragments.RegistrationFragment;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.network.services.FeedService;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.network.services.SearchService;
import graaby.app.wallet.network.services.SettingsService;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;
import retrofit.RestAdapter;

@Module(
        includes = RetrofitModule.class,
        library = true,
        injects = {
                RegistrationFragment.class,
                LoginFragment.class,
                ProfileFragment.class,
                LoginFragment.class,
                MarketFragment.class,
                FeedFragment.class,
                BusinessesFragment.class,
                BusinessDetailFragment.class,
                ContactsFragment.class,
                DiscountItemDetailsActivity.class,
                SearchResultsActivity.class,
                SettingsActivity.class,
                PointReceivedFromContactFragment.class,
                PointReceivedFromTransactionFragment.class,
                GraabyBroadcastReceiver.class,
                MarketActivity.class,
                BrandDetailsActivity.class,
                BusinessDetailsActivity.class,
                DiscountItemDetailsActivity.class,
                PointReceivedActivity.class,
                SearchResultsActivity.class,
                GraabyOutletDiscoveryService.class,
                ExtraInfoActivity.class
        })
public class ApiServicesModule {
    private static final String TAG = ApiServicesModule.class.getSimpleName();

    @Provides
    @Singleton
    public AuthService provideAuthService(@Named(value = "open") RestAdapter restAdapter) {
        return restAdapter.create(AuthService.class);
    }

    @Provides
    @Singleton
    public ProfileService provideProfileService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(ProfileService.class);
    }

    @Provides
    @Singleton
    public MarketService provideMarketService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(MarketService.class);
    }

    @Provides
    @Singleton
    public FeedService provideFeedService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(FeedService.class);
    }

    @Provides
    @Singleton
    public BusinessService provideBusinessService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(BusinessService.class);
    }

    @Provides
    @Singleton
    public ContactService provideContactService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(ContactService.class);
    }

    @Provides
    @Singleton
    public SearchService provideSearchService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(SearchService.class);
    }

    @Provides
    @Singleton
    public SettingsService provideSettingsService(@Named(value = "authenticated") RestAdapter restAdapter) {
        return restAdapter.create(SettingsService.class);
    }
}
