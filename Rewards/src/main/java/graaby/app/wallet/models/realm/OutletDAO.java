package graaby.app.wallet.models.realm;

import graaby.app.wallet.models.retrofit.OutletDetail;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akash.
 */
public class OutletDAO extends RealmObject {

    @PrimaryKey
    private int oID;
    private double lat;
    private double lon;
    private int bID;
    private String name;

    public OutletDAO(OutletDetail outletInfo) {
        this.lat = outletInfo.latitude;
        this.lon = outletInfo.longitude;
        this.oID = outletInfo.outletID;
        this.bID = outletInfo.businessID;
        this.name = outletInfo.businessName;
    }

    public OutletDAO() {
    }

    public int getoID() {
        return oID;
    }

    public void setoID(int oID) {
        this.oID = oID;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getbID() {
        return bID;
    }

    public void setbID(int bID) {
        this.bID = bID;
    }
}
