package graaby.app.wallet.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.BuildConfig;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.AccountAuthenticatorActivity;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.fragments.LoginFragment;
import graaby.app.wallet.fragments.RegistrationFragment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class UserLoginActivity extends AccountAuthenticatorActivity implements
        RegistrationFragment.OnRegistrationListener, LoginFragment.LoginInterface, TabLayout.OnTabSelectedListener {

    final public static String ACCOUNT_TYPE = BuildConfig.ACCOUNT_AUTHENTICATOR;
    final public static String AUTHTOKEN_TYPE = "graaby.app.wallet";
    final public static String AUTHTOKEN_USERDATA_KEY = "oauth";
    final private static String TAG = "UserLoginActivity";
    @Bind(R.id.pager)
    ViewPager mPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;


    // UI references.

    public static String[] getAccountNames(Context context) {
        AccountManager mAccountManager = AccountManager.get(context);
        Account[] accounts = mAccountManager.getAccountsByType(
                GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        mPager.setAdapter(new UserLoginPagerAdapter());
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.setOnTabSelectedListener(this);

        AccountManager acm = AccountManager.get(this);
        Account[] accounts = acm
                .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);

        if (accounts.length != 0) {
            if (getIntent().hasExtra("update")) {
                acm.removeAccount(accounts[0], null, null);
            } else {
                Toast.makeText(this, "Only 1 account allowed", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }


    }

    @Override
    public void onLoginSuccessful(Intent intent) {
        EventBus.getDefault().postSticky(new ProfileEvents.LoginSuccessfulEvent());
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onNewUserRegisterRequest() {
        mPager.setCurrentItem(1, true);
    }

    @Override
    public void onRegistrationComplete(String email, String password) {
        mPager.setCurrentItem(0, true);
        LoginFragment fragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_frag);
        fragment.loginAfterRegistering(email, password);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private class UserLoginPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    return findViewById(R.id.login_frag);
                case 1:
                    return findViewById(R.id.register_frag);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_login);
                case 1:
                    return getString(R.string.title_register);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
