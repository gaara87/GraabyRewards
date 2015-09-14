package graaby.app.vendor.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.vendor.R;

/**
 * Created by gaara on 9/10/14.
 */
public class DiscountInstrumentAdapter extends ArrayAdapter<JSONObject> {

    LayoutInflater inflater = null;
    String fieldVal = "", rupees = "", fieldTypeDIscount = "", fieldID = "";

    public DiscountInstrumentAdapter(Context context) {
        super(context, R.layout.grid_item_discount_instrument);
        inflater = LayoutInflater.from(context);
        fieldTypeDIscount = context.getString(R.string.field_type_discount_item);
        fieldVal = context.getString(R.string.field_value);
        rupees = context.getString(R.string.Rs);
        fieldID = context.getString(R.string.field_id);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.grid_item_discount_instrument, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        JSONObject item = getItem(position);


        try {
            String discountType = item.getString(fieldTypeDIscount), offerValue = "";
            int drawableResourceId = -1;

            if (discountType.equals("p")) {
                drawableResourceId = R.drawable.punch_nopadding;
                offerValue = "Surprise Gift";
                holder.mTvItemValue.setSingleLine(Boolean.FALSE);
                holder.mTvItemValue.setGravity(Gravity.CENTER_HORIZONTAL);
                holder.mTvItemValue.setTextSize(32);
            } else if (discountType.equals("v")) {
                drawableResourceId = R.drawable.voucher_nopadding;
                offerValue = rupees + String.valueOf(item.getInt(fieldVal));
            } else if (discountType.equals("c")) {
                drawableResourceId = R.drawable.coupon_nopadding;
                offerValue = rupees + String.valueOf(item.getInt(fieldVal));
            }
            holder.mTvItemValue.setText(offerValue);

            holder.mTvDiscountItemId.setText(item.getString(fieldID));

            if (item.has(getContext().getString(R.string.field_gift_from))) {
                drawableResourceId = R.drawable.gift_voucher_nopadding;
                holder.mTvDiscountItemGiftedBy.setText(item.getString(getContext().getString(R.string.field_gift_from)));
                holder.mTvDiscountItemGiftedBy.setVisibility(View.VISIBLE);
            }

            holder.mTvItemValue.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'grid_item_discount_instrument.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */

    static class ViewHolder {
        @InjectView(R.id.tv_discount_item_id)
        TextView mTvDiscountItemId;
        @InjectView(R.id.tv_item_value)
        TextView mTvItemValue;
        @InjectView(R.id.tv_discount_item_gifted_by)
        TextView mTvDiscountItemGiftedBy;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
