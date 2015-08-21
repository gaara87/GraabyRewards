package graaby.app.wallet.database;

import android.content.Context;

import java.util.List;

import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.models.realm.OutletDAO;
import graaby.app.wallet.models.realm.ProfileDAO;
import graaby.app.wallet.models.retrofit.OutletDetail;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

/**
 * Created by Akash on 3/9/15.
 */
public class ORMService {
    private final static String REALM_FILE = "graaby.realm";
    private Realm realmer;

    public ORMService(Context context) {
        try {
            Realm.setDefaultConfiguration(new RealmConfiguration.Builder(context)
                    .name(REALM_FILE)
                    .schemaVersion(2)
                    .migration((realm, version) -> {
                        if (version == 1) {
                            Table profileTable = realm.getTable(ProfileDAO.class);
                            long columnIndex = profileTable.addColumn(ColumnType.STRING, "currentPoints");
                            for (int i = 0; i < profileTable.size(); i++) {
                                profileTable.setString(columnIndex, i, "0");
                            }
                            version++;
                        }
                        realmer = realm;
                        return version;
                    }).build());

            realmer = Realm.getInstance(context);
        } catch (RealmMigrationNeededException rmne) {
            Realm.deleteRealm(Realm.getDefaultInstance().getConfiguration());
        }
    }

    public void clearProfileInfo() {
        realmer.beginTransaction();
        realmer.where(ProfileDAO.class).findAll().clear();
        realmer.commitTransaction();
    }

    public void addProfileInfo(String email) {
        ProfileDAO profile = new ProfileDAO(email);
        realmer.beginTransaction();
        realmer.copyToRealm(profile);
        realmer.commitTransaction();
    }

    public ProfileDAO getProfileInfo() {
        return realmer.where(ProfileDAO.class).findFirst();
    }

    public void updateProfileInfo(String name, String profilePicURL, String points) {
        realmer.beginTransaction();
        ProfileDAO profile = realmer.where(ProfileDAO.class).findFirst();
        if (profile == null) {
            profile = new ProfileDAO(GraabyApplication.getOG().get(UserAuthenticationHandler.class).getAccountEmail());
        }
        profile.setFullName(name);
        profile.setPictureURL(profilePicURL);
        profile.setCurrentPoints(points);
        realmer.copyToRealmOrUpdate(profile);
        realmer.commitTransaction();
    }

    public void addOutletsCollection(List<OutletDetail> outlets) {
        realmer.beginTransaction();
        for (OutletDetail detail : outlets) {
            realmer.copyToRealmOrUpdate(new OutletDAO(detail));
        }
        realmer.commitTransaction();
    }

    public RealmResults<OutletDAO> getAllOutlets() {
        return realmer.where(OutletDAO.class).findAll();
    }

    public RealmResults<OutletDAO> getAllOutletsForBrand(int brandID) {
        return realmer.where(OutletDAO.class).equalTo("bID", brandID).findAll();
    }

}
