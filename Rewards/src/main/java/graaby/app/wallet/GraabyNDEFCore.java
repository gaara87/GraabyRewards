
package graaby.app.wallet;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

public final class GraabyNDEFCore implements Parcelable {

    public static final Creator<GraabyNDEFCore> CREATOR = new Creator<GraabyNDEFCore>() {
        public GraabyNDEFCore createFromParcel(Parcel source) {
            return new GraabyNDEFCore();
        }

        public GraabyNDEFCore[] newArray(int size) {
            return new GraabyNDEFCore[size];
        }

    };
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
    private GraabyNDEFCore() {
        this.expiryDate = Calendar.getInstance();
        this.expiryDate.set(this.expiry[0] + GraabyNDEFCore.BASE_YEAR, this.expiry[1], this.expiry[2]);
    }

    public GraabyNDEFCore(JSONObject object, Context context) throws JSONException {
        graabyID = object.getLong(context.getString(R.string.core_id));
        String keyString = object.getString(context.getString(R.string.core_key));
        localAESKey = Base64.decode(keyString, Base64.DEFAULT);
        name = object.getString(context.getString(R.string.core_name));
        male = object.getBoolean(context.getString(R.string.core_gender));

        Long expiryLong = object.getLong(context.getString(R.string.core_expiry));

        this.expiryDate = Calendar.getInstance();
        this.expiryDate.setTime(new Date(expiryLong));

        this.expiry[0] = (byte) (this.expiryDate.get(Calendar.YEAR) - GraabyNDEFCore.BASE_YEAR);
        this.expiry[1] = (byte) this.expiryDate.get(Calendar.MONTH);
        this.expiry[2] = (byte) this.expiryDate.get(Calendar.DATE);
    }

    public static NdefMessage createNdefMessage(Context applicationContext) {
        Parcel pc = Parcel.obtain();
        try {
            FileInputStream fis = applicationContext.openFileInput("beamer");
            DataInputStream ois = new DataInputStream(fis);
            String jsonString = ois.readUTF();
            JSONObject jsonCore = new JSONObject(jsonString);
            byte[] iv = Base64.decode(jsonCore.getString(applicationContext.getString(R.string.core_iv)), Base64.DEFAULT);
            GraabyNDEFCore core = new GraabyNDEFCore(jsonCore, applicationContext);
            ois.close();
            fis.close();
            core.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            byte[] data = pc.marshall();
            NdefRecord nr = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    "application/graaby.app".getBytes(Charset.forName("US-ASCII")),
                    iv, data);
            NdefMessage nm = new NdefMessage(new NdefRecord[]{nr});
            return nm;
        } catch (Exception e) {
        }
        return null;
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
