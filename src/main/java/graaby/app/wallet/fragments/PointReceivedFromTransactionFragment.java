package graaby.app.wallet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

/**
 * Created by gaara on 9/1/14.
 */
public class PointReceivedFromTransactionFragment extends Fragment implements RatingBar.OnRatingBarChangeListener {
    private String mTimeStamp;
    private JSONObject pointNode;
    private ActionBarActivity mActivity;
    private int mActivityID, mAmount;
    private String mBusinessName;

    public PointReceivedFromTransactionFragment() {

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ActionBarActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String info = getArguments().getString(
                Helper.INTENT_CONTAINER_INFO);
        try {
            pointNode = new JSONObject(info);
            mBusinessName = pointNode.getString(getString(R.string.field_business_name));
            mAmount = pointNode.getInt(mActivity.getString(R.string.contact_send_amount));
            mTimeStamp = pointNode.getString(mActivity.getString(R.string.timestamp));
            mActivityID = pointNode.getInt(mActivity.getString(R.string.field_activity_id));
        } catch (JSONException e) {
//            mActivity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_point_received_from_tx, container, false);
        rootView.findViewById(R.id.swiperefresh).setEnabled(Boolean.FALSE);

        TextView tv = (TextView) rootView.findViewById(R.id.points_textView);
        tv.setText(String.valueOf(mAmount));
        tv = (TextView) rootView.findViewById(R.id.name);
        tv.setText(mBusinessName);
        tv = (TextView) rootView.findViewById(R.id.timestamp);
        tv.setText(mTimeStamp);

        RatingBar rating = (RatingBar) rootView.findViewById(R.id.ratingBar);
        rating.setOnRatingBarChangeListener(this);
        return rootView;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(getResources().getString(R.string.field_activity_id), mActivityID);
        try {
            Helper.getRQ().add(
                    new CustomRequest("rate", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt(mActivity.getString(R.string.response_success)) == 1) {
                                    Toast.makeText(mActivity, "Rating saved", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Helper.handleVolleyError(error, mActivity);
                        }
                    })
            );
        } catch (JSONException e) {
        }
    }
}
