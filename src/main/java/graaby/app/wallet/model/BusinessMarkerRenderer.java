package graaby.app.wallet.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import graaby.app.wallet.R;

/**
 * Created by gaara on 7/21/14.
 */
public class BusinessMarkerRenderer extends DefaultClusterRenderer<BusinessMarker> {

    private final TextView mBusinessNameTextView;
    private final IconGenerator mIconGenerator;

    public BusinessMarkerRenderer(Context context, GoogleMap map, ClusterManager<BusinessMarker> clusterManager) {
        super(context, map, clusterManager);
        View inflatedView = LayoutInflater.from(context).inflate(R.layout.business_marker, null);
        mBusinessNameTextView = (TextView) inflatedView.findViewById(R.id.item_businessNameTextView);
        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setContentView(inflatedView);
        mIconGenerator.setBackground(null);
    }

    @Override
    protected void onBeforeClusterItemRendered(BusinessMarker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        mBusinessNameTextView.setText(item.getTitle());
        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory
                .fromBitmap(icon))
                .title(item.getTitle())
                .snippet(item.getArea());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<BusinessMarker> cluster) {
        return cluster.getSize() > 5;
    }
}
