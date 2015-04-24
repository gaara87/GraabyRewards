package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/23/15.
 */
@JsonObject
public class UserCredentialsResponse extends BaseResponse {
    @JsonField
    public String url;

    @JsonField
    public String oauth;

    @JsonField
    public String core;
}
