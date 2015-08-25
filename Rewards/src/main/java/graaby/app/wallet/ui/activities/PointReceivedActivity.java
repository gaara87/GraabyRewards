package graaby.app.wallet.ui.activities;

import android.os.Bundle;

import graaby.app.wallet.R;
import graaby.app.wallet.gcm.GraabyGCMListenerService;
import graaby.app.wallet.ui.fragments.PointReceivedFromContactFragment;
import graaby.app.wallet.ui.fragments.PointReceivedFromTransactionFragment;

public class PointReceivedActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        if (savedInstanceState == null) {
            String notyAction = getIntent().getAction();
            switch (notyAction) {
                case GraabyGCMListenerService.NOTIFICATION_ACTION_POINTS: {
                    PointReceivedFromContactFragment frag = new PointReceivedFromContactFragment();
                    frag.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, frag)
                            .commit();
                    break;
                }
                case GraabyGCMListenerService.NOTIFICATION_ACTION_TX: {
                    PointReceivedFromTransactionFragment frag = new PointReceivedFromTransactionFragment();
                    frag.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, frag)
                            .commit();
                    break;
                }
            }
        }
    }

    @Override
    protected void setupInjections() {

    }
}
