package graaby.app.wallet.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.FeedsResponse;
import graaby.app.wallet.util.ActivityType;

/**
 * Created by gaara on 11/4/14.
 * Make some impeccable shyte
 */
public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ViewHolder> {

    private final ArrayList<FeedsResponse.NewsFeed> feeds = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_feed, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        FeedsResponse.NewsFeed node = feeds.get(i);

        holder.mContent.setText(node.newsContent);
        holder.mName.setText(node.newsSource);
        String timestamp = DateUtils.getRelativeTimeSpanString(
                node.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS).toString();
        holder.mTstamp.setText(timestamp);
        Glide.with(holder.mPic.getContext())
                .load(node.pictureURL)
                .placeholder(R.drawable.chatter_icon)
                .into(holder.mPic);
        holder.mIcon.setImageResource(ActivityType.getDrawableResourceIDForActivity(node.type));
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void clear() {
        feeds.clear();
    }

    public void addAll(List<FeedsResponse.NewsFeed> feedsList) {
        feeds.addAll(feedsList);
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_list_feed.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.pic)
        ImageView mPic;
        @Bind(R.id.feed_type)
        ImageView mIcon;
        @Bind(R.id.name)
        TextView mName;
        @Bind(R.id.content)
        TextView mContent;
        @Bind(R.id.tstamp)
        TextView mTstamp;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
