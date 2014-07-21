package graaby.app.wallet.model;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import graaby.app.wallet.R;

/**
 * Created by gaara on 7/21/14.
 */
public class BusinessMarkerRenderer extends DefaultClusterRenderer<BusinessMarker> {

    public BusinessMarkerRenderer(Context context, GoogleMap map, ClusterManager<BusinessMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(BusinessMarker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.business_map_pointer))
                .title(item.getTitle())
                .snippet(item.getArea());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<BusinessMarker> cluster) {
        return cluster.getSize() > 5;
    }
}
