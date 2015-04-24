package graaby.app.wallet.util;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import rx.Subscriber;

/**
 * @param <T> the data entity type that we're interested in
 */
public abstract class CacheSubscriber<T> extends Subscriber<T> {
    private WeakReference<Context> mContext;

    public CacheSubscriber(@NotNull final Context context) {
        this.mContext = new WeakReference<>(context);
    }

    @Override
    final public void onCompleted() {
        Log.d(CacheSubscriber.class.toString(), "Completed");
    }

    @Override
    final public void onError(Throwable e) {
        Crashlytics.logException(e);
        if (mContext.get() != null) {
            onFail(e);
        }
    }

    @Override
    final public void onNext(T result) {
        if (mContext.get() != null) {
            onSuccess(result);
        }
    }

    /**
     * Implement an on error handler.
     *
     * @param e an exception that was thrown
     */
    public abstract void onFail(Throwable e);

    /**
     * Implement a success handler.
     *
     * @param result the response object
     */
    public abstract void onSuccess(T result);
}
