package chatfrida.com.chatfrida;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MapasActivity extends AppCompatActivity {

    private String nombre;
    private double myLatitud;
    private double myLongitud;
    static Lugar lugar;
    static ArrayList<Lugar> lugares = new ArrayList<Lugar>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);
        recibirDatos();
        ref = database.getReference("Lugares/Tipo/Comida");
        final MapView map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //mi Ubicacion
        this.myLongitud = myLongitud*-1;
        Toast.makeText(this, ""+(myLatitud)+" "+ myLongitud, Toast.LENGTH_SHORT).show();
        GeoPoint startPoint = new GeoPoint(20.378584999999998, -99.97083333333333);
        IMapController mapController = map.getController();

        //zoom
        mapController.setZoom(16);
        mapController.setCenter(startPoint);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        map.invalidate();
    }

    public void recibirDatos(){
        Bundle extras = this.getIntent().getExtras();
        this.nombre = extras.getString("nombre");
        this.myLatitud = Double.parseDouble(extras.getString("latitud"));
        this.myLongitud = Double.parseDouble(extras.getString("longitud"));
    }
}
