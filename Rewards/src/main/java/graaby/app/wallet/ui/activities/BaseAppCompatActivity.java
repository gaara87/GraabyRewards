package graaby.app.wallet.ui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.iid.InstanceID;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.events.AuthEvents;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import graaby.app.wallet.util.CacheSubscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Akash on 3/3/15.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {
    private static boolean isExecutingLogoutProcess = false;
    protected Toolbar mToolbar;
    protected CompositeSubscription mCompositeSubscriptions;
    @Inject
    @Nullable
    protected NfcAdapter mNfcAdapter;
    @Inject
    protected UserAuthenticationHandler authHandler;
    @Inject
    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.getApplication().getComponent().inject(this);
        setupInjections();
        mCompositeSubscriptions = new CompositeSubscription();
    }

    protected abstract void setupInjections();

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
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscriptions.unsubscribe();
    }

    @Subscribe(sticky = true)
    public void handle(ToolbarEvents event) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(getResources().getColor(event.getToolbarBgColor()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(event.getSbBgColor()));
            }
        }
    }

    @Subscribe
    public void handle(ToolbarEvents.SetTitle event) {
        if (mToolbar != null) {
            mToolbar.setTitle(event.getName());
        }
    }

    @Subscribe(sticky = true)
    public void handle(AuthEvents.LoggedOutEvent event) {
        EventBus.getDefault().removeStickyEvent(AuthEvents.LoggedOutEvent.class);
        if (!isExecutingLogoutProcess) {
            isExecutingLogoutProcess = true;
            final AccountManager acm = AccountManager.get(this);
            final Account[] accounts = acm
                    .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);

            switch (event.typeOfEvent) {
                case UPDATE:
                    if (accounts.length != 0) {
                        try {
                            InstanceID.getInstance(this).deleteInstanceID();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        acm.updateCredentials(accounts[0], UserLoginActivity.AUTHTOKEN_TYPE, null, this, future -> {
                            if (future.isDone()) {
                                try {
                                    authHandler.initFromFuture(future);
                                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                                    e.printStackTrace();
                                    isExecutingLogoutProcess = false;
                                }
                            }
                        }, null);
                    }
                    break;
                case REMOVE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (acm.removeAccountExplicitly(accounts[0])) {
                            finishLogoutProcess();
                        }
                    } else {
                        acm.removeAccount(accounts[0], future -> {
                            if (future.isDone()) {
                                try {
                                    if (future.getResult() != null) {
                                        finishLogoutProcess();
                                    }
                                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                                    e.printStackTrace();
                                    isExecutingLogoutProcess = false;
                                }

                            }
                        }, null);
                    }
                    break;
            }
        }
    }

    private void finishLogoutProcess() {
        authHandler.logout(this);
        Toast.makeText(BaseAppCompatActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        isExecutingLogoutProcess = false;
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
                        if (((GraabyApplication) getApplication()).getComponent().userAuthenticationHandler().isAuthenticated())
                            mCompositeSubscriptions.add(
                                    ((GraabyApplication) getApplication()).getApiComponent().profileServce().getNFCInfo().observeOn(Schedulers.newThread())
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