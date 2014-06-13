package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by gaara on 1/6/14.
 */
public class ContactsFragment extends ListFragment implements Response.Listener<JSONObject>,
        Response.ErrorListener, View.OnClickListener, DialogInterface.OnClickListener {

    private Activity mActivity;
    private ContactsAdapter mAdapter;
    private List<JSONObject> contactList = new ArrayList<JSONObject>();
    private CustomRequest contactRequest;
    private AlertDialog sendPointsDialog;
    private int pointsToSend;
    private Integer contactIDToSendPointsTo = -1;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        View tempView = inflater.inflate(R.layout.dialog_send_points, null);
        sendPointsDialog = new AlertDialog.Builder(mActivity).setTitle("Share points")
                .setView(tempView).setPositiveButton("Send", this)
                .setNegativeButton("Cancel", null)
                .setCancelable(Boolean.TRUE).create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, null);
        mAdapter = new ContactsAdapter(mActivity, contactList, this);
        setListAdapter(mAdapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This is the View which is created by ListFragment
        ViewGroup viewGroup = (ViewGroup) view;

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        sendRequest();
                    }
                })
                .options(Options.create()
                        .scrollDistance(.5f).build())
                .setup(mPullToRefreshLayout);
        sendRequest();

    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("last_updated_tstamp", 123456778);
        try {
            contactRequest = new CustomRequest("contacts", params, this, this);
            Helper.getRQ().add(contactRequest);
            JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(Helper.getRQ().getCache().get(contactRequest.getCacheKey()));
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
        Assert.assertNotNull(response);

        try {
            JSONArray contacts = response.getJSONArray(mActivity
                    .getResources().getString(R.string.contact_users));
            contactList.clear();
            for (int i = 0; i < contacts.length(); i++) {
                contactList.add(contacts.optJSONObject(i));
            }
            mAdapter.notifyDataSetChanged();
        } catch (Resources.NotFoundException e) {
        } catch (JSONException e) {
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
        contactList.clear();

        try {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(contactRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            mPullToRefreshLayout.setRefreshComplete();

        }
    }


    @Override
    public void onClick(View v) {
        contactIDToSendPointsTo = (Integer) v.getTag();
        sendPointsDialog.show();

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        EditText text = (EditText) sendPointsDialog.findViewById(R.id.editText_send_points);
        pointsToSend = Integer.parseInt(text.getText().toString());
        HashMap<String, Object> params = new HashMap<String, Object>();

        try {
            params.put(getString(R.string.contact_send_to), contactIDToSendPointsTo);
            params.put(getString(R.string.contact_send_amount), pointsToSend);
            Helper.getRQ().add(new CustomRequest("contact/send", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        int responseValue = jsonObject.getInt(getString(R.string.response_success));
                        if (responseValue == getResources().getInteger(R.integer.response_success)) {
                            Toast.makeText(getActivity(), "Your points were successfully shared", Toast.LENGTH_LONG).show();
                        } else if (responseValue == getResources().getInteger(R.integer.response_failure)) {
                            String errorMsg = jsonObject.getString(getString(R.string.response_msg));
                            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Helper.handleVolleyError(volleyError, mActivity);
                }
            }
            ));
        } catch (Resources.NotFoundException e1) {
        } catch (JSONException e1) {
        }
    }

    private class ContactsAdapter extends ArrayAdapter<JSONObject> {

        private LayoutInflater inflater;
        private String contactNameField, contactIDField;
        private View.OnClickListener listener;

        public ContactsAdapter(Activity activity, List<JSONObject> contacts, View.OnClickListener l) {
            super(activity, R.layout.fragment_contacts, R.layout.item_list_contacts, contacts);
            inflater = LayoutInflater.from(getContext());
            contactNameField = getContext().getResources().getString(
                    R.string.contact_name);
            contactIDField = getContext().getResources().getString(
                    R.string.contact_id);
            listener = l;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_list_contacts, null);
            JSONObject node = getItem(position);

            TextView tv;
            {
                tv = (TextView) convertView.findViewById(R.id.contacts_usertextView);
                try {
                    tv.setText(node.getString(contactNameField));
                } catch (JSONException e) {
                }
            }

            ImageView iv = (ImageView) convertView.findViewById(R.id.contacts_userProfilePicImageView);
            try {
                Helper.getImageLoader().get(node.getString(getString(
                        R.string.pic_url)), ImageLoader.getImageListener(iv, R.drawable.ic_connections, R.drawable.ic_connections));
            } catch (Resources.NotFoundException e) {
            } catch (JSONException e) {
            }

            Button sendPointsButton = (Button) convertView.findViewById(R.id.contacts_btn_sendPoints);
            sendPointsButton.setOnClickListener(listener);
            try {
                sendPointsButton.setTag(node.getInt(contactIDField));
            } catch (JSONException e) {
            }

            return convertView;
        }
    }
}
