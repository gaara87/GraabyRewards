package graaby.app.wallet.models.retrofit;


import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

import graaby.app.wallet.util.ActivityType;

/**
 * Created by gaara on 1/14/15.
 * Make some impeccable shyte
 */
@JsonObject
public class ProfileResponse extends BaseResponse {
    @JsonField(name = "bio")
    public Biography userBiography;

    @JsonField(name = "points")
    public PointStats pointStatistics;

    @JsonField(name = "recents")
    public List<RecentUserActivityDetails> recentActivities;

    @JsonObject
    public static class Biography {
        @JsonField
        public String name;
        @JsonField(name = "loc")
        public String location;
        @JsonField(name = "pic")
        public String profilePicURL;
        String moto;
        @JsonField(name = "tag")
        private boolean isTagAssociatedWithAccount = false;

        public boolean getIsTagAssociatedWithAccount() {
            return isTagAssociatedWithAccount;
        }

        public void setIsTagAssociatedWithAccount(boolean isTagAssociatedWithAccount) {
            this.isTagAssociatedWithAccount = isTagAssociatedWithAccount;
        }
    }

    @JsonObject
    public static class PointStats {
        @JsonField(name = "balance")
        public String currentPointBalance;

        @JsonField(name = "sum_points")
        public String cumulativePointTotal;

        @JsonField(name = "sum_savings")
        public String cumulativeSavingsTotal;

        @JsonField(name = "conxns")
        public String totalNumberOfContacts;

        @JsonField(name = "checkins")
        public String totalCheckinCount;

        @JsonField(name = "following")
        public String totalBusinessUserIsFollowingCount;

        @JsonField(name = "cpns")
        public String totalValidCouponsCount;

        @JsonField(name = "vcrs")
        public String totalValidVouchersCount;
    }

    @JsonObject
    public static class RecentUserActivityDetails {
        @JsonField(name = "dtl")
        public String detail;

        @JsonField(name = "type", typeConverter = ActivityType.ActivityTypeConverter.class)
        public ActivityType type;
    }
}
