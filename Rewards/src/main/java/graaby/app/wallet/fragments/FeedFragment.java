package graaby.app.wallet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.adapter.FeedsAdapter;
import graaby.app.wallet.models.retrofit.FeedsResponse;
import graaby.app.wallet.network.services.FeedService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.EndlessScrollListener;
import graaby.app.wallet.util.Helper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FeedFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(android.R.id.list)
    ListView mList;
    @Inject
    FeedService mFeedService;
    private FeedsAdapter mAdapter;
    private int mCurrentPage = 0;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_feeds);
        ButterKnife.inject(this, v);
        mSwipeRefresh.setColorSchemeResources(R.color.alizarin, R.color.pomegranate, R.color.wisteria, R.color.peterriver);
        mAdapter = new FeedsAdapter(getActivity());
        mList.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList.setOnScrollListener(new EndlessScrollListener(0, -1) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("Load More", "Page : " + page + "ItemCount : " + totalItemsCount);
                mCurrentPage = page;
                sendRequest();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarColors(R.color.pomegranate, R.color.pomegranate_dark);
    }

    @Override
    protected void sendRequest() {
        mFeedService.getUserFeeds(mCurrentPage, Helper.PAGE_SIZE)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<FeedsResponse>(getActivity()) {
                    @Override
                    public void onFail(Throwable e) {
                        mSwipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onSuccess(FeedsResponse result) {
                        mSwipeRefresh.setRefreshing(false);
                        if (mCurrentPage == 0)
                            mAdapter.clear();

                        if (result.feedsList.size() != 0) {
                            mCurrentPage++;
                            mAdapter.addAll(result.feedsList);
                            mAdapter.notifyDataSetChanged();
                        }

                    }
                });
    }

    @Override
    public void onRefresh() {
        mCurrentPage = 0;
        sendRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
