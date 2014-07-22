package graaby.app.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import graaby.app.wallet.activities.BusinessDetailsctivity;

public class Helper {

    public final static String serverURL = "http://mapi.graaby.com/";
    public final static int connectionTimeout = 5000;
    public final static int socketTimeout = 8000;
    public final static int LOADER_ID = 0;
    public static String INTENT_CONTAINER_INFO = "jsonnode_key";
    public static String KEY_TYPE = "type";
    public static String MY_DISCOUNT_ITEMS_FLAG = "market";
    public static String BRAND_ID_BUNDLE_KEY = "bidkey";
    public static final String ARG_SECTION_NUMBER = "section_number";

    public static enum DiscountItemType {
        Coupons(1), Vouchers(2), Punch(3);
        private final int value;

        private DiscountItemType(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }

        public static DiscountItemType getType(int x) {
            switch (x) {
                case 1:
                    return Coupons;
                case 2:
                    return Vouchers;
                case 3:
                    return Punch;
            }
            return null;
        }

        public static DiscountItemType getType(String type, Context context) {
            if (type.equals(context.getString(R.string.market_item_type_coupon))) {
                return Coupons;
            } else if (type.equals(context.getString(R.string.market_item_type_voucher))) {
                return Vouchers;
            } else if (type.equals(context.getString(R.string.market_item_type_punch))) {
                return Punch;
            }
            return null;
        }
    }

    ;

    public static enum ActivityType {
        Transaction(1), Buy_Coupon(2), Buy_Voucher(3), Check_in(4), Share_Points(5), Receieve_Points(6);
        private final int value;

        private ActivityType(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }

        public static ActivityType getType(int x) {
            switch (x) {
                case 1:
                    return Transaction;
                case 2:
                    return Buy_Coupon;
                case 3:
                    return Buy_Voucher;
                case 4:
                    return Check_in;
                case 5:
                    return Share_Points;
                case 6:
                    return Receieve_Points;
            }
            return null;
        }
    }

    private static String mAuthToken = "";
    private static RequestQueue mRequestQ;
    private static ImageLoader mImageLoader;

    public static void initializeAppWorkers(String token, Context context) {
        mAuthToken = token;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mRequestQ = Volley.newRequestQueue(context);
        } else {
            mRequestQ = Volley.newRequestQueue(context, new OkHttpStack());
        }

        mImageLoader = new ImageLoader(mRequestQ, new BitmapLruCache());
    }

    public static String getAuth_token() {
        return mAuthToken;
    }

    public static RequestQueue getRQ() {
        return mRequestQ;
    }

    public static ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static void openBusiness(Activity activity, JSONObject node) {
        Intent intent = new Intent(activity, BusinessDetailsctivity.class);
        intent.putExtra(Helper.INTENT_CONTAINER_INFO, node.toString());
        activity.startActivity(intent);
    }

    public static void handleVolleyError(VolleyError error, Context context) {
        int errorResourceString = -1;
        if (error instanceof ParseError) {
            errorResourceString = R.string.error_data_parsing;
        } else if (error instanceof NetworkError) {
            errorResourceString = R.string.error_data_connection_not_available;
        } else if (error instanceof ServerError) {
            errorResourceString = R.string.error_server;
        } else if (error instanceof AuthFailureError) {
            errorResourceString = R.string.error_unauthorized;
            getRQ().getCache().clear();
        }
        if (errorResourceString != -1)
            Toast.makeText(context, errorResourceString,
                    Toast.LENGTH_SHORT).show();
    }

    public static String getRepString(String original) {
        Integer number = Math.round(Float.parseFloat(original));
        String[] suffix = new String[]{"k", "m", "b", "t"};
        int size = (number.intValue() != 0) ? (int) Math.log10(number) : 0;
        if (size >= 3) {
            while (size % 3 != 0) {
                size = size - 1;
            }
        }
        double notation = Math.pow(10, size);
        String result = (size >= 3) ? +(Math.round((number / notation) * 100) / 100.0d) + suffix[(size / 3) - 1] : +number + "";
        return result;
    }

    public static NdefMessage createNdefMessage(Context context) {
        Parcel pc = Parcel.obtain();
        try {
            FileInputStream fis = context.openFileInput("beamer");
            DataInputStream ois = new DataInputStream(fis);
            String jsonString = ois.readUTF();
            JSONObject jsonCore = new JSONObject(jsonString);
            byte[] iv = Base64.decode(jsonCore.getString(context.getString(R.string.core_iv)), Base64.DEFAULT);
            GraabyCore core = new GraabyCore(jsonCore, context);
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
}