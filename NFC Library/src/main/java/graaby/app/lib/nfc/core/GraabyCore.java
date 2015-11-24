package graaby.app.lib.nfc.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * The CORE and most cellular object which is used to construct and contain
 * unencrypted data pertaining to a graaby tag/user
 *
 * @author gaara
 */
final class GraabyCore implements Parcelable, Serializable {

    /**
     * Construction of graabycore from another parcel
     */
    public static final Creator<GraabyCore> CREATOR = new Creator<GraabyCore>() {
        public GraabyCore createFromParcel(Parcel source) {
            return new GraabyCore(source);
        }

        public GraabyCore[] newArray(int size) {
            return new GraabyCore[size];
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

    /**
     * Used while reading and constructing the core from a tag Would mostly be
     * whenever a user taps
     *
     * @param source
     */
    private GraabyCore(Parcel source) {
        source.setDataPosition(0);
        this.graabyID = source.readLong();

        source.readByteArray(this.expiry);
        this.expiryDate = Calendar.getInstance();
        this.expiryDate.set(this.expiry[0] + GraabyCore.BASE_YEAR,
                this.expiry[1], this.expiry[2]);

        this.name = source.readString();

        boolean[] b = new boolean[]{Boolean.TRUE};
        source.readBooleanArray(b);
        this.male = b[0];

        source.readByteArray(this.localAESKey);
    }


    protected GraabyCore(Long id, byte[] key) {
        this.graabyID = id;
        this.expiry[0] = (byte) 0;
        this.expiry[1] = (byte) 0;
        this.expiry[2] = (byte) 0;
        this.name = "";
        this.male = Boolean.TRUE;
        if (key != null && key.length == 32)
            this.localAESKey = key;

    }

    /**
     * Used when graaby internally initializes a tag as a graaby tag. Key is
     * fetched from the server a temporary object is constructed from it
     *
     * @param id         - 16 digit id with just numbers
     * @param key        - byte array of the encryption key
     * @param name       - name of the person the tag belongs to
     * @param male       - whether the person is male (true) or female (false)
     * @param expiryDate - expiry date of the tag in milliseconds
     */
    protected GraabyCore(Long id, byte[] key, String name, boolean male, long expiryDate) {
        this.graabyID = id;
        this.localAESKey = key;
        this.name = name;
        this.male = male;
        if (expiryDate != 0) {
            this.expiryDate = Calendar.getInstance();
            this.expiryDate.setTime(new Date(expiryDate));
            this.expiry[0] = (byte) (this.expiryDate.get(Calendar.YEAR) - GraabyCore.BASE_YEAR);
            this.expiry[1] = (byte) this.expiryDate.get(Calendar.MONTH);
            this.expiry[2] = (byte) this.expiryDate.get(Calendar.DATE);
        }
    }

    protected GraabyCore(GraabyCore graabyCore, String name, Boolean male, Long expiryDate) {
        this.graabyID = graabyCore.graabyID;
        this.localAESKey = graabyCore.localAESKey;
        this.name = name;
        this.male = male;
        this.expiryDate = Calendar.getInstance();
        this.expiryDate.setTime(new Date(expiryDate));
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
        dest.writeBooleanArray(new boolean[]{this.male});
        dest.writeByteArray(this.localAESKey);
    }

    protected String getName() {
        return this.name;
    }

    protected Long getId() {
        return this.graabyID;
    }

    protected Calendar getExpiryDate() {
        return this.expiryDate;
    }

    protected Boolean getGender() {
        return this.male;
    }

    protected byte[] getKey() {
        return this.localAESKey;
    }

    protected boolean isFullyInitialized() {
        if ((this.expiry[0] == 0) || (this.name.equals(""))) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean isHalfInitialized() {
        if (this.graabyID != 0 && !(this.localAESKey[0] == 0 && this.localAESKey[1] == 0) && this.expiry[0] != 0) {
            return true;
        } else {
            return false;
        }
    }
}
