package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.models.retrofit.OutletDetailsRequest;
import graaby.app.wallet.models.retrofit.OutletsRequest;
import graaby.app.wallet.models.retrofit.OutletsResponse;
import graaby.app.wallet.models.retrofit.RatingRequest;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Akash on 4/1/15.
 */
public interface BusinessService {

    @POST("/stores")
    Observable<OutletsResponse> getOutletsAroundLocation(@Body OutletsRequest locationRequest);

    @POST("/store")
    Observable<OutletDetail> getOutletDetails(@Body OutletDetailsRequest request);

    @POST("/rating")
    Observable<BaseResponse> rateUserTransactionForBusiness(@Body RatingRequest ratingRequest);
}
