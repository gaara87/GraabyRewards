package graaby.app.wallet.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

public class BusinessDetailsctivity extends ActionBarActivity implements
        ErrorListener, Listener<JSONObject>, SwipeRefreshLayout.OnRefreshListener {

    private int businessId;
    private JSONObject placeNode;
    private SwipeRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);

        mPullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setOnRefreshListener(this);
        mPullToRefreshLayout.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);

        getSupportActionBar().setTitle(R.string.title_activity_business_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);

        try {
            placeNode = new JSONObject(info);
        } catch (JSONException e) {
            placeNode = new JSONObject();
        }


        try {
            businessId = placeNode.getInt(getString(R.string.field_business_outlet_id));
            getSupportActionBar().setTitle(placeNode.getString(getString(
                    R.string.field_business_title)));
        } catch (JSONException e) {
            businessId = 0;
        }

        sendRequest();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(Helper.createNdefMessage(this), this);
            }
        }
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        try {
            params.put(getString(R.string.field_business_outlet_id), businessId);
            Helper.getRQ().add(new CustomRequest("store", params, this, this));
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
        } catch (NotFoundException e1) {
        } catch (JSONException e1) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (placeNode.has(getString(R.string.field_business_latitude)) && placeNode.has(getString(R.string.field_business_longitude)))
            getMenuInflater().inflate(R.menu.menu_business_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_menu_item_directions:

                String geoUri = null;
                try {
                    geoUri = "http://maps.google.com/maps?f=d&daddr=" + placeNode.getDouble(getString(R.string.field_business_latitude)) + "," + placeNode.getDouble(getString(R.string.field_business_longitude));
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(geoUri));
                    intent.setComponent(new ComponentName("com.google.android.apps.maps",
                            "com.google.android.maps.MapsActivity"));
                    startActivity(intent);
                } catch (JSONException jse) {
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(JSONObject response) {
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        ImageView iv = (ImageView) findViewById(R.id.item_businessPicImageView);
        try {
            Helper.getImageLoader().get(response.getString(getString(
                    R.string.pic_url)), ImageLoader.getImageListener(iv, R.drawable.default_business_profile_image, R.drawable.default_business_profile_image));
        } catch (NotFoundException e) {
            iv.setImageResource(R.drawable.default_business_profile_image);
        } catch (JSONException e) {
            iv.setImageResource(R.drawable.default_business_profile_image);
        }

        try {
            getSupportActionBar().setTitle(response.getString(getResources().getString(
                    R.string.field_business_name)));

        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }

        TextView tv = (TextView) findViewById(R.id.item_businessAddressTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.field_)));
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }

        tv = (TextView) findViewById(R.id.item_businessPhoneTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.business_phone)));
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }

        tv = (TextView) findViewById(R.id.item_businessSiteTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.business_site)));
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, this);
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }
}
