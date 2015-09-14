package graaby.app.taginitializer.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.taginitializer.R;
import graaby.app.taginitializer.TagWriterActivity;
import graaby.app.taginitializer.VolleySingletonRequestQueue;

/**
 * A fragment representing a single GraabyTag detail screen.
 * This fragment is either contained in a {@link graaby.app.taginitializer.GraabyTagListActivity}
 * in two-pane mode (on tablets) or a {@link graaby.app.taginitializer.GraabyTagDetailActivity}
 * on handsets.
 */
public class GraabyTagDetailFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String NFC_PARCEL = "nfc_parcel";
    @InjectView(R.id.status)
    TextView mTvStatus;
    @InjectView(R.id.id)
    TextView mTvId;
    @InjectView(R.id.key)
    TextView mTvKey;
    @InjectView(R.id.name)
    TextView mTvName;
    @InjectView(R.id.expiry)
    TextView mTvExpiry;
    @InjectView(R.id.gender)
    TextView mTvGender;
    @InjectView(R.id.token)
    TextView mTvToken;
    @InjectView(R.id.outlet)
    TextView mTvOutlet;
    /**
     * The dummy content this fragment is presenting.
     */
    private Long mItem;
    private Intent intent;

    private Intent nfcIntent;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GraabyTagDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = getArguments().getLong(ARG_ITEM_ID);
        } else {
            nfcIntent = getArguments().getParcelable(NFC_PARCEL);
        }
        intent = new Intent(getActivity(), TagWriterActivity.class);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graabytag_detail, container, false);
        ButterKnife.inject(this, rootView);
        if (mItem != null)
            mTvId.setText(mItem.toString());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JSONObject postParameters = new JSONObject();
        if (mItem != null) {
            try {
                postParameters.put(getString(R.string.field_card), mItem.longValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (nfcIntent != null) {
            GraabyTag parsedTag = GraabyTag.parseNDEFInfo(getActivity(), (Tag) nfcIntent
                    .getParcelableExtra(NfcAdapter.EXTRA_TAG), nfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
            if (parsedTag != null) {
                try {
                    postParameters.put(getString(R.string.field_card), parsedTag.getGraabyId().longValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mTvId.setText(parsedTag.getGraabyId().toString());
                mTvKey.setText("Key available and hidden");
                mTvName.setText(parsedTag.getGraabyUserName());
                mTvExpiry.setText(parsedTag.getExpiryDate());
                mTvGender.setText(parsedTag.isMale() ? "m" : "f");
            }
        }
        JsonObjectRequest request = new JsonObjectRequest(getString(R.string.url) + getString(R.string.api_get_tag_info), postParameters, this, this);
        VolleySingletonRequestQueue.getInstance(getActivity()).addToRequestQueue(request);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (intent.hasExtra("json"))
            inflater.inflate(R.menu.write, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_write:
                getActivity().startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            mTvName.setText(response.getString(getString(R.string.tag_field_name)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mTvKey.setText(response.getString(getString(R.string.field_key)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mTvGender.setText(response.getBoolean(getString(R.string.tag_field_gender)) ? "m" : "f");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            mTvExpiry.setText(format.format(new Date(response.getLong(getString(R.string.tag_field_expiry)))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mTvToken.setText(response.getString(getString(R.string.field_token)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {

            mTvOutlet.setText(String.format(getString(R.string.outlet_id_string),
                    response.getString(getString(R.string.field_agent_id)),
                    response.getString(getString(R.string.field_agent_name))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mTvStatus.setText(response.getString(getString(R.string.field_state)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getActivity().invalidateOptionsMenu();
        intent.putExtra("json", response.toString());
    }
}
