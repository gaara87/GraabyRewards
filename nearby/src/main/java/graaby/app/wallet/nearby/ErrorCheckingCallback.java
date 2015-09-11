package graaby.app.wallet.nearby;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by Akash.
 */
public class ErrorCheckingCallback implements ResultCallback<Status> {
    public static final int REQUEST_RESOLVE_ERROR = 898;
    private static final String TAG = ErrorCheckingCallback.class.toString();
    private final String method;
    private final Runnable runOnSuccess;
    private final BooleanWrapper mResolvingError;
    private Activity mContext;

    protected ErrorCheckingCallback(Activity context, BooleanWrapper resolvingError, String method) {
        this(context, resolvingError, method, null);
    }

    protected ErrorCheckingCallback(Activity context, BooleanWrapper resolvingError, String method, @Nullable Runnable runOnSuccess) {
        this.mContext = context;
        this.method = method;
        this.runOnSuccess = runOnSuccess;
        this.mResolvingError = resolvingError;
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, method + " succeeded.");
            if (runOnSuccess != null) {
                runOnSuccess.run();
            }
        } else {
            // Currently, the only resolvable error is that the device is not opted
            // in to Nearby. Starting the resolution displays an opt-in dialog.
            if (status.hasResolution()) {
                if (!mResolvingError.isResolvingError()) {
                    try {
                        status.startResolutionForResult(mContext,
                                REQUEST_RESOLVE_ERROR);
                        mResolvingError.setResolvingError(true);
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(mContext, method + " failed with exception: " + e, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // This will be encountered on initial startup because we do
                    // both publish and subscribe together.  So having a toast while
                    // resolving dialog is in progress is confusing, so just log it.
                    Log.i(TAG, method + " failed with status: " + status
                            + " while resolving error.");
                }
            } else {
                Toast.makeText(mContext, method + " failed with : " + status
                        + " resolving error: " + mResolvingError.isResolvingError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class BooleanWrapper {
        boolean resolvingError;

        public boolean isResolvingError() {
            return resolvingError;
        }

        public void setResolvingError(boolean resolvingError) {
            this.resolvingError = resolvingError;
        }

    }

}