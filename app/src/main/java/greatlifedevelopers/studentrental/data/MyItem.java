package greatlifedevelopers.studentrental.data;


import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by kenny on 6/10/15.
 */
public class MyItem implements ClusterItem {


    public final String name;
    public final String direccion, categoria, idAlojamiento;
    public final int profilePhoto;
    private final LatLng mPosition;

    public MyItem(LatLng position, String name, String direccion, String categoria, String idAlojamiento, int picturedResource){

        this.name = name;
        this.direccion = direccion;
        this.categoria = categoria;
        this.idAlojamiento = idAlojamiento;
        profilePhoto = picturedResource;
        mPosition = position;

    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }


}
