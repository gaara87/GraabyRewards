package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.SearchRequest;
import graaby.app.wallet.models.retrofit.SearchResponse;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Akash on 4/8/15.
 */
public interface SearchService {
    @POST("/search")
    Observable<SearchResponse> searchBusinesses(SearchRequest searchRequest);
}
