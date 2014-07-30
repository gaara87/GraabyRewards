package graaby.app.wallet.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import graaby.app.wallet.R;
import graaby.app.wallet.fragments.PointReceivedFragment;

public class PointReceivedActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_received);
        if (savedInstanceState == null) {
            PointReceivedFragment frag = new PointReceivedFragment();
            frag.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();
        }
    }
}
