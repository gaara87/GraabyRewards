package graaby.app.wallet.network.services;

import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.RegistrationRequest;
import graaby.app.wallet.models.retrofit.UserCredentials;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */
public interface AuthService {

    @POST("/login")
    void attemptLogin(@Body UserCredentials creds, Callback<UserCredentialsResponse> callback);

    @POST("/register")
    void attemptRegister(@Body RegistrationRequest request, Callback<BaseResponse> callback);

}
