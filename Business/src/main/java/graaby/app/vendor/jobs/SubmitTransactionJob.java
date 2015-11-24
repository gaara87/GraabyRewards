package graaby.app.vendor.jobs;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import graaby.app.vendor.GraabyRedemptionApplication;
import graaby.app.vendor.R;
import graaby.app.vendor.volley.CustomRequest;
import graaby.app.vendor.volley.VolleySingletonRequestQueue;

/**
 * Created by gaara on 9/9/14.
 */
public class SubmitTransactionJob extends Job {
    private final HashMap<String, Object> mParams;
    private final String stringResourceId;
    private final String tabUID;

    public SubmitTransactionJob(Context context, HashMap<String, Object> params) {
        super(new Params(1).requireNetwork().persist());
        this.tabUID = CustomRequest.getTabletUID();
        this.mParams = params;
        Long millis = Calendar.getInstance().getTimeInMillis();
        if (!mParams.containsKey(context.getString(R.string.field_id))) {
            MessageDigest digester = null;
            try {
                digester = MessageDigest.getInstance("MD5");
                byte[] digest = digester.digest(millis.toString().getBytes());
                mParams.put(context.getString(R.string.field_id), android.util.Base64.encodeToString(digest, android.util.Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        if (mParams.size() == 2 && mParams.containsKey(context.getString(R.string.field_graaby_user_id))) {
            stringResourceId = context.getString(R.string.api_checkin);
        } else {
            stringResourceId = context.getString(R.string.api_resubmit_transaction);
        }

    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        try {
            RequestQueue mRequestQ = VolleySingletonRequestQueue.getInstance(GraabyRedemptionApplication.getInstance()).getJobRequestQueue();
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            CustomRequest submitTransaction = new CustomRequest(stringResourceId, mParams, future, future, tabUID);
            mRequestQ.add(submitTransaction);
            JSONObject response = future.get();
            if (response.getInt(GraabyRedemptionApplication.getInstance().getString(R.string.field_success)) == 1) {
                Log.v("TX", "Submitted");
            }
        } catch (JSONException e) {
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        if (throwable instanceof ExecutionException) {
            return true;
        } else if (throwable instanceof InterruptedException) {
            return false;
        }
        return false;
    }
}
