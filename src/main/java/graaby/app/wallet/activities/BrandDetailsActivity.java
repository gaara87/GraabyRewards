package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.wallet.Helper;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.BusinessesFragment;

public class BrandDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_details);

        String info = getIntent().getExtras().getString(
                Helper.INTENT_CONTAINER_INFO);
        int bid = -1;
        try {
            JSONObject brandNode = new JSONObject(info);
            bid = brandNode.getInt(getString(R.string.business_id));
            getSupportActionBar().setTitle(brandNode.getString(getString(R.string.business_name)));
        } catch (JSONException e1) {

        }

        Fragment fragment = new BusinessesFragment();
        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());
        args.putInt(Helper.BRAND_ID_BUNDLE_KEY, bid);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(Helper.createNdefMessage(this), this);
            }
        }
    }
}
