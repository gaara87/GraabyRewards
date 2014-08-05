package graaby.app.wallet.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

/**
 * Created by gaara on 5/22/14.
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
                    findViewById(R.id.onboarding_next).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.onboarding_next).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.onboarding_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, getAccountNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /*RequestQueue queue = Volley.newRequestQueue(OnboardingActivity.this);

        JSONObject params = new JSONObject();
        try {
            params.put("email", textView.getText().toString());
            JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, "http://www.graaby.com/landing", params, OnboardingActivity.this, OnboardingActivity.this);
            registerRequest.setShouldCache(false);
            queue.add(registerRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
        try {
            if (response.getInt(getString(R.string.response_success)) == 1) {
                Toast.makeText(OnboardingActivity.this, "A verification e-mail has been sent to you", Toast.LENGTH_SHORT).show();
                OnboardingActivity.this.finish();
            } else if (response.getInt(getString(R.string.response_success)) == 0) {
                String msg = response.getString(getString(R.string.response_msg));
                Toast.makeText(OnboardingActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, OnboardingActivity.this);
    }

    /**
     * A simple pager adapter that represents 5 OnboardingPageFragment objects, in
     * sequence.
     */
    private class OnboardingPagerAdapter extends PagerAdapter {

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            boolean nextFlag = true;
            switch (position) {
                case 0:
                    resId = R.id.page_one;
                    break;
                case 1:
                    resId = R.id.page_two;
                    break;
                case 2:
                    resId = R.id.page_three;
                    nextFlag = false;
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
            return view == ((View) object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }
}

