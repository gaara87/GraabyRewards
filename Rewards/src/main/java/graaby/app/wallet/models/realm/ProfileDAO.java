package graaby.app.wallet.models.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akash on 2/25/15.
 */
public class ProfileDAO extends RealmObject {

    private String fullName;
    private String pictureURL;

    @PrimaryKey
    private String email;


    public ProfileDAO() {
    }

    public ProfileDAO(String email) {
        this.fullName = "";
        this.email = email;
    }

    public ProfileDAO(String name, String email) {
        this.fullName = name;
        this.email = email;
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
}
