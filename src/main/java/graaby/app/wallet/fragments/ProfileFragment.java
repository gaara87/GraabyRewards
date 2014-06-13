package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.Helper.ActivityType;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.MyDiscountItemsActivity;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ProfileFragment extends Fragment implements OnClickListener,
        ErrorListener, Listener<JSONObject> {

    private Activity mActivity;
    private PullToRefreshLayout mPullToRefreshLayout;
    private CustomRequest profileRequest;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(Boolean.TRUE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_profile, null);
        v.findViewById(R.id.profile_coupons_viewall_button).setOnClickListener(
                this);
        v.findViewById(R.id.profile_vouchers_viewall_button)
                .setOnClickListener(this);
        mPullToRefreshLayout = (PullToRefreshLayout) v.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(mActivity)
                .options(Options.create()
                        // Here we make the refresh scroll distance to 75% of the refreshable view's height
                        .scrollDistance(.5f).build())
                        // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set the OnRefreshListener
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        sendRequest();
                    }
                })
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
        sendRequest();
        return v;
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        // TODO Create and save User ID
        params.put(getResources().getString(R.string.userid), "");

        try {
            profileRequest = new CustomRequest("user", params, this, this);

            Helper.getRQ().add(profileRequest);
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(Helper.getRQ().getCache().get(profileRequest.getCacheKey()));
            if (jsonCachedResponse != null) {
                onResponse(jsonCachedResponse);
            }
        } catch (JSONException e) {
        } catch (NullPointerException npe) {
        } finally {
            try {
                mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            } catch (NullPointerException npe) {

            }
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.has(getString(R.string.response_msg))) {

            } else {
                refreshDetails(
                        response.getJSONObject(mActivity.getResources().getString(
                                R.string.profile_bio)),
                        response.getJSONObject(mActivity.getResources().getString(
                                R.string.profile_points)),
                        response.getJSONArray(mActivity.getResources().getString(
                                R.string.profile_recents))
                );
            }
        } catch (Exception e) {
        } finally {
            try {
                mPullToRefreshLayout.setRefreshComplete();
            } catch (NullPointerException npe) {
            }
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

        Helper.handleVolleyError(error, mActivity);

        try {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(profileRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            mPullToRefreshLayout.setRefreshComplete();

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_refresh:
                sendRequest();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshDetails(JSONObject bioNode, JSONObject pointsNode,
                                JSONArray recentsArray) throws NullPointerException,
            NotFoundException {
        if (bioNode != null) {
            TextView tv = null;
            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_name_textView);
                tv.setText(bioNode.getString(mActivity.getResources().getString(
                        R.string.profile_name)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity.findViewById(R.id.profile_moto_textView);
                tv.setText(bioNode.getString(mActivity.getResources().getString(
                        R.string.profile_moto)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity.findViewById(R.id.profile_loc_textView);
                tv.setText(bioNode.getString(mActivity.getResources().getString(
                        R.string.profile_loc)));
            } catch (JSONException e) {

            } catch (NotFoundException e) {
            }

            try {
                NetworkImageView iv = (NetworkImageView) mActivity
                        .findViewById(R.id.profile_pic_imageview);
                iv.setImageUrl(
                        bioNode.getString(mActivity.getResources().getString(
                                R.string.profile_pic)), Helper.getImageLoader()
                );
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }
        }

        if (pointsNode != null) {
            TextView tv = null;
            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_points_textView);
                tv.setText(Helper.getRepString(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_balance))));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_total_points_textView);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_total_points)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_total_savings_textView);
                {
                    String temp = pointsNode.getString(mActivity.getResources()
                            .getString(R.string.profile_total_savings));
                    tv.setText(temp);
                }
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }
            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_connections_textview);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_connections)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_checkins_textview);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_checkins)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_following_textview);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_following)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_total_coupons_textView);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_coupons)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }

            try {
                tv = (TextView) mActivity
                        .findViewById(R.id.profile_total_vouchers_textView);
                tv.setText(pointsNode.getString(mActivity.getResources().getString(
                        R.string.profile_vouchers)));
            } catch (JSONException e) {
            } catch (NotFoundException e) {
            }
        }

        if (recentsArray != null) {
            LinearLayout l = (LinearLayout) mActivity
                    .findViewById(R.id.profile_recents);
            l.removeViews(1, l.getChildCount() - 1);

            for (int i = 0; i < recentsArray.length(); i++) {
                View v = mActivity.getLayoutInflater().inflate(
                        R.layout.profile_recent_details, null);
                TextView tv = (TextView) v
                        .findViewById(R.id.profile_recent_transactions_item_text_view);
                try {
                    String details = recentsArray.optJSONObject(i).getString(
                            mActivity.getResources().getString(
                                    R.string.profile_recent_detail)
                    );
                    tv.setText(details);
                    ActivityType recentType = ActivityType.getType(recentsArray
                            .optJSONObject(i).getInt(
                                    mActivity.getResources().getString(
                                            R.string.profile_activity_type)
                            ));

                    switch (recentType) {
                        case Transaction:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_transaction, 0);
                            break;
                        case Share_Points:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_sendpoint, 0);
                            break;
                        case Receieve_Points:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_receivepoint, 0);
                            break;
                        case Check_in:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_checkin, 0);
                            break;
                        case Buy_Coupon:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.coupon_withpadding, 0);
                            break;
                        case Buy_Voucher:
                            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.voucher_withpadding, 0);
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                l.addView(v);
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!profileRequest.isCanceled())
            profileRequest.cancel();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), MyDiscountItemsActivity.class);
        switch (v.getId()) {
            case R.id.profile_coupons_viewall_button:
                intent.putExtra(Helper.KEY_TYPE,
                        Helper.DiscountItemType.Coupons.getValue());
                break;
            case R.id.profile_vouchers_viewall_button:
                intent.putExtra(Helper.KEY_TYPE,
                        Helper.DiscountItemType.Vouchers.getValue());
                break;
        }
        startActivity(intent);
    }
}
