package graaby.app.wallet.models.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akash on 2/25/15.
 */
public class ProfileDAO extends RealmObject {

    private String fullName;
    private String pictureURL;
    private String currentPoints;

    @PrimaryKey
    private String email;


    public ProfileDAO() {
        this.fullName = "";
        this.email = "";
        currentPoints = "0";
    }

    public ProfileDAO(String email) {
        this.fullName = "";
        this.email = email;
        currentPoints = "0";
    }

    public ProfileDAO(String name, String email) {
        this.fullName = name;
        this.email = email;
        currentPoints = "0";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(String currentPoints) {
        this.currentPoints = currentPoints;
    }

}
