package graaby.app.wallet.activities;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.analytics.Tracker;

import java.io.File;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.util.CacheSubscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Akash on 3/3/15.
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected CompositeSubscription mCompositeSubscriptions;
    @Inject
    protected NfcAdapter mNfcAdapter;
    @Inject
    Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.inject(this);
        mCompositeSubscriptions = new CompositeSubscription();
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
        setNDEFForBeam();
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

    public void onEvent(ToolbarEvents.SetTitle event) {
        if (mToolbar != null) {
            mToolbar.setTitle(event.getName());
        }
    }

    private void setNDEFForBeam() {
        if (mNfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                try {
                    mNfcAdapter.setNdefPushMessage(GraabyNDEFCore.createNdefMessage(this), this);
                } catch (Exception e) {
                    File f = new File(getFilesDir() + "/beamer");
                    if (f.exists()) {
                        f.delete();
                        if (GraabyApplication.getOG().get(UserAuthenticationHandler.class).isAuthenticated())
                            mCompositeSubscriptions.add(
                                    GraabyApplication.getOG().get(ProfileService.class).getNFCInfo().observeOn(Schedulers.newThread())
                                            .subscribeOn(Schedulers.newThread())
                                            .subscribe(new CacheSubscriber<UserCredentialsResponse.NFCData>(this) {
                                                @Override
                                                public void onSuccess(UserCredentialsResponse.NFCData result) {
                                                    GraabyNDEFCore.saveNfcData(BaseAppCompatActivity.this, result);
                                                }
                                            })
                            );
                    }
                }
            }
        }
    }
}