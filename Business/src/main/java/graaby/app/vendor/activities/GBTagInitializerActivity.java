package graaby.app.vendor.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.vendor.R;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;

/**
 * Created by gaara on 10/21/13.
 */
public class GBTagInitializerActivity extends Activity implements View.OnClickListener {

    public RequestQueue mRequestQ;
    private GraabyTag userTag;
    private JSONObject fullTagJsonDetails, halfTagJsonDetails;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private boolean verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mRequestQ = VolleySingletonRequestQueue.getInstance(this.getApplicationContext()).
                getRequestQueue();
        userTag = (GraabyTag) getIntent().getSerializableExtra(GraabyTag.GraabyTagParcelKey);
        JSONObject postParameters = new JSONObject();
        try {
            postParameters.put(getString(R.string.field_id), userTag.getGraabyId());
        } catch (JSONException e) {
        }


        JsonObjectRequest registerCardRequest = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_register_user), postParameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setProgressBarIndeterminateVisibility(false);
                try {
                    String name = response.getString(getString(R.string.tag_field_name));
                    if (name.equals("")) {
                        halfTagJsonDetails = response;
                        findViewById(R.id.layout_id_info).setVisibility(View.VISIBLE);
                        findViewById(R.id.tv_tap_to_confirm).setVisibility(View.GONE);
                        findViewById(R.id.btn_verify_user_info).setOnClickListener(GBTagInitializerActivity.this);
                    } else {
                        fullTagJsonDetails = response;
                        findViewById(R.id.layout_id_info).setVisibility(View.GONE);
                        findViewById(R.id.tv_tap_to_confirm).setVisibility(View.VISIBLE);
                    }
                } catch (JSONException je) {
                    fullTagJsonDetails = null;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setProgressBarIndeterminateVisibility(false);
                setResult(R.integer.response_code_fail);
                Toast.makeText(GBTagInitializerActivity.this, getString(R.string.error_register_user), Toast.LENGTH_LONG).show();
                GBTagInitializerActivity.this.finish();
            }
        }
        );
        registerCardRequest.setShouldCache(false);
        mRequestQ.add(registerCardRequest);
        setProgressBarIndeterminateVisibility(true);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefDiscoveredIntentFilter = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDiscoveredIntentFilter.addDataType(getString(R.string.nfc_mime_type));
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

        setContentView(R.layout.activity_initialize);
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
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            if (fullTagJsonDetails != null) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    if (GraabyTag.writeTag(this, fullTagJsonDetails.getString(getString(R.string.tag_field_name)),
                            fullTagJsonDetails.getBoolean(getString(R.string.tag_field_gender)),
                            fullTagJsonDetails.getLong(getString(R.string.tag_field_expiry)), userTag, tag)) {
                        JSONObject postParameters = new JSONObject();
                        try {
                            postParameters.put(getString(R.string.field_id), userTag.getGraabyId());
                            postParameters.put(getString(R.string.field_confirm), getResources().getInteger(R.integer.confirm_tag_written_value));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest request = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_confirm_card), postParameters, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                setProgressBarIndeterminateVisibility(false);
                                Toast t = Toast.makeText(GBTagInitializerActivity.this, getString(R.string.toast_success_tag_write), Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.END | Gravity.CENTER_VERTICAL, 0, 0);
                                t.show();
                                setResult(getResources().getInteger(R.integer.response_code_success));
                                GBTagInitializerActivity.this.finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                setProgressBarIndeterminateVisibility(false);
                                Toast t = Toast.makeText(GBTagInitializerActivity.this, getString(R.string.toast_success_tag_write), Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.END | Gravity.CENTER_VERTICAL, 0, 0);
                                t.show();
                                setResult(getResources().getInteger(R.integer.response_code_success));
                                GBTagInitializerActivity.this.finish();
                            }
                        }
                        );
                        mRequestQ.add(request);
                        setProgressBarIndeterminateVisibility(true);

                    } else {
                        Toast.makeText(this, getString(R.string.error_write_fail), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (halfTagJsonDetails != null && verified) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    if (GraabyTag.writeTag(this, halfTagJsonDetails.getString(getString(R.string.tag_field_name)),
                            halfTagJsonDetails.getBoolean(getString(R.string.tag_field_gender)),
                            halfTagJsonDetails.getLong(getString(R.string.tag_field_expiry)), userTag, tag)) {
                        Toast t = Toast.makeText(GBTagInitializerActivity.this, getString(R.string.toast_success_tag_write), Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.END | Gravity.CENTER_VERTICAL, 0, 0);
                        t.show();
                        setResult(getResources().getInteger(R.integer.response_code_success));
                        GBTagInitializerActivity.this.finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_write_fail), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    @Override
    public void onClick(final View v) {
        Boolean localVerified = true;

        EditText et = ((EditText) findViewById(R.id.editText_primary_email));
        String email = et.getText().toString();
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et.setError(null);
        } else {
            et.setError("Incorrect email format");
            verified = false;
            et.requestFocus();
            return;
        }

        et = (EditText) findViewById(R.id.editText_primary_phone);
        String phone = et.getText().toString();
        if (Patterns.PHONE.matcher(phone).matches()) {
            et.setError(null);
        } else {
            et.setError("Incorrect phone format");
            verified = false;
            et.requestFocus();
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        CheckBox cb = (CheckBox) findViewById(R.id.checkBox_verify_mail);
        if (!cb.isChecked()) {
            cb.setError("Please verify your E-mail");
            localVerified = false;
        }

        cb = (CheckBox) findViewById(R.id.checkBox_verify_phone);
        if (!cb.isChecked()) {
            cb.setError("Please verify your Phone");
            localVerified = false;
        }

        if (localVerified) {

            JSONObject postParameters = new JSONObject();
            try {
                postParameters.put(getString(R.string.field_id), userTag.getGraabyId());
                postParameters.put(getString(R.string.field_email), email);
                postParameters.put(getString(R.string.field_phone_num), phone);
            } catch (JSONException e) {
            }
            JsonObjectRequest registerCardRequest = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_register_user), postParameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    setProgressBarIndeterminateVisibility(false);
                    try {
                        if (response.getInt(getString(R.string.response_success)) == 1) {
                            try {
                                String fieldName = getString(R.string.tag_field_name);
                                if (response.has(fieldName)) {
                                    String name = response.getString(fieldName);
                                    if (!name.equals("")) {
                                        fullTagJsonDetails = response;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            verified = true;
                            findViewById(R.id.layout_id_info).setVisibility(View.GONE);
                            findViewById(R.id.tv_tap_to_confirm).setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setProgressBarIndeterminateVisibility(false);
                    v.setClickable(Boolean.TRUE);
                    Toast.makeText(GBTagInitializerActivity.this, "There seems to be a problem contacting the server,try again later", Toast.LENGTH_LONG).show();
                    verified = false;
                }
            }
            );

            mRequestQ.add(registerCardRequest);
            v.setClickable(Boolean.FALSE);

            setProgressBarIndeterminateVisibility(true);
        }


    }

    @Override
    public void onBackPressed() {
        setResult(getResources().getInteger(R.integer.response_code_need_to_rewrite_tag));
        finish();
        super.onBackPressed();
    }
}
