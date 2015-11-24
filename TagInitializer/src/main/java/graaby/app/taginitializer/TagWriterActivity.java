package graaby.app.taginitializer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

import graaby.app.lib.nfc.core.AES256Cipher;
import graaby.app.lib.nfc.core.GraabyTag;

public class TagWriterActivity extends Activity implements Response.ErrorListener, Response.Listener<JSONObject> {

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private int statusCode = -1;

    private Long id;
    private byte[] key;
    private String name;
    private long expiryDate;
    private boolean male;


    private boolean businessFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (getIntent().hasExtra("json")) {
            try {
                JSONObject tagJson = new JSONObject(getIntent().getStringExtra("json"));
                statusCode = tagJson.getInt(getString(R.string.field_state));
                id = Long.valueOf(tagJson.getString(getString(R.string.field_id)));
                key = Base64.decode(tagJson.getString(getString(R.string.field_key)), Base64.DEFAULT);
                try {
                    name = tagJson.getString(getString(R.string.tag_field_name));
                    expiryDate = tagJson.getLong(getString(R.string.tag_field_expiry));
                    male = tagJson.getBoolean(getString(R.string.tag_field_gender));
                    if (!name.equals("") && statusCode == 0) {
                        statusCode = 1;
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.finish();
            }
        } else if (getIntent().hasExtra("b")) {
            businessFlag = getIntent().getBooleanExtra("b", false);
            mAdapter.setNdefPushMessage(createNdefMessage(), this);
        } else {
            this.finish();
        }


        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TagWriterActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefDiscoveredIntentFilter = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDiscoveredIntentFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("GraabyTagWriter", e);
        }
        mFilters = new IntentFilter[]{
                ndefDiscoveredIntentFilter
        };

        mTechLists = new String[][]{
                new String[]{
                        NfcA.class.getName()
                }, new String[]{
                Ndef.class.getName()
        }, new String[]{
                NdefFormatable.class.getName()
        }
        };

        setContentView(R.layout.activity_write_to_tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null)
            mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent != null && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))) {
            Tag tag = intent
                    .getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (businessFlag) {
                try {
                    if (GraabyTag.writeBusinessTag(this, tag)) {
                        Toast.makeText(this, "Business Tag Written Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "There was some problem while writing", Toast.LENGTH_SHORT).show();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Please make sure the tag is writable", Toast.LENGTH_SHORT).show();
                }
            } else {
                JSONObject postParameters = new JSONObject();
                try {
                    postParameters.put("confirm", statusCode + 1);
                    postParameters.put(getString(R.string.field_id), id);
                    boolean successFlag = false;
                    switch (statusCode) {
                        case 0:
                            successFlag = writeReturnStatus(GraabyTag.writeTag(this, id, key, tag));
                            break;
                        case 1:
                            successFlag = writeReturnStatus(GraabyTag.writeTag(this, id, key, name, male, expiryDate, tag));
                            break;
                        default:

                            this.finish();
                    }
                    postParameters.put(getString(R.string.field_iv), AES256Cipher.getIVStringFromTag(tag));
                    JsonObjectRequest request = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_tag_confirm), postParameters, this, this);
                    if (successFlag) {
                        VolleySingletonRequestQueue.getInstance(this).getRequestQueue().add(request);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Card NOT confirmed with server", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Toast.makeText(this, "Card confirmed with server", Toast.LENGTH_SHORT).show();
        this.finish();
    }


    private NdefMessage createNdefMessage() {
        NdefRecord nr = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                this.getString(graaby.app.lib.nfc.R.string.nfc_mime_type_business).getBytes(Charset.forName("US-ASCII")), new byte[0],
                new byte[0]);
        return new NdefMessage(new NdefRecord[]{nr});
    }

    private boolean writeReturnStatus(boolean writeStatus) {
        if (writeStatus) {
            Toast.makeText(this, "Tag written successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tag writing failed", Toast.LENGTH_SHORT).show();
        }
        return writeStatus;
    }
}
