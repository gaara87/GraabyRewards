package graaby.app.wallet.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import graaby.app.wallet.R;
import graaby.app.wallet.ui.fragments.NearbyFragment;
import graaby.app.wallet.ui.fragments.ProfileFragment;

public class ProfileActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        NearbyFragment fragment = (NearbyFragment) getSupportFragmentManager().findFragmentByTag("nearby");
        if (fragment == null) {
            fragment = NearbyFragment.newInstance(false);
            getSupportFragmentManager().beginTransaction().add(fragment, "nearby").commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        ProfileFragment profileFragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, profileFragment).commit();
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
