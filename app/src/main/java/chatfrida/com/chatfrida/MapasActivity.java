package chatfrida.com.chatfrida;


import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class MapasActivity extends AppCompatActivity {

    private String nombre;
    private double myLatitud;
    private double myLongitud;
    private MedirDistancia distancia;
    private Lugar lugar;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         final ArrayList<Lugar> lugares = new ArrayList<Lugar>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);
        ref = database.getReference("Lugares");
        recibirDatos();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final MapView mapa = (MapView) findViewById(R.id.map);
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setBuiltInZoomControls(true);
        mapa.setMultiTouchControls(true);

        //asignar un punto
        this.myLongitud = -myLongitud;
        Toast.makeText(this, " "+myLatitud+" "+myLongitud, Toast.LENGTH_SHORT).show();
        GeoPoint startPoint = new GeoPoint(myLatitud, -myLongitud);
        IMapController mapController = mapa.getController();
        mapController.setZoom(16);
        mapController.setCenter(startPoint);

        Marker startMarker = new Marker(mapa);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(startMarker);

        //startMarker.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
        startMarker.setTitle("aqui estoy");

        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        GeoPoint endPoint = new GeoPoint(20.3747018, -99.9841484);
        waypoints.add(startPoint);
        waypoints.add(endPoint);

        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        mapa.getOverlays().add(roadOverlay);

        // final FolderOverlay poiMarkers = new FolderOverlay();
        final RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(this);
        mapa.getOverlays().add(poiMarkers);

        final Drawable restauranteIcon = getResources().getDrawable(R.drawable.restaurante);
        final Drawable tacoIcon = getResources().getDrawable(R.drawable.taco);
        final Drawable cafeIcon = getResources().getDrawable(R.drawable.cafe);
        final Drawable pizzaIcon = getResources().getDrawable(R.drawable.pizza);
        final Drawable sushiIcon = getResources().getDrawable(R.drawable.sushi);
        final Drawable hamburguesaIcon = getResources().getDrawable(R.drawable.hamburguesa);
        final Drawable pastelIcon = getResources().getDrawable(R.drawable.pastel);
        final Drawable heladoIcon = getResources().getDrawable(R.drawable.helado);
        final Drawable panIcon = getResources().getDrawable(R.drawable.pan);

        ref.orderByChild("Puntaje").addChildEventListener(new ChildEventListener() {

            ArrayList<Double> listado = new ArrayList<Double>();
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                double dist, val=0;
                int count=0;
                lugar = dataSnapshot.getValue(Lugar.class);
                lugares.add(lugar);
                Marker poiMarker = new Marker(mapa);
                poiMarker.setTitle(lugar.Nombre+"("+lugar.Comida+")");
                poiMarker.setSnippet(lugar.Direccion);
                poiMarker.setSubDescription( " Puntuacion :"+lugar.Puntaje +
                        " Horario :"+lugar.HorarioInicio+
                        " hrs. - "+lugar.HorarioFin+" hrs.");
                GeoPoint point = new GeoPoint(lugar.Latitud,lugar.Longitud);
                poiMarker.setPosition(point);

                if(lugar.Comida.equals("Tacos")){
                    poiMarker.setIcon(tacoIcon);
                }
                else if(lugar.Comida.equals("Restaurante") || lugar.Comida.equals("Restaurante Bar")){
                    poiMarker.setIcon(restauranteIcon);
                }
                else if(lugar.Comida.equals("Cafeteria") || lugar.Comida.equals("Cafe")){
                    poiMarker.setIcon(cafeIcon);
                }
                else if(lugar.Comida.equals("Hamburguesas")){
                    poiMarker.setIcon(hamburguesaIcon);
                }
                else if(lugar.Comida.equals("Restaurante-Sushi")){
                    poiMarker.setIcon(sushiIcon);
                }
                else if(lugar.Comida.equals("Pizzas")){
                    poiMarker.setIcon(pizzaIcon);
                }
                else if(lugar.Comida.equals("Pastel")){
                    poiMarker.setIcon(pastelIcon);
                }
                else if(lugar.Comida.equals("Pan")){
                    poiMarker.setIcon(panIcon);
                }
                else if(lugar.Comida.equals("Helados")){
                    poiMarker.setIcon(heladoIcon);
                }
                poiMarkers.add(poiMarker);
             //
                //   System.out.println("direccion 123 "+lugar.Direccion+"  "+ lugar.Longitud+ "  "+lugar.Latitud+"  "+lugar.Puntaje);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (lugares.isEmpty()) {
            System.out.println("direccion 123 " );
        }


        mapa.invalidate();
    }

    public void recibirDatos(){
        Bundle extras = this.getIntent().getExtras();
        this.nombre = extras.getString("nombre");
        this.myLatitud = Double.parseDouble(extras.getString("latitud"));
        this.myLongitud = Double.parseDouble(extras.getString("longitud"));
    }
}
