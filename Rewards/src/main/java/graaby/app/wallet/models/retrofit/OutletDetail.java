package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

import graaby.app.wallet.models.realm.OutletDAO;

/**
 * Created by Akash on 4/1/15.
 */
@JsonObject
public class OutletDetail {
    @JsonField(name = "lt")
    public double latitude;
    @JsonField(name = "lg")
    public double longitude;
    @JsonField(name = "area")
    public String areaName;
    @JsonField(name = "title")
    public String businessName;
    @JsonField(name = "bname")
    public String outletName;
    @JsonField(name = "oid")
    public int outletID;
    @JsonField(name = "bid")
    public int businessID;
    @JsonField(name = "phone")
    public String phoneNumber;
    @JsonField(name = "site")
    public String websiteURL;
    @JsonField(name = "pic")
    public String pictureURL;
    @JsonField(name = "baddr")
    public String outletAddress;

    @JsonField(name = "punchcard")
    public Punchcards punchards;

    @JsonField(name = "discount")
    public int flatGraabyDiscountPercentage;

    @JsonField(name = "stats")
    public Stats outletStatistics;

    public OutletDetail() {

    }

    public OutletDetail(OutletDAO outletDAO) {
        this.latitude = outletDAO.getLat();
        this.longitude = outletDAO.getLon();
        this.businessName = outletDAO.getName();
        this.outletID = outletDAO.getoID();
        this.businessID = outletDAO.getbID();
    }

    @JsonObject
    public static class Stats {
        @JsonField(name = "given")
        public String pointsGivenOut;
        @JsonField(name = "following")
        public String numberOfUsersFollowing;
        @JsonField(name = "checkins")
        public String totalCheckins;
        @JsonField(name = "sum_points")
        public String totalPointsEarnedInOutlet;
        @JsonField(name = "my_checkins")
        public String totalCheckinsInOutlet;
        @JsonField(name = "sum_savings")
        public String totalSavingsInOutlet;
    }

    @JsonObject
    public static class Punchcards {
        @JsonField(name = "rewards")
        public List<Reward> punchCardRewards;
    }

    @JsonObject
    public static class Reward {
        @JsonField(name = "reward")
        public String rewardDetail;
        @JsonField(name = "reward_visit")
        public String onVisitCount;
    }
}
