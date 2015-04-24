package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Akash on 3/3/15.
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected CompositeSubscription mCompositeSubscriptions;
    @Inject
    protected NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.inject(this);
        mCompositeSubscriptions = new CompositeSubscription();
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mNfcAdapter.setNdefPushMessage(GraabyNDEFCore.createNdefMessage(this), this);
            }
        }

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null)
            setSupportActionBar(mToolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscriptions.unsubscribe();
    }

    public void onEvent(ToolbarEvents event) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(getResources().getColor(event.getToolbarBgColor()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(event.getSbBgColor()));
            }
        }
    }

}