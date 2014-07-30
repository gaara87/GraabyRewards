package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PointReceivedFragment extends Fragment {

    private JSONObject pointNode;
    private ActionBarActivity mActivity;
    private int mActivityID, mAmount;

    public PointReceivedFragment() {
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
            mActivity.getSupportActionBar().setTitle(pointNode.getString(getString(R.string.contact_send_from)));
            mAmount = pointNode.getInt(mActivity.getString(R.string.contact_send_amount));
            mActivityID = pointNode.getInt(mActivity.getString(R.string.field_activity_id));
        } catch (JSONException e) {
//            mActivity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_point_received, container, false);
        rootView.findViewById(R.id.swiperefresh).setEnabled(Boolean.FALSE);
        rootView.findViewById(R.id.btn_thanks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(Boolean.FALSE);

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(getResources().getString(R.string.field_activity_id), mActivityID);
                try {
                    Helper.getRQ().add(
                            new CustomRequest("thank", params, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getInt(mActivity.getString(R.string.response_success)) == 1) {
                                            ((Button) view).setText("Sent");
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Helper.handleVolleyError(error, mActivity);
                                    view.setEnabled(Boolean.TRUE);
                                }
                            })
                    );
                } catch (JSONException e) {
                }
            }
        });

        ImageView iv = (ImageView) rootView.findViewById(R.id.contacts_userProfilePicImageView);
        int defaultImageResource = R.drawable.nav_profile_man;
        try {
            Helper.getImageLoader().get(pointNode.getString(getString(
                    R.string.pic_url)), ImageLoader.getImageListener(iv, defaultImageResource, defaultImageResource));
        } catch (Resources.NotFoundException e) {
            iv.setImageResource(defaultImageResource);
        } catch (JSONException e) {
            iv.setImageResource(defaultImageResource);
        }

        TextView tv = (TextView) rootView.findViewById(R.id.points_textView);
        tv.setText(String.valueOf(mAmount));

        return rootView;
    }
}
