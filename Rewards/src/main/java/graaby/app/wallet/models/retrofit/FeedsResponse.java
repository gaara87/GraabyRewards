package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

import graaby.app.wallet.util.ActivityType;

/**
 * Created by Akash on 4/3/15.
 */
@JsonObject
public class FeedsResponse {
    @JsonField(name = "news_feed_count")
    public int totalFeedCount;

    @JsonField(name = "feed")
    public List<NewsFeed> feedsList;

    @JsonObject
    public static class NewsFeed {
        @JsonField(name = "said")
        public String newsContent;
        @JsonField(name = "id_name")
        public String newsSource;
        @JsonField(name = "tstamp")
        public long timestamp;
        @JsonField(name = "id_pic")
        public String pictureURL;
        @JsonField(name = "type", typeConverter = ActivityType.ActivityTypeConverter.class)
        public ActivityType type;
        @JsonField(name = "share-point")
        public int sharePointValue;
        @JsonField(name = "customer_id")
        public int customerID;
        @JsonField(name = "bid")
        public int bid;
    }

}
