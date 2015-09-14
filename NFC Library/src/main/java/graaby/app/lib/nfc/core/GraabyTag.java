package graaby.app.lib.nfc.core;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import graaby.app.lib.nfc.R;

public final class GraabyTag implements Serializable {

    private static final long serialVersionUID = 2940380582738452043L;
    private static final String TAG = GraabyTag.class.getSimpleName();
    public static String GraabyTagParcelKey = "graabyTagParcel";
    protected Boolean isTagFormedThroughBeam = Boolean.FALSE;
    private GraabyCore graabyCore = null;
    private byte[] tagID = null;
    private AES256Cipher cipher = null;

    public GraabyTag() {

    }

    private GraabyTag(GraabyCore coreObject) {
        graabyCore = coreObject;
    }

    public GraabyTag(GraabyCore graabyCoreTemp, byte[] id) {
        this.graabyCore = graabyCoreTemp;
        this.tagID = id;
    }

    public static GraabyTag parseNDEFInfo(Context context, Tag tag, Parcelable[] parcelledNdefs) {
        Parcel pc = Parcel.obtain();

        try {
            NdefMessage ndm = (NdefMessage) parcelledNdefs[0];
            NdefRecord nr = ndm.getRecords()[0];

            if (new String(nr.getType(), "US-ASCII").equals(
                    context.getString(R.string.nfc_mime_type))) {
                pc.unmarshall(nr.getPayload(), 0, nr.getPayload().length);
                GraabyCore graabyCoreTemp = GraabyCore.CREATOR.createFromParcel(pc);
                if (nr.getId().length != 0) {
                    GraabyTag gt = new GraabyTag(graabyCoreTemp);
                    gt.isTagFormedThroughBeam = Boolean.TRUE;
                    gt.tagID = nr.getId();
                    return gt;
                }
                return new GraabyTag(graabyCoreTemp, tag.getId());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean writeTag(Context context, Long id, byte[] key, Tag tag) {
        Parcel pc = Parcel.obtain();
        {
            GraabyCore writeObj = new GraabyCore(id, key, "", true, 0);
            writeObj.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        }
        byte[] data = pc.marshall();
        return actualWrite(context, data, tag);
    }

    public static boolean writeTag(Context context, Long id, byte[] key, String name, boolean male, long expiryDate, Tag tag) {
        Parcel pc = Parcel.obtain();
        {
            GraabyCore writeObj = new GraabyCore(id, key, name, male, expiryDate);
            writeObj.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        }
        byte[] data = pc.marshall();
        return actualWrite(context, data, tag);
    }

    public static Boolean writeTag(Context context, String name, Boolean male, Long expiryDate, GraabyTag parsedTag, Tag tag) {

        Parcel pc = Parcel.obtain();
        {
            GraabyCore writeObj = new GraabyCore(parsedTag.graabyCore, name, male, expiryDate);
            writeObj.writeToParcel(pc, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        }
        byte[] data = pc.marshall();
        return actualWrite(context, data, tag);
    }

    private static Boolean actualWrite(Context context, byte[] data, Tag tag) {
        NdefRecord nr = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                context.getString(R.string.nfc_mime_type).getBytes(Charset.forName("US-ASCII")), new byte[0],
                data);
        NdefMessage nm = new NdefMessage(new NdefRecord[]{nr});
        ArrayList<String> techList = new ArrayList<String>(Arrays.asList(tag.getTechList()));
        if (techList.contains("android.nfc.tech.NdefFormatable")) {
            Log.i(TAG, "NdefFormattable Tag found");
            NdefFormatable nf = NdefFormatable.get(tag);
            try {
                nf.connect();
                while (!nf.isConnected()) {

                }
                nf.format(nm);
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    nf.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return Boolean.FALSE;
            }
        } else if (techList.contains("android.nfc.tech.Ndef")) {
            Log.i(TAG, "Ndef Tag found");
            Ndef nf = Ndef.get(tag);
            try {
                nf.connect();
                while (!nf.isConnected()) {

                }
                nf.writeNdefMessage(nm);
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    nf.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    public static Boolean writeBusinessTag(Context context, Tag tag) {
        NdefRecord nr = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                context.getString(R.string.nfc_mime_type_business).getBytes(Charset.forName("US-ASCII")), new byte[0],
                new byte[0]);
        NdefMessage nm = new NdefMessage(new NdefRecord[]{nr});

        if (tag.getTechList()[2].contains("NdefFormatable")) {
            Log.i(TAG, "NdefFormattable Tag found");
            NdefFormatable nf = NdefFormatable.get(tag);
            try {
                nf.connect();
                while (!nf.isConnected()) {

                }
                nf.format(nm);
                return Boolean.TRUE;
            } catch (Exception e) {
                try {
                    nf.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return Boolean.FALSE;
            }
        } else if (tag.getTechList()[2].contains("Ndef")) {
            Log.i(TAG, "Ndef Tag found");
            Ndef nf = Ndef.get(tag);
            try {
                nf.connect();
                while (!nf.isConnected()) {

                }
                nf.writeNdefMessage(nm);
                return Boolean.TRUE;
            } catch (Exception e) {
                try {
                    nf.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    public static boolean authenticateBusinessTag(Context context, Parcelable[] ndef) {

        NdefMessage ndm = (NdefMessage) ndef[0];
        NdefRecord nr = ndm.getRecords()[0];

        try {
            if (new String(nr.getType(), "US-ASCII").equals(
                    context.getString(R.string.nfc_mime_type_business))) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return false;
        }

        return false;
    }


    public byte[] getTagID() {
        return tagID;
    }

    public boolean isCoreAvailable() {
        return (graabyCore == null) ? Boolean.FALSE : Boolean.TRUE;
    }

    public String getGraabyUserName() {
        try {
            return graabyCore.getName();
        } catch (NullPointerException ne) {

        }
        return "";
    }

    public String getGraabyIdString() {
        try {
            return graabyCore.getId().toString();
        } catch (NullPointerException ne) {
            return "";
        }
    }

    public Long getGraabyId() {
        try {
            return graabyCore.getId();
        } catch (NullPointerException ne) {
            return 0L;
        }
    }

    public boolean isTagStillValid() {
        return Calendar.getInstance().before(graabyCore.getExpiryDate());
    }

    public String getExpiryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(graabyCore.getExpiryDate().getTime());
    }

    public boolean isMale() {
        try {
            return graabyCore.getGender();
        } catch (NullPointerException e) {
            return Boolean.TRUE;
        }
    }

    public boolean isTagGraabified() {
        return graabyCore.isFullyInitialized();
    }

    public boolean isTagSemiGraabified() {
        return graabyCore.isHalfInitialized();
    }

    protected byte[] getEncKey() {
        return graabyCore.getKey();
    }

    public void clear() {
        graabyCore = null;
    }

    public String encryptData(String data) {
        try {
            return cipher.encryptDataToString(data.getBytes());
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }

    public void initliazeCrypto() {
        cipher = new AES256Cipher(this.tagID, getEncKey(), isTagFormedThroughBeam);
    }

    public void clearCrypto() {
        cipher = null;
    }

    public JSONObject decryptStringToJsonObject(String encryptedDataString, String encodedIVString) {
        try {
            byte[] iv = Base64.decode(encodedIVString, Base64.DEFAULT);
            byte[] data = Base64.decode(encryptedDataString, Base64.DEFAULT);
            byte[] rawJsonByteData = AES256Cipher.decrypt(iv, graabyCore.getKey(), data);
            String parsedData = new String(rawJsonByteData, "UTF-8");
            return new JSONObject(parsedData);
        } catch (Exception e) {
        }
        return null;
    }
}
