package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.GCMInfo;
import graaby.app.wallet.models.retrofit.ProfileResponse;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */
public interface ProfileService {

    @Headers("Cache-Control: max-age=" + 20)
    @GET("/user")
    Observable<ProfileResponse> getProfileInfo();

    @POST("/gcm")
    Observable<BaseResponse> registerGCM(@Body GCMInfo gcmInfo);
}
