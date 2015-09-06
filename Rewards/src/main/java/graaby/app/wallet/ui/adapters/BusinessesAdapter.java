package graaby.app.wallet.ui.adapters;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.OutletDetail;

/**
 * Created by Akash.
 */
public class BusinessesAdapter extends RecyclerView.Adapter<BusinessesAdapter.ViewHolder> {
    ArrayList<OutletDetail> outlets = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_search, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OutletDetail outlet = outlets.get(position);
        holder.itemBusinessNameTextView.setText(outlet.businessName);
        holder.itemBusinessAddressTextView.setText(outlet.areaName);
    }

    @Override
    public int getItemCount() {
        return outlets.size();
    }

    public void addOutlets(OutletDetail outlet) {
        outlets.add(outlet);
        notifyItemInserted(outlets.size() - 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_businessNameTextView)
        AppCompatTextView itemBusinessNameTextView;
        @Bind(R.id.item_businessAddressTextView)
        AppCompatTextView itemBusinessAddressTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
