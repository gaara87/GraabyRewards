package graaby.app.wallet.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.OutletDetail;

public class SearchResultsAdapter extends ArrayAdapter<OutletDetail> {

    private final LayoutInflater inflater;

    public SearchResultsAdapter(Context context) {
        super(context, R.layout.item_list_search);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_search, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        OutletDetail place = getItem(position);

        holder.businessName.setText(place.businessName);
        if (TextUtils.isEmpty(place.areaName)) {
            holder.businessAddress.setVisibility(View.GONE);
        } else
            holder.businessAddress.setText(place.areaName);
        convertView.setTag(holder);
        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_list_business.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.item_businessNameTextView)
        TextView businessName;
        @Bind(R.id.item_businessAddressTextView)
        TextView businessAddress;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
