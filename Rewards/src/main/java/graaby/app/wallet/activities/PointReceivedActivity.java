package graaby.app.wallet.activities;

import android.os.Bundle;

import graaby.app.wallet.R;
import graaby.app.wallet.fragments.PointReceivedFromContactFragment;
import graaby.app.wallet.fragments.PointReceivedFromTransactionFragment;
import graaby.app.wallet.services.GcmIntentService;

public class PointReceivedActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        if (savedInstanceState == null) {
            String notyAction = getIntent().getAction();
            switch (notyAction) {
                case GcmIntentService.NOTIFICATION_ACTION_POINTS: {
                    PointReceivedFromContactFragment frag = new PointReceivedFromContactFragment();
                    frag.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, frag)
                            .commit();
                    break;
                }
                case GcmIntentService.NOTIFICATION_ACTION_TX: {
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
}
