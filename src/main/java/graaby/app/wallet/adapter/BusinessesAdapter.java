package graaby.app.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import graaby.app.wallet.R;

public class BusinessesAdapter extends ArrayAdapter<JSONObject> {

    private final LayoutInflater inflater;
    private String bName_field, bArea_field;

    public BusinessesAdapter(Context context) {
        this(context, new ArrayList<JSONObject>());
    }

    public BusinessesAdapter(Context context, List<JSONObject> places) {
        super(context, R.layout.item_list_business, places);
        bName_field = context.getString(R.string.field_business_title);
        bArea_field = context.getString(R.string.business_area);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_list_business, null);
        JSONObject place = getItem(position);

        try {
            ((TextView) convertView.findViewById(R.id.item_businessNameTextView)).setText(place.getString(bName_field));
        } catch (JSONException e) {
        }

        try {
            if (place.has(bArea_field)) {
                ((TextView) convertView.findViewById(R.id.item_businessAddressTextView)).setText(place.getString(bArea_field));
            } else {
                convertView.findViewById(R.id.item_businessAddressTextView).setVisibility(View.GONE);
            }
        } catch (JSONException e) {
        }
        return convertView;
    }
}
