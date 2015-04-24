package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by Akash on 4/3/15.
 */
@JsonObject
public class ContactsResponse {
    @JsonField(name = "count")
    public int totalContactsCount;

    @JsonField(name = "users")
    public List<ContactDetail> userContacts;

    @JsonObject
    public static class ContactDetail {
        @JsonField(name = "name")
        public String contactName;
        @JsonField(name = "id")
        public int contactID;
        @JsonField(name = "pic")
        public String pictureURL;
        @JsonField(name = "points")
        public int graabyPoints;
    }

}
