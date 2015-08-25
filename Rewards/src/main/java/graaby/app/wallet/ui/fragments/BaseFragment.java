package graaby.app.wallet.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    protected final CompositeSubscription mCompositeSubscriptions;
    protected SwipeRefreshLayout mSwipeRefresh;

    public BaseFragment() {
        mCompositeSubscriptions = new CompositeSubscription();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInjections();
    }

    protected void setSwipeRefreshColors(int... colors) {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setColorSchemeResources(colors);
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, int layoutResource) {
        super.onCreateView(inflater, container, savedInstanceState);
        View inflatedView = inflater.inflate(layoutResource, null);
        mSwipeRefresh = (SwipeRefreshLayout) inflatedView.findViewById(R.id.swiperefresh);
        if (mSwipeRefresh != null)
            mSwipeRefresh.setOnRefreshListener(this);
        return inflatedView;
    }

    protected abstract void sendRequest();

    @Override
    public void onRefresh() {
        sendRequest();
    }

    @Override
    public void onDetach() {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        mCompositeSubscriptions.unsubscribe();
    }

    protected void setToolbarColors(int toolbarColorResourceID, int statusBarColorResourceID) {
        EventBus.getDefault().postSticky(new ToolbarEvents(toolbarColorResourceID, statusBarColorResourceID));
    }

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    abstract void setupInjections();
}
