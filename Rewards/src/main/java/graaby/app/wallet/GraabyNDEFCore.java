
package graaby.app.wallet;

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.bluelinelabs.logansquare.LoganSquare;
import com.crashlytics.android.Crashlytics;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import graaby.app.wallet.models.retrofit.UserCredentialsResponse;

public final class GraabyNDEFCore implements Parcelable {

    public static final Creator<GraabyNDEFCore> CREATOR = new Creator<GraabyNDEFCore>() {
        public GraabyNDEFCore createFromParcel(Parcel source) {
            return new GraabyNDEFCore();
        }

        public GraabyNDEFCore[] newArray(int size) {
            return new GraabyNDEFCore[size];
        }

    };
    public static final String CORE_FILENAME = "beamer";
    private static final String NDEF_TYPE_APPLICATION = "application/graaby.app";
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

    private GraabyNDEFCore(UserCredentialsResponse.NFCData object) {
        graabyID = object.id;
        String keyString = object.key;
        localAESKey = Base64.decode(keyString, Base64.DEFAULT);
        name = object.name;
        male = object.gender;

        Long expiryLong = object.expiry;

        this.expiryDate = Calendar.getInstance();
        this.expiryDate.setTime(new Date(expiryLong));

        this.expiry[0] = (byte) (this.expiryDate.get(Calendar.YEAR) - GraabyNDEFCore.BASE_YEAR);
        this.expiry[1] = (byte) this.expiryDate.get(Calendar.MONTH);
        this.expiry[2] = (byte) this.expiryDate.get(Calendar.DATE);
    }

    public static void saveNfcData(Context context, UserCredentialsResponse.NFCData data) {
        if (data != null) {
            try {
                FileOutputStream fos = context.openFileOutput(CORE_FILENAME, Activity.MODE_PRIVATE);
                LoganSquare.serialize(data, fos);
                fos.close();
            } catch (IOException ignored) {
                if (BuildConfig.USE_CRASHLYTICS) Crashlytics.logException(ignored);
            }
        }
    }

    public static NdefMessage createNdefMessage(Context applicationContext) throws IOException {
        Parcel pc = Parcel.obtain();
        FileInputStream fis = applicationContext.openFileInput(CORE_FILENAME);
        UserCredentialsResponse.NFCData nfcCore = LoganSquare.parse(fis, UserCredentialsResponse.NFCData.class);
        byte[] iv = Base64.decode(nfcCore.iv, Base64.DEFAULT);
        GraabyNDEFCore core = new GraabyNDEFCore(nfcCore);
        fis.close();

        core.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        byte[] data = pc.marshall();
        NdefRecord nr = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                NDEF_TYPE_APPLICATION.getBytes(Charset.forName("US-ASCII")),
                iv, data);
        return new NdefMessage(new NdefRecord[]{nr});
    }

    public static byte[] getGraabyUserAsBytes(Context applicationContext) throws IOException {
        Parcel pc = Parcel.obtain();
        FileInputStream fis = applicationContext.openFileInput(CORE_FILENAME);
        UserCredentialsResponse.NFCData nfcCore = LoganSquare.parse(fis, UserCredentialsResponse.NFCData.class);
        fis.close();
        GraabyNDEFCore core = new GraabyNDEFCore(nfcCore);

        core.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        return pc.marshall();
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

    public static String getGraabyUserID(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(CORE_FILENAME);
        UserCredentialsResponse.NFCData nfcCore = LoganSquare.parse(fis, UserCredentialsResponse.NFCData.class);
        fis.close();
        GraabyNDEFCore core = new GraabyNDEFCore(nfcCore);
        return String.valueOf(core.graabyID);
    }
}
