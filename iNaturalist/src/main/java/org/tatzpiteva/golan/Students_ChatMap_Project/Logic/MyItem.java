package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ta2er
 */
public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String title;
    private String snippet;
    private BitmapDescriptor icon;


    public MyItem(double lat, double lng ,String title ,String snippet ,BitmapDescriptor icon) {
        mPosition = new LatLng(lat, lng);
        this.title=title;
        this.snippet=snippet;
        this.icon=icon;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getTitle() {
        return title;
    }
}
