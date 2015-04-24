package graaby.app.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.OutletDetail;

public class BusinessesAdapter extends ArrayAdapter<OutletDetail> {

    private final LayoutInflater inflater;

    public BusinessesAdapter(Context context) {
        super(context, R.layout.item_list_business);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_list_business, null);
        OutletDetail place = getItem(position);

        ((TextView) convertView.findViewById(R.id.item_businessNameTextView)).setText(place.outletName);

        ((TextView) convertView.findViewById(R.id.item_businessAddressTextView)).setText(place.areaName);
        return convertView;
    }
}
