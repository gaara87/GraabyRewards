package graaby.app.wallet.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

/**
 * Created by gaara on 5/22/14.
 * Make some impeccable shyte
 */
public class OnboardingActivity extends ActionBarActivity implements Response.ErrorListener, Response.Listener<JSONObject> {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private EditText mNameView;
    private GetNameTask task;
    private Spinner mSpinner;
    private EditText mPasswordView;
    private Button mRegisterButton;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_onboarding);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new OnboardingPagerAdapter();
        mPager.setOffscreenPageLimit(3);
        mPager.setAdapter(mPagerAdapter);

        findViewById(R.id.onboarding_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
        });

        findViewById(R.id.onboarding_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnboardingActivity.this.finish();
            }
        });

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    findViewById(R.id.onboarding_next).setEnabled(Boolean.FALSE);
                } else {
                    findViewById(R.id.onboarding_next).setEnabled(Boolean.TRUE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSpinner = (Spinner) findViewById(R.id.onboarding_spinner);
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, getAccountNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mPasswordView = (EditText) findViewById(R.id.password);
        mNameView = (EditText) findViewById(R.id.login_fullname);

        mRegisterButton = (Button) findViewById(R.id.register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cancel = false;
                mPasswordView.setError(null);
                String mPassword = mPasswordView.getText().toString();
                String name = mNameView.getText().toString();
                View focusView = null;
                if (TextUtils.isEmpty(mPassword)) {
                    mPasswordView.setError(getString(R.string.error_field_required));
                    focusView = mPasswordView;
                    cancel = true;
                } else if (mPassword.length() < 4) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    focusView = mPasswordView;
                    cancel = true;
                }

                if (mNameView.getVisibility() == View.VISIBLE) {
                    if (TextUtils.isEmpty(name)) {
                        mNameView.setError(getString(R.string.error_field_required));
                        focusView = mNameView;
                        cancel = true;
                    } else if (name.split("\\s").length != 2) {
                        mNameView.setError(getString(R.string.error_field_lastname));
                        focusView = mNameView;
                        cancel = true;
                    }
                }

                if (cancel) {
                    if (focusView != null)
                        focusView.requestFocus();
                } else {
                    if (mNameView.getVisibility() == View.VISIBLE) {
                        RequestQueue queue = Volley.newRequestQueue(OnboardingActivity.this);

                        HashMap<String, Object> params = new HashMap<String, Object>();
                        try {
                            params.put("email", mSpinner.getSelectedItem().toString());
                            params.put("pwd", mPassword);
                            String[] names = name.split("\\s");
                            params.put("firstname", names[0]);
                            params.put("lastname", names[1]);
                            if (!TextUtils.isEmpty(mToken)) {
                                params.put("token", mToken);
                                params.put("provider", "g");
                            }

                            CustomRequest registerRequest = new CustomRequest("register", params, OnboardingActivity.this, OnboardingActivity.this);
                            registerRequest.setShouldCache(false);
                            Toast.makeText(OnboardingActivity.this, "Attempting to register", Toast.LENGTH_SHORT).show();
                            view.setEnabled(false);
                            queue.add(registerRequest);
                            toggleViews(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        task = new GetNameTask();
                        task.execute(mSpinner.getSelectedItem().toString());
                        view.setEnabled(false);
                    }
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 102) {
                task.execute(mSpinner.getSelectedItem().toString());
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == 102) {
                task.cancel(false);
                Toast.makeText(this, "Cannot register without permission", Toast.LENGTH_SHORT).show();
                mNameView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void toggleViews(boolean show) {
        mSpinner.setEnabled(show);
        mNameView.setEnabled(show);
        mPasswordView.setEnabled(show);
        if (show)
            mRegisterButton.setVisibility(View.VISIBLE);
        else
            mRegisterButton.setVisibility(View.GONE);
    }

    private String[] getAccountNames() {
        AccountManager mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(
                GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onResponse(JSONObject response) {

        Toast.makeText(this, "Registered. Logging in now", Toast.LENGTH_SHORT).show();
        try {
            if (response.getInt(getString(R.string.response_success)) == 1) {
                Intent intent = new Intent();
                intent.putExtra("uname", mSpinner.getSelectedItem().toString());
                intent.putExtra("pwd", mPasswordView.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } else if (response.getInt(getString(R.string.response_success)) == 0) {
                String msg = response.getString(getString(R.string.response_msg));
                Toast.makeText(OnboardingActivity.this, msg, Toast.LENGTH_SHORT).show();
                toggleViews(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, OnboardingActivity.this);
        mRegisterButton.setEnabled(true);
    }

    private class GetNameTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            try {
                mToken = GoogleAuthUtil.getToken(OnboardingActivity.this, email, "oauth2:" + Scopes.PLUS_LOGIN);
                URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token="
                        + mToken);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                int serverCode = con.getResponseCode();
//successful query
                if (serverCode == 200) {
                    InputStream is = con.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(is));
                    StringBuilder total = new StringBuilder(is.available());
                    String name;
                    while ((name = r.readLine()) != null) {
                        total.append(name);
                    }
                    is.close();
                    return name;
//bad token, invalidate and get a new one
                } else if (serverCode == 401) {
                    GoogleAuthUtil.invalidateToken(OnboardingActivity.this, mToken);
                    return null;
//unknown error, do something else
                } else {
                    Log.e("Server returned the following error code: " + serverCode, null);
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UserRecoverableAuthException ure) {
                OnboardingActivity.this.startActivityForResult(ure.getIntent(), 102);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s)) {
                mNameView.setText(s);
                mNameView.setVisibility(View.VISIBLE);
            }
            toggleViews(true);
            super.onPostExecute(s);
        }
    }

    /**
     * A simple pager adapter that represents 5 OnboardingPageFragment objects, in
     * sequence.
     */
    private class OnboardingPagerAdapter extends PagerAdapter {

        public Object instantiateItem(View collection, int position) {

            int resId;
            switch (position) {
                case 0:
                    resId = R.id.page_one;
                    break;
                case 1:
                    resId = R.id.page_two;
                    break;
                case 2:
                    resId = R.id.page_three;
                    break;
                default:
                    resId = R.id.page_three;
                    break;
            }
            return findViewById(resId);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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

