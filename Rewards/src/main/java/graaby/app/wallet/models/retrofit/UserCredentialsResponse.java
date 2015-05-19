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
    public NFCData core;

    @JsonObject
    public static class NFCData {
        @JsonField(name = "m")
        public boolean gender;

        @JsonField(name = "name")
        public String name;

        @JsonField(name = "id")
        public long id;

        @JsonField(name = "key")
        public String key;

        @JsonField(name = "expiry")
        public long expiry;

        @JsonField(name = "iv")
        public String iv;
    }
}
