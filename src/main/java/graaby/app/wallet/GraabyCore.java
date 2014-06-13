
package graaby.app.wallet;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public final class GraabyCore implements Parcelable {

    private static final long serialVersionUID = -4251480829022377156L;

    public static int BASE_YEAR = 2010;

    private Long graabyID;
    private Calendar expiryDate = null;
    private String name = null;
    private boolean male = Boolean.TRUE;
    private byte[] expiry = new byte[]{0, 0, 0};
    private byte[] localAESKey = new byte[32];

    /*
     * Used to construct a core to write it to half graabify a tag
     */
    private GraabyCore() {
        this.expiryDate = Calendar.getInstance();
        this.expiryDate.set(this.expiry[0] + GraabyCore.BASE_YEAR, this.expiry[1], this.expiry[2]);
    }

    public static final Creator<GraabyCore> CREATOR = new Creator<GraabyCore>() {
        public GraabyCore createFromParcel(Parcel source) {
            return new GraabyCore();
        }

        public GraabyCore[] newArray(int size) {
            return new GraabyCore[size];
        }

    };

    public GraabyCore(JSONObject object, Context context) throws JSONException {
        graabyID = object.getLong(context.getString(R.string.core_id));
        String keyString = object.getString(context.getString(R.string.core_key));
        localAESKey = Base64.decode(keyString, Base64.DEFAULT);
        name = object.getString(context.getString(R.string.core_name));
        male = object.getBoolean(context.getString(R.string.core_gender));

        Long expiryLong = object.getLong(context.getString(R.string.core_expiry));

        this.expiryDate = Calendar.getInstance();
        this.expiryDate.setTime(new Date(expiryLong));

        this.expiry[0] = (byte) (this.expiryDate.get(Calendar.YEAR) - GraabyCore.BASE_YEAR);
        this.expiry[1] = (byte) this.expiryDate.get(Calendar.MONTH);
        this.expiry[2] = (byte) this.expiryDate.get(Calendar.DATE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.graabyID);
        dest.writeByteArray(this.expiry);
        dest.writeString(this.name);
        dest.writeBooleanArray(new boolean[]{
                this.male
        });
        dest.writeByteArray(this.localAESKey);
    }
}
