package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.Helper.DiscountItemType;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.DiscountItemDetailsActivity;
import graaby.app.wallet.activities.MyDiscountItemsActivity;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MarketFragment extends Fragment implements OnItemClickListener,
        com.android.volley.Response.Listener<JSONObject>, ErrorListener {

    private GridView marketGrid;

    private DiscountItemType whatType;
    private CustomRequest marketRequest;

    private List<JSONObject> discountItemList = new ArrayList<JSONObject>();
    private MarketAdapter adapter;

    private Boolean areTheseMyDiscountItems;
    private Activity mActivity;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;

        if (activity.getClass() == MainActivity.class)
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(Helper.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            whatType = DiscountItemType.getType(getArguments().getInt(
                    Helper.KEY_TYPE));
            areTheseMyDiscountItems = getArguments().getBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG);
        }

        adapter = new MarketAdapter(mActivity, discountItemList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_marketplace, null);
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

        marketGrid = (GridView) v.findViewById(R.id.discountItemsGridView);
        marketGrid.setOnItemClickListener(this);
        marketGrid.setAdapter(adapter);
        sendRequest();
        return v;
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        String specificURL = "";
        if (areTheseMyDiscountItems) {
            // viewing user coupons or vouchers
            if (whatType != null) {
                switch (whatType) {
                    case Coupons:
                        specificURL += "/mine/c";
                        break;
                    case Vouchers:
                        specificURL += "/mine/v";
                        break;
                }
            }
        }

        try {
            marketRequest = new CustomRequest("market" + specificURL, params,
                    this, this);
            marketRequest.setShouldCache(areTheseMyDiscountItems);
            Helper.getRQ().add(marketRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            } catch (NullPointerException npe) {

            }
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        JSONArray discountItemsArray = new JSONArray();
        try {
            discountItemsArray = response.getJSONArray(getString(R.string.market_items));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        discountItemList.clear();
        for (int i = 0; i < discountItemsArray.length(); i++) {
            discountItemList.add(discountItemsArray.optJSONObject(i));
        }
        adapter.notifyDataSetChanged();
        try {
            mPullToRefreshLayout.setRefreshComplete();
        } catch (NullPointerException npe) {
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, mActivity);

        try {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(marketRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            try {
                mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            } catch (NullPointerException npe) {

            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        JSONObject node = adapter.getItem(i);
        intent.putExtra(Helper.INTENT_CONTAINER_INFO, node.toString());
        if (whatType != null)
            intent.putExtra(Helper.KEY_TYPE, whatType.getValue());
        intent.putExtra(Helper.MY_DISCOUNT_ITEMS_FLAG, areTheseMyDiscountItems);
        intent.setClass(mActivity, DiscountItemDetailsActivity.class);
        startActivity(intent);
    }

    private class MarketAdapter extends ArrayAdapter<JSONObject> {

        private LayoutInflater inflater;
        private String marketValueField;
        private String marketBusinessNameField;
        private String rupeeSymbol;
        private String cost;
        private String type;
        private Boolean myDiscountItems = Boolean.FALSE;

        public MarketAdapter(Context context, List<JSONObject> discountItems) {
            super(context, R.layout.item_grid_market,
                    R.id.discount_item_discountValue, discountItems);
            inflater = LayoutInflater.from(context);
            marketValueField = getString(
                    R.string.market_value);
            marketBusinessNameField = getString(
                    R.string.business_name);
            rupeeSymbol = getString(R.string.Rs);
            type = getString(R.string.market_item_type);
            cost = getString(R.string.market_cost);
            if (context.getClass() == MyDiscountItemsActivity.class) {
                myDiscountItems = Boolean.TRUE;
            }

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_grid_market, null);
            JSONObject node = getItem(position);
            try {

                TextView tv;
                {
                    tv = (TextView) convertView
                            .findViewById(R.id.discount_item_discountValue);
                    int discountItemResourceId = -1;
                    String incomingDiscountItemType = node.getString(type);
                    String finalDiscountValue = "";
                    if (incomingDiscountItemType.equals(getString(R.string.market_item_type_coupon))) {
                        discountItemResourceId = R.drawable.coupon_nopadding;
                        finalDiscountValue = rupeeSymbol + " " + node.getString(marketValueField);
                    } else if (incomingDiscountItemType.equals(getString(R.string.market_item_type_voucher))) {
                        discountItemResourceId = R.drawable.voucher_nopadding;
                        finalDiscountValue = rupeeSymbol + " " + node.getString(marketValueField);
                    } else if (incomingDiscountItemType.equals(getString(R.string.market_item_type_punch))) {
                        discountItemResourceId = R.drawable.punch_nopadding;
                        finalDiscountValue = mActivity.getString(R.string.market_item_punch_value);
                    }
                    tv.setCompoundDrawablesWithIntrinsicBounds(
                            discountItemResourceId, 0, 0, 0);
                    tv.setText(finalDiscountValue);
                }


                {
                    tv = (TextView) convertView
                            .findViewById(R.id.discount_item_business_name_textView);
                    tv.setText(node.getString(marketBusinessNameField));
                }

                {
                    tv = (TextView) convertView
                            .findViewById(R.id.discount_item_cost);
                    if (!myDiscountItems) {
                        tv.setText(tv.getText() + " " + node.getString(cost));
                    } else {
                        tv.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

    }
}
