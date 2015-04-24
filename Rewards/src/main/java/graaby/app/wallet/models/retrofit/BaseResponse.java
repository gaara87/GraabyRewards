package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/23/15.
 */
@JsonObject
public class BaseResponse {
    @JsonField(name = "msg")
    public String message;
    @JsonField(name = "success")
    public int responseSuccessCode;
}
