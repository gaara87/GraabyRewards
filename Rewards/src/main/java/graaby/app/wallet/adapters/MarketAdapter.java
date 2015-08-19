package graaby.app.wallet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;

/**
 * This class contains all butterknife-injected Views & Layouts from layout file 'item_grid_market.xml'
 * for easy to all layout elements.
 *
 * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
 */


public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.ViewHolder> {

    protected final ArrayList<DiscountItemDetailsResponse> mItems = new ArrayList<>();
    private final String punchValueString;
    private final String marketItemsLeftString;
    private final MarketItemClickListener marketItemClickListener;
    private Boolean myDiscountItems = Boolean.FALSE;
    private String rupeeSymbol;

    public MarketAdapter(Context context, boolean areTheseMyDiscountItems, MarketItemClickListener listener) {
        marketItemClickListener = listener;
        myDiscountItems = areTheseMyDiscountItems;
        rupeeSymbol = context.getString(R.string.Rs);
        punchValueString = context.getString(R.string.market_item_punch_value);
        marketItemsLeftString = context.getString(R.string.market_items_left);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid_market, viewGroup, false);
        return new ViewHolder(v, marketItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        DiscountItemDetailsResponse item = mItems.get(i);

        int discountItemResourceId = -1;
        String finalDiscountValue = "";

        switch (item.typeOfDI) {
            case COUPONS:
                discountItemResourceId = R.drawable.coupon_nopadding;
                finalDiscountValue = rupeeSymbol + " " + item.discountValue;
                break;
            case VOUCHERS:
                discountItemResourceId = R.drawable.voucher_nopadding;
                finalDiscountValue = rupeeSymbol + " " + item.discountValue;
                break;
            case PUNCH:
                discountItemResourceId = R.drawable.punch_nopadding;
                finalDiscountValue = punchValueString;
                break;
        }
        holder.mDiscountItemDiscountValue.setCompoundDrawablesWithIntrinsicBounds(
                discountItemResourceId, 0, 0, 0);
        holder.mDiscountItemDiscountValue.setText(finalDiscountValue);
        holder.mDiscountItemBusinessNameTextView.setText(item.businessName);

        if (!myDiscountItems) {
            holder.mDiscountItemCost.setText(item.costOfDI);

            holder.mDiscountItemCount.setText(String.format(
                    marketItemsLeftString,
                    item.leftOverCount));
        } else {
            holder.mDiscountItemCostContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addAll(List<DiscountItemDetailsResponse> items) {
        mItems.addAll(items);
    }

    public void clear() {
        mItems.clear();
    }

    public DiscountItemDetailsResponse getItem(int position) {
        return mItems.get(position);
    }

    public interface MarketItemClickListener {
        void onMarketItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.discount_item_discountValue)
        TextView mDiscountItemDiscountValue;
        @Bind(R.id.discount_item_business_name_textView)
        TextView mDiscountItemBusinessNameTextView;
        @Bind(R.id.discount_item_cost)
        TextView mDiscountItemCost;
        @Bind(R.id.discount_item_count)
        TextView mDiscountItemCount;
        @Bind(R.id.discount_item_cost_container)
        LinearLayout mDiscountItemCostContainer;

        ViewHolder(View view, MarketItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            if (listener != null)
                view.setOnClickListener(v -> {
                    listener.onMarketItemClick(ViewHolder.this.getAdapterPosition());
                });
        }
    }


}
