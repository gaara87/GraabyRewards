package graaby.app.wallet.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.ContactsResponse;

/**
 * Created by Akash.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final ArrayList<ContactsResponse.ContactDetail> mList = new ArrayList<>();

    public void clear() {
        mList.clear();
    }

    public void addAll(List<ContactsResponse.ContactDetail> contactDetailList) {
        mList.addAll(contactDetailList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_contacts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactsResponse.ContactDetail node = mList.get(position);

        holder.contactName.setText(node.contactName);

        Glide.with(holder.contactPic.getContext())
                .load(node.pictureURL)
                .placeholder(R.drawable.ic_connections)
                .into(holder.contactPic);

        holder.contactPoints.setText(node.graabyPoints + " Graaby Points");

        holder.sharePoints.setTag(node.contactID);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.contacts_userProfilePicImageView)
        ImageView contactPic;
        @Bind(R.id.contacts_usertextView)
        TextView contactName;
        @Bind(R.id.contacts_points)
        TextView contactPoints;
        @Bind(R.id.share_points)
        Button sharePoints;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
