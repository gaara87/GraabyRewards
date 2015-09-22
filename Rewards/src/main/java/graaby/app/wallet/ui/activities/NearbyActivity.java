package graaby.app.wallet.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import graaby.app.wallet.R;
import graaby.app.wallet.ui.fragments.NearbyFragment;

/**
 * Created by Akash.
 */
public class NearbyActivity extends BaseAppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(NearbyActivity.class.toString(), "Starting to look for graaby business devices");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        NearbyFragment fragment = new NearbyFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    protected void setupInjections() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
