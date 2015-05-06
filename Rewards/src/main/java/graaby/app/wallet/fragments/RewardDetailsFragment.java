package graaby.app.wallet.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import graaby.app.wallet.R;
import graaby.app.wallet.adapters.PunchAdapter;
import graaby.app.wallet.models.retrofit.OutletDetail;

/**
 * Created by Akash.
 */
public class RewardDetailsFragment extends Fragment {

    private ListView mListView;
    private TextView mDiscountTextView;

    public RewardDetailsFragment() {

    }

    public static RewardDetailsFragment newInstance() {
        return new RewardDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reward_info, null);
        mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setEmptyView(v.findViewById(android.R.id.empty));
        mDiscountTextView = (TextView) v.findViewById(R.id.business_reward_discount_value);
        return v;
    }

    public void setPunchCards(Context context, Integer discount, List<OutletDetail.Reward> punches) {
        mDiscountTextView.setText(String.valueOf(discount) + "%");
        mListView.setAdapter(new PunchAdapter(context, punches));
    }

}
