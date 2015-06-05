package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ForgotPasswordRequest;
import graaby.app.wallet.models.retrofit.RegistrationRequest;
import graaby.app.wallet.models.retrofit.UserCredentials;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */
public interface AuthService {

    @POST("/login")
    Observable<UserCredentialsResponse> attemptLogin(@Body UserCredentials request);

    @POST("/register")
    Observable<BaseResponse> attemptRegister(@Body RegistrationRequest request);

    @POST("/forgot-password")
    Observable<BaseResponse> passwordReset(@Body ForgotPasswordRequest request);

}
