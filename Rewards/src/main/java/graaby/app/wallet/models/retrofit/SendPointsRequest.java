package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/3/15.
 */
@JsonObject
public class SendPointsRequest {
    @JsonField(name = "to_id")
    public int receiverContactID;
    @JsonField(name = "amt")
    public int amountOfPointsToSend;

    public SendPointsRequest() {

    }

    public SendPointsRequest(int contactIDToSendPointsTo, int pointsToSend) {
        this.receiverContactID = contactIDToSendPointsTo;
        this.amountOfPointsToSend = pointsToSend;
    }
}
