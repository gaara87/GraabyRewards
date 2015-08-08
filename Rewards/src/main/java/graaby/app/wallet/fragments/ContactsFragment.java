package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.AddContactRequest;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ContactsResponse;
import graaby.app.wallet.models.retrofit.SendPointsRequest;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;

/**
 * Created by gaara on 1/6/14.
 */
public class ContactsFragment extends BaseFragment implements DialogInterface.OnClickListener, View.OnClickListener {

    @Bind(R.id.grid)
    GridView mGrid;
    @Inject
    ContactService mContactService;
    private ContactsAdapter mAdapter;
    private AlertDialog sendPointsDialog;
    private Integer contactIDToSendPointsTo = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        View tempView = inflater.inflate(R.layout.dialog_send_points, null);
        sendPointsDialog = new AlertDialog.Builder(getActivity()).setTitle("Share points")
                .setView(tempView).setPositiveButton("Send", this)
                .setNegativeButton("Cancel", null)
                .setCancelable(Boolean.TRUE).create();
        this.setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_contacts);
        ButterKnife.bind(this, v);
        mAdapter = new ContactsAdapter(getActivity());
        mSwipeRefresh.setColorSchemeResources(R.color.belizehole, R.color.pomegranate, R.color.orange, R.color.peterriver);
        mGrid.setAdapter(mAdapter);
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
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_item_share));
        Intent sendIntent = getIntent();
        mShareActionProvider.setShareIntent(sendIntent);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarColors(R.color.belizehole, R.color.belizehole_darker);
    }

    private Intent getIntent() {
        String text = "I see you don't have Graaby. Check it out right away. http://goo.gl/jgIIn1";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    @Override
    protected void sendRequest() {
        mCompositeSubscriptions.add(mContactService.getUserContacts()
                .compose(this.<ContactsResponse>applySchedulers())
                .subscribe(new CacheSubscriber<ContactsResponse>(getActivity(), mSwipeRefresh) {
                    @Override
                    public void onSuccess(ContactsResponse result) {
                        mAdapter.clear();
                        mAdapter.addAll(result.userContacts);
                        mAdapter.notifyDataSetChanged();
                    }
                }));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        EditText text = (EditText) sendPointsDialog.findViewById(R.id.editText_send_points);
        int pointsToSend;
        try {
            pointsToSend = Integer.parseInt(text.getText().toString());
        } catch (Exception e) {
            pointsToSend = 0;
        }
        if (pointsToSend == 0)
            return;

        mCompositeSubscriptions.add(
                mContactService.sendPointsToUser(new SendPointsRequest(contactIDToSendPointsTo, pointsToSend))
                        .compose(this.<BaseResponse>applySchedulers())
                        .subscribe(new CacheSubscriber<BaseResponse>(getActivity(), mSwipeRefresh) {
                            @Override
                            public void onSuccess(BaseResponse result) {
                                if (result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                                    Toast.makeText(getActivity(), "Your points were successfully shared", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), result.message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }));
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            Uri uriContact = data.getData();
            /*Cursor contacts = getActivity().getContentResolver().query(uriContact, null, null, null, null);
            String email = "";
            if (contacts.moveToFirst()) {
                int nameColumn = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                email = contacts.getString(nameColumn);
                contacts.close();
            }*/

            String phone = "";
            Cursor phones = getActivity().getContentResolver().query(uriContact, null, null, null, null);
            if (phones.moveToFirst()) {
                phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phones.close();

            if (!TextUtils.isEmpty(phone)) {
                String formattedPhone = PhoneNumberUtils.stripSeparators(phone);

                int length = formattedPhone.length();
                if (length > 10) {
                    formattedPhone = formattedPhone.substring(length - 10, length);
                }
                mCompositeSubscriptions.add(
                        mContactService.addContact(new AddContactRequest(formattedPhone))
                                .compose(this.<BaseResponse>applySchedulers())
                                .subscribe(new CacheSubscriber<BaseResponse>(getActivity(), mSwipeRefresh) {
                                    @Override
                                    public void onSuccess(BaseResponse result) {
                                        if (result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                                            Toast.makeText(getActivity(), result.message, Toast.LENGTH_SHORT).show();
                                            sendRequest();
                                        } else {
                                            Toast.makeText(getActivity(), result.message, Toast.LENGTH_LONG).show();
                                            startActivity(Intent.createChooser(getIntent(), getActivity().getString(R.string.contact_invite_using)));
                                        }
                                    }
                                }));
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.contact_info_na), Toast.LENGTH_LONG).show();
                startActivity(Intent.createChooser(getIntent(), getActivity().getString(R.string.contact_invite_using)));
            }

        }
    }

    @Override
    public void onClick(View v) {
        contactIDToSendPointsTo = (Integer) v.getTag();
        sendPointsDialog.show();
    }

    static class ViewHolder {
        @Bind(R.id.contacts_userProfilePicImageView)
        ImageView contactPic;
        @Bind(R.id.contacts_usertextView)
        TextView contactName;
        @Bind(R.id.contacts_points)
        TextView contactPoints;
        @Bind(R.id.share_points)
        Button sharePoints;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class ContactsAdapter extends ArrayAdapter<ContactsResponse.ContactDetail> {

        private LayoutInflater inflater;

        public ContactsAdapter(Activity activity) {
            super(activity, R.layout.fragment_contacts);
            inflater = LayoutInflater.from(activity);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = inflater.inflate(R.layout.item_grid_contacts, null, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            ContactsResponse.ContactDetail node = getItem(position);

            holder.contactName.setText(node.contactName);

            Glide.with(getContext())
                    .load(node.pictureURL)
                    .placeholder(R.drawable.ic_connections)
                    .into(holder.contactPic);

            holder.contactPoints.setText(node.graabyPoints + " Graaby Points");

            holder.sharePoints.setOnClickListener(ContactsFragment.this);
            holder.sharePoints.setTag(node.contactID);


            return convertView;
        }

        /**
         * This class contains all butterknife-injected Views & Layouts from layout file 'item_grid_contacts.xml'
         * for easy to all layout elements.
         *
         * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
         */
    }
}
