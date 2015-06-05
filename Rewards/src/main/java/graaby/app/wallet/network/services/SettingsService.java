package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.EmptyJson;
import graaby.app.wallet.models.retrofit.ExtraInfoRequest;
import graaby.app.wallet.models.retrofit.LocationUpdateRequest;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Akash on 4/8/15.
 */
public interface SettingsService {
    @POST("/settings/location")
    void updateUserLocation(@Body LocationUpdateRequest request, Callback<BaseResponse> response);

    @POST("/settings-data")
    Observable<BaseResponse> updateUserInfo(@Body ExtraInfoRequest request);


    @POST("/logout")
    Observable<BaseResponse> logoutUser(@Body EmptyJson request);
}
