package graaby.app.wallet.dagger.modules;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.network.services.FeedService;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.network.services.SearchService;
import graaby.app.wallet.network.services.SettingsService;
import retrofit.RestAdapter;

@Module
public class ApiServicesModule {

    @Provides
    public ProfileService provideProfileService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(ProfileService.class);
    }

    @Provides
    public MarketService provideMarketService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(MarketService.class);
    }

    @Provides
    public FeedService provideFeedService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(FeedService.class);
    }

    @Provides
    public BusinessService provideBusinessService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(BusinessService.class);
    }

    @Provides
    public ContactService provideContactService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(ContactService.class);
    }

    @Provides
    public SearchService provideSearchService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(SearchService.class);
    }

    @Provides
    public SettingsService provideSettingsService(@Named(value = RetrofitModule.NAMED_SCOPE_AUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(SettingsService.class);
    }

    @Provides
    public AuthService provideAuthService(@Named(value = RetrofitModule.NAMED_SCOPE_UNAUTHENTICATED) RestAdapter restAdapter) {
        return restAdapter.create(AuthService.class);
    }
}
