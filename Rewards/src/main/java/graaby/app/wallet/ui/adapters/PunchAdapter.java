package graaby.app.wallet.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.OutletDetail;

/**
 * Created by Akash.
 */
public class PunchAdapter extends ArrayAdapter<OutletDetail.Reward> {

    private LayoutInflater inflater;

    public PunchAdapter(Context context, List<OutletDetail.Reward> punches) {
        super(context, R.layout.fragment_punchcards, punches);
        inflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.item_list_reward_info, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        OutletDetail.Reward node = getItem(position);

        holder.text1.setText(node.rewardDetail);
        holder.text2.setText(node.onVisitCount);

        return convertView;
    }


    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_list_reward_info.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(android.R.id.text1)
        TextView text1;
        @Bind(android.R.id.text2)
        TextView text2;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
