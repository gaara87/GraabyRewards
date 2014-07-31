package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
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

/**
 * Created by gaara on 1/6/14.
 */
public class ContactsFragment extends ListFragment implements Response.Listener<JSONObject>,
        Response.ErrorListener, DialogInterface.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Activity mActivity;
    private ContactsAdapter mAdapter;
    private List<JSONObject> contactList = new ArrayList<JSONObject>();
    private CustomRequest contactRequest;
    private AlertDialog sendPointsDialog;
    private int pointsToSend;
    private Integer contactIDToSendPointsTo = -1;
    private SwipeRefreshLayout mPullToRefreshLayout;
    private ShareActionProvider mShareActionProvider;

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
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, null);
        mPullToRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setOnRefreshListener(this);
        mPullToRefreshLayout.setColorSchemeResources(R.color.belizehole, R.color.pomegranate, R.color.orange, R.color.peterriver);
        mAdapter = new ContactsAdapter(mActivity, contactList);
        setListAdapter(mAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_contacts, menu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_item_share));
        Intent sendIntent = getIntent();
        mShareActionProvider.setShareIntent(sendIntent);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent getIntent() {
        String text = "I see you don't have Graaby. Check it out right away. http://goo.gl/jgIIn1";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("last_updated_tstamp", 123456778);
        try {
            contactRequest = new CustomRequest("contacts", params, this, this);
            Cache.Entry entry = Helper.getRQ().getCache().get(contactRequest.getCacheKey());
            if (entry != null) {
                JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(entry);
                if (jsonCachedResponse != null) {
                    onResponse(jsonCachedResponse);
                }
            }
            Helper.getRQ().add(contactRequest);
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
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
                mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
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
            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_add_contact:
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, 1001);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            Uri uriContact = data.getData();
            /*Cursor contacts = mActivity.getContentResolver().query(uriContact, null, null, null, null);
            String email = "";
            if (contacts.moveToFirst()) {
                int nameColumn = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                email = contacts.getString(nameColumn);
                contacts.close();
            }*/

            String phone = "";
            Cursor phones = mActivity.getContentResolver().query(uriContact, null, null, null, null);
            if (phones.moveToFirst()) {
                phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phones.close();

            HashMap<String, Object> params = new HashMap<String, Object>();

            try {
                if (!TextUtils.isEmpty(phone)) {
                    String formattedPhone = PhoneNumberUtils.stripSeparators(phone);

                    int length = formattedPhone.length();
                    if (length > 10) {
                        formattedPhone = formattedPhone.substring(length - 10, length);
                    }
                    params.put(getString(R.string.contact_phone), formattedPhone);
                /*if (!TextUtils.isEmpty(email))
                    params.put(getString(R.string.contact_email), email);*/
                    Helper.getRQ().add(new CustomRequest("contact/add", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
                            try {
                                int responseCode = jsonObject.getInt(getString(R.string.response_success));
                                if (responseCode == 1) {
                                    Toast.makeText(mActivity, jsonObject.getString(getString(R.string.response_msg)), Toast.LENGTH_SHORT).show();
                                    sendRequest();
                                } else if (responseCode == 0) {
                                    Toast.makeText(mActivity, "Contact not available. Please invite them!", Toast.LENGTH_LONG).show();
                                    startActivity(Intent.createChooser(getIntent(), "Invite through.."));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Helper.handleVolleyError(volleyError, mActivity);
                            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
                        }
                    }
                    ));
                    mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
                } else {
                    Toast.makeText(mActivity, "Contact info not available. Please invite them!", Toast.LENGTH_LONG).show();
                    startActivity(Intent.createChooser(getIntent(), "Invite through.."));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }

    private class ContactsAdapter extends ArrayAdapter<JSONObject> {

        private LayoutInflater inflater;
        private String contactNameField, contactIDField;

        public ContactsAdapter(Activity activity, List<JSONObject> contacts) {
            super(activity, R.layout.fragment_contacts, R.layout.item_list_contacts, contacts);
            inflater = LayoutInflater.from(getContext());
            contactNameField = getContext().getResources().getString(
                    R.string.contact_name);
            contactIDField = getContext().getResources().getString(
                    R.string.contact_id);

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

            try {
                convertView.setTag(node.getInt(contactIDField));
            } catch (JSONException e) {
            }

            return convertView;
        }
    }
}
