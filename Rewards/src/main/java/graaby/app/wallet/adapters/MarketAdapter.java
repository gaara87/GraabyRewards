package graaby.app.wallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

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


public class MarketAdapter extends ArrayAdapter<DiscountItemDetailsResponse> {

    private final Context mContext;
    private LayoutInflater inflater;
    private Boolean myDiscountItems = Boolean.FALSE;
    private String rupeeSymbol;

    public MarketAdapter(Context context, Boolean areTheseMyDiscountItems) {
        super(context, R.layout.item_grid_market,
                R.id.discount_item_discountValue);
        mContext = context;
        rupeeSymbol = context.getString(R.string.Rs);
        inflater = LayoutInflater.from(context);
        myDiscountItems = areTheseMyDiscountItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.item_grid_market, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        DiscountItemDetailsResponse item = getItem(position);

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
                finalDiscountValue = mContext.getString(R.string.market_item_punch_value);
                break;
        }
        holder.mDiscountItemDiscountValue.setCompoundDrawablesWithIntrinsicBounds(
                discountItemResourceId, 0, 0, 0);
        holder.mDiscountItemDiscountValue.setText(finalDiscountValue);
        holder.mDiscountItemBusinessNameTextView.setText(item.businessName);

        if (!myDiscountItems) {
            holder.mDiscountItemCost.setText(item.costOfDI);

            holder.mDiscountItemCount.setText(String.format(
                    mContext.getString(R.string.market_items_left),
                    item.leftOverCount));
        } else {
            holder.mDiscountItemCostContainer.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
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

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
