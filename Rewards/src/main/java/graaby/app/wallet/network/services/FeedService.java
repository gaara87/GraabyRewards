package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.FeedsResponse;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Akash on 4/3/15.
 */
public interface FeedService {

    @Headers("Cache-Control: max-age=" + 20)
    @GET("/feeds")
    Observable<FeedsResponse> getUserFeeds(@Query("page") int page, @Query("size") int size);

}
