package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.EmptyJson;
import graaby.app.wallet.models.retrofit.LocationUpdateRequest;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Akash on 4/8/15.
 */
public interface SettingsService {
    @POST("/settings/location")
    Observable<BaseResponse> updateUserLocation(@Body LocationUpdateRequest request);

    @POST("/logout")
    Observable<BaseResponse> logoutUser(@Body EmptyJson request);
}
