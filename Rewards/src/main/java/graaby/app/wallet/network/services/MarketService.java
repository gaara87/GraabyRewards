package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;
import graaby.app.wallet.models.retrofit.MarketRequest;
import graaby.app.wallet.models.retrofit.MarketResponse;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */
public interface MarketService {

    @Headers("Cache-Control: max-age=" + 100)
    @POST("/market/mine/v")
    Observable<MarketResponse> getUserVouchers(@Body MarketRequest request);
//    Observable<MarketResponse> getUserVouchers(@Body MarketRequest request, @Query("page") int page, @Query("size") int size);

    @Headers("Cache-Control: max-age=" + 100)
    @POST("/market/mine/c")
    Observable<MarketResponse> getUserCoupons(@Body MarketRequest request);
//    Observable<MarketResponse> getUserCoupons(@Body MarketRequest request, @Query("page") int page, @Query("size") int size);

    @POST("/market")
    Observable<MarketResponse> getMarketDiscountItems(@Body MarketRequest request);
//    Observable<MarketResponse> getMarketDiscountItems(@Body MarketRequest request, @Query("page") int page, @Query("size") int size);

    @POST("/market/v")
    Observable<DiscountItemDetailsResponse> getVoucherDetails(@Body DiscountItemDetailsResponse voucherWithID);
//    Observable<DiscountItemDetailsResponse> getVoucherDetails(Query("id") String voucherWithID);

    @POST("/market/c")
    Observable<DiscountItemDetailsResponse> getCouponDetails(@Body DiscountItemDetailsResponse couponWithID);
//    Observable<DiscountItemDetailsResponse> getCouponDetails(Query("id") String couponWithID);

    @POST("/market/buy")
    Observable<BaseResponse> buyDiscountItem(@Body DiscountItemDetailsResponse discountItemWithID);

}
