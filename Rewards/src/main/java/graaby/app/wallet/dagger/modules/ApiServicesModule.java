package graaby.app.wallet.dagger.modules;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */

import dagger.Module;
import dagger.Provides;
import graaby.app.wallet.dagger.scopes.Authenticated;
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
    public ProfileService provideProfileService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(ProfileService.class);
    }

    @Provides
    public MarketService provideMarketService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(MarketService.class);
    }

    @Provides
    public FeedService provideFeedService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(FeedService.class);
    }

    @Provides
    public BusinessService provideBusinessService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(BusinessService.class);
    }

    @Provides
    public ContactService provideContactService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(ContactService.class);
    }

    @Provides
    public SearchService provideSearchService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(SearchService.class);
    }

    @Provides
    public SettingsService provideSettingsService(@Authenticated RestAdapter restAdapter) {
        return restAdapter.create(SettingsService.class);
    }
}
