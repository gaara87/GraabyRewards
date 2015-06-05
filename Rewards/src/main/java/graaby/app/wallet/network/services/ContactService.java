package graaby.app.wallet.network.services;


import graaby.app.wallet.models.retrofit.AddContactRequest;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ContactsResponse;
import graaby.app.wallet.models.retrofit.SendPointsRequest;
import graaby.app.wallet.models.retrofit.ThankContactRequest;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Akash on 4/3/15.
 */
public interface ContactService {

    @Headers("Cache-Control: max-age=" + 20)
    @GET("/contacts")
    Observable<ContactsResponse> getUserContacts();

    @POST("/contact/send")
    Observable<BaseResponse> sendPointsToUser(@Body SendPointsRequest request);

    @POST("/contact/add")
    Observable<BaseResponse> addContact(@Body AddContactRequest request);

    @POST("/contact/thank")
    void thankContact(@Body ThankContactRequest request, Callback<BaseResponse> callback);

}
