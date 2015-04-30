package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.DiscountItemDetailsActivity;
import graaby.app.wallet.activities.SearchResultsActivity;
import graaby.app.wallet.adapter.MarketAdapter;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;
import graaby.app.wallet.models.retrofit.MarketForBusinessRequest;
import graaby.app.wallet.models.retrofit.MarketRequest;
import graaby.app.wallet.models.retrofit.MarketResponse;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.DiscountItemType;
import graaby.app.wallet.util.EndlessScrollListener;
import graaby.app.wallet.util.Helper;
import graaby.app.wallet.widgets.MultiSwipeRefreshLayout;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MarketFragment extends BaseFragment implements OnItemClickListener {

    public final static String SEARCHABLE_PARAMETER = "searchable";
    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(android.R.id.empty)
    TextView mEmpty;
    @Inject
    MarketService mMarketService;
    private DiscountItemType whatType;
    private MarketAdapter adapter;
    private Boolean areTheseMyDiscountItems;
    private Activity mActivity;
    private Integer mBrandID = Helper.DEFAULT_NON_BRAND_RELATED;
    private int mCurrentPage;

    public static MarketFragment newInstance(Boolean myFlag, int brandIDNum, Boolean searchable) {
        MarketFragment fragment = new MarketFragment();
        Bundle args = new Bundle();
        args.putInt(Helper.BRAND_ID_BUNDLE_KEY, brandIDNum);
        args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, myFlag);
        args.putBoolean(SEARCHABLE_PARAMETER, searchable);
        fragment.setArguments(args);
        return fragment;
    }

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
            if (getArguments().get(
                    Helper.KEY_TYPE) != null)
                whatType = (DiscountItemType) getArguments().get(Helper.KEY_TYPE);
            areTheseMyDiscountItems = getArguments().getBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.FALSE);
            mBrandID = getArguments().getInt(Helper.BRAND_ID_BUNDLE_KEY, Helper.DEFAULT_NON_BRAND_RELATED);
        }
        adapter = new MarketAdapter(mActivity, areTheseMyDiscountItems);
        if (getArguments().getBoolean(SEARCHABLE_PARAMETER, Boolean.FALSE)) {
            this.setHasOptionsMenu(Boolean.TRUE);
        }
        sendRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (areTheseMyDiscountItems) {
            switch (whatType) {
                case COUPONS:
                    setToolbarColors(R.color.peterriver, R.color.belizehole);
                    break;
                case VOUCHERS:
                    setToolbarColors(R.color.sunflower, R.color.sunflower_darker);
                    break;
            }
        } else {
            if (mBrandID == Helper.DEFAULT_NON_BRAND_RELATED)
                setToolbarColors(R.color.sunflower, R.color.sunflower_darker);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_marketplace);
        ButterKnife.inject(this, v);
        mSwipeRefresh.setColorSchemeResources(R.color.sunflower, R.color.nephritis, R.color.peterriver, R.color.pumpkin);
        mGrid.setOnItemClickListener(this);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MultiSwipeRefreshLayout) mSwipeRefresh).setSwipeableChildren(R.id.grid, android.R.id.empty);
        mGrid.setAdapter(adapter);
        mGrid.setOnScrollListener(new EndlessScrollListener(0, -1) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("Load More", "Page : " + page + "ItemCount : " + totalItemsCount);
                mCurrentPage = page;
                sendRequest();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (areTheseMyDiscountItems.equals(Boolean.FALSE)) {
            inflater.inflate(R.menu.menu_fragment_market, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(mActivity, SearchResultsActivity.class);
                intent.putExtra(Helper.KEY_TYPE, SearchResultsActivity.SEARCH_COLLAPSE);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        mActivity = null;
    }

    @Override
    protected void sendRequest() {
        MarketRequest request = new MarketRequest();
        if (mBrandID != Helper.DEFAULT_NON_BRAND_RELATED) {
            request = new MarketForBusinessRequest(mBrandID);
        }
        request.page = mCurrentPage;
        //TODO: convert PAGE_SIZE to configuration parameter from GTM
        request.size = Helper.PAGE_SIZE;

        Observable<MarketResponse> tempObs = mMarketService.getMarketDiscountItems(request);
        if (areTheseMyDiscountItems) {
            // viewing user coupons or vouchers
            switch (whatType) {
                case COUPONS:
                    tempObs = mMarketService.getUserCoupons(request);
                    break;
                case VOUCHERS:
                    tempObs = mMarketService.getUserVouchers(request);
                    break;
            }
        }
        Subscription subscriber = tempObs.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<MarketResponse>(getActivity(), mSwipeRefresh) {
                    @Override
                    public void onSuccess(MarketResponse result) {
                        if (result.items.size() > 0) {
                            if (mCurrentPage++ == 0) {
                                adapter.clear();
                            }
                            adapter.addAll(result.items);
                        } else if (mCurrentPage == 0) {
                            mGrid.setEmptyView(mEmpty);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        mCompositeSubscriptions.add(subscriber);
        Log.d("Loading", "Page - " + mCurrentPage);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        try {
            DiscountItemDetailsResponse marketDiscountItem = adapter.getItem(i);
            intent.putExtra(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(marketDiscountItem));

            if (whatType != null)
                intent.putExtra(Helper.KEY_TYPE, whatType);
            intent.putExtra(Helper.MY_DISCOUNT_ITEMS_FLAG, areTheseMyDiscountItems);
            intent.setClass(mActivity, DiscountItemDetailsActivity.class);
            startActivityForResult(intent, 66);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 66 && resultCode == Activity.RESULT_OK) {
            sendRequest();
        }
    }
}
