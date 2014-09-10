package graaby.app.wallet.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import graaby.app.wallet.Helper;
import graaby.app.wallet.R;
import graaby.app.wallet.fragments.PointReceivedFromContactFragment;
import graaby.app.wallet.fragments.PointReceivedFromTransactionFragment;
import graaby.app.wallet.gcm.GcmIntentService;

public class PointReceivedActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_received);
        if (savedInstanceState == null) {
            int noty = getIntent().getExtras().getInt(Helper.NOTIFICATIONID);
            switch (noty) {
                case GcmIntentService.NOTIFICATION_ID_POINTS: {
                    PointReceivedFromContactFragment frag = new PointReceivedFromContactFragment();
                    frag.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, frag)
                            .commit();
                    break;
                }
                case GcmIntentService.NOTIFICATION_ID_TX: {
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
