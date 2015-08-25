package graaby.app.wallet.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import graaby.app.wallet.R;
import graaby.app.wallet.ui.fragments.FeedFragment;

public class FeedActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        Fragment fragment = new FeedFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    protected void setupInjections() {

    }

}
