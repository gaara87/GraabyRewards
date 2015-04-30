package graaby.app.wallet.database;

import android.content.Context;

import java.util.List;

import graaby.app.wallet.models.realm.OutletDAO;
import graaby.app.wallet.models.realm.ProfileDAO;
import graaby.app.wallet.models.retrofit.OutletDetail;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by Akash on 3/9/15.
 */
public class ORMService {
    private final static String REALM_FILE = "graaby.realm";
    private Realm realmer;

    public ORMService(Context context) {
        try {
            realmer = Realm.getInstance(context, REALM_FILE);
        } catch (RealmMigrationNeededException rmne) {
            Realm.deleteRealmFile(context, REALM_FILE);
//            Realm.migrateRealmAtPath(REALM_FILE, new RealmMigration() {
//                @Override
//                public long execute(Realm realm, long version) {
//                    if (version == 0) {
//                        Table outletDAOtype = realm.getTable(OutletDAO.class);
//                        long bidIndex = outletDAOtype.addColumn(ColumnType.INTEGER, "bID");
//                        for (int i = 0; i < outletDAOtype.size(); i++) {
//                            outletDAOtype.setLong(bidIndex, i, 0);
//                        }
//                        version++;
//                    }
//
//                    if (version == 1) {
//
//                    }
//                    realmer = realm;
//                    return version;
//                }
//            });
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

    public void updateProfileInfo(String name, String profilePicURL) {
        realmer.beginTransaction();
        ProfileDAO profile = realmer.where(ProfileDAO.class).findFirst();
        if (profile != null) {
            profile.setFullName(name);
            profile.setPictureURL(profilePicURL);
        }
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
