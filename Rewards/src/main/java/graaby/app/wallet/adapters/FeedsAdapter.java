package graaby.app.wallet.adapters;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.FeedsResponse;
import graaby.app.wallet.util.ActivityType;

/**
 * Created by gaara on 11/4/14.
 * Make some impeccable shyte
 */
public class FeedsAdapter extends ArrayAdapter<FeedsResponse.NewsFeed> {

    private LayoutInflater inflater;

    public FeedsAdapter(Activity activity) {
        super(activity, R.layout.fragment_feeds);
        inflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.item_list_feed, null, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        FeedsResponse.NewsFeed node = getItem(position);

        holder.mContent.setText(node.newsContent);
        holder.mName.setText(node.newsSource);
        String timestamp = DateUtils.getRelativeTimeSpanString(
                node.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS).toString();
        holder.mTstamp.setText(timestamp);
        Glide.with(getContext())
                .load(node.pictureURL)
                .placeholder(R.drawable.chatter_icon)
                .into(holder.mPic);
        holder.mIcon.setImageResource(ActivityType.getDrawableResourceIDForActivity(node.type));

        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_list_feed.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder {
        @InjectView(R.id.pic)
        ImageView mPic;
        @InjectView(R.id.feed_type)
        ImageView mIcon;
        @InjectView(R.id.name)
        TextView mName;
        @InjectView(R.id.content)
        TextView mContent;
        @InjectView(R.id.tstamp)
        TextView mTstamp;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
