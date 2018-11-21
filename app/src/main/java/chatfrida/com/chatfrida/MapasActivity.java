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
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
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
    static Lugar lugar;
    static ArrayList<Lugar> lugares = new ArrayList<Lugar>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//        recibirDatos();
//        ref = database.getReference("Lugares/Tipo/Comida");
//        final MapView map = findViewById(R.id.map);
//        map.setTileSource(TileSourceFactory.MAPNIK);
//
//        map.setBuiltInZoomControls(true);
//        map.setMultiTouchControls(true);
//
//        //mi Ubicacion
//        this.myLongitud = myLongitud*-1;
//        Toast.makeText(this, ""+(myLatitud)+" "+ myLongitud, Toast.LENGTH_SHORT).show();
//        GeoPoint startPoint = new GeoPoint(20.378584999999998, -99.97083333333333);
//        IMapController mapController = map.getController();
//
//        //zoom
//        mapController.setZoom(16);
//        mapController.setCenter(startPoint);
//        Marker startMarker = new Marker(map);
//        startMarker.setPosition(startPoint);
//        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        map.getOverlays().add(startMarker);
//        startMarker.setTitle("aqui estoy");
//        map.invalidate();


  //      ref = database.getReference("Lugares/Tipo/Comida");

//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Lugar post = dataSnapshot.getValue(Lugar.class);
//                System.out.println("hola "+ post.Direccion);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });

        ref = database.getReference("Lugares/Tipo/Comida");

//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Lugar post = dataSnapshot.getValue(Lugar.class);
//                System.out.println("hola "+ post.Direccion);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });





        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //////=-------------------1------------------////
        final MapView mapa = (MapView) findViewById(R.id.mapa);
        mapa.setTileSource(TileSourceFactory.MAPNIK);

        //agregar controles de zoom
        mapa.setBuiltInZoomControls(true);
        mapa.setMultiTouchControls(true);

        //asignar un punto
        GeoPoint startPoint = new GeoPoint(20.378506666666667, -99.97083333333333);
        IMapController mapController = mapa.getController();

        //zoom
        mapController.setZoom(16);
        mapController.setCenter(startPoint);

        //agregar un marcador
        Marker startMarker = new Marker(mapa);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(startMarker);

        //agregar un icono al marcador del mapa
        //startMarker.setIcon(getResources().getDrawable(R.drawable.ic_launcher));

        //agregar un titulo al marcador esto solo se activa al tocar el icono
        startMarker.setTitle("aqui estoy");



        //////=-------------------2------------------////

        //clase para crear rutas
        RoadManager roadManager = new OSRMRoadManager(this);

        //almacena puntos en una estructura
        //punto de inicio
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);

        //punto final
        GeoPoint endPoint = new GeoPoint(20.374713333333332, -99.98412833333332);
        waypoints.add(endPoint);

        //recuperar ruta de la estructura arraylist
        Road road = roadManager.getRoad(waypoints);

        // construye una linea entre los puntos
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

        //agrega la ruta y la dibuja en el mapa
        mapa.getOverlays().add(roadOverlay);


        //////=-------------------3------------------////


        // En primer lugar, creamos una instancia de un proveedor de PDI
        // de Nominatim (el servicio de Nominatim solo requiere que usted defina su agente de usuario) y hacemos la solicitud:


        //Bakery = panaderia
        //Fast food = comida rapida
        //Cafe
        //Restaurant
        //Bar
        // GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("daniel13zz");
        // ArrayList<POI> pois = poiProvider.getPOIInside(map.getBoundingBox(), 50);

        NominatimPOIProvider poiProvider = new NominatimPOIProvider("OSMBonusPackTutoUserAgent");
        ArrayList<POI> pois = poiProvider.getPOICloseTo(startPoint, "Bars", 50, 1);

        //  Podemos construir los marcadores de PDI y colocarlos en el mapa.
        //  agrupando todos esos marcadores de POI en un FolderOverlay.

        // final FolderOverlay poiMarkers = new FolderOverlay();
        final RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(this);
        mapa.getOverlays().add(poiMarkers);

        //crear esos marcadores y ponerlos en el folder overlay
//        final Drawable restauranteIcon = getResources().getDrawable(R.drawable.restaurante);
//        final Drawable tacoIcon = getResources().getDrawable(R.drawable.taco);
//        final Drawable cafeIcon = getResources().getDrawable(R.drawable.cafe);
//        final Drawable pizzaIcon = getResources().getDrawable(R.drawable.pizza);
//        final Drawable sushiIcon = getResources().getDrawable(R.drawable.sushi);
//        final Drawable hamburguesaIcon = getResources().getDrawable(R.drawable.hamburguesa);


//        for (POI poi:pois){
//            Marker poiMarker = new Marker(map);
//            poiMarker.setTitle(poi.mType);
//            poiMarker.setSnippet(poi.mDescription);
//            poiMarker.setPosition(poi.mLocation);
//            poiMarker.setIcon(poiIcon);
//        //    poiMarker.setInfoWindow(new CustomInfoWindow(map));
//
////            if (poi.mThumbnail != null){
////                poiItem.setImage(new BitmapDrawable(poi.mThumbnail));
////            }
//            poiMarkers.add(poiMarker);
//        }




        ref.orderByChild("Puntaje").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
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

//                if(lugar.Comida.equals("Taqueria")){
//                    poiMarker.setIcon(tacoIcon);
//                }
//                else if(lugar.Comida.equals("Restaurante") || lugar.Comida.equals("Restaurante-Bar")){
//                    poiMarker.setIcon(restauranteIcon);
//                }
//                else if(lugar.Comida.equals("Cafeteria")){
//                    poiMarker.setIcon(cafeIcon);
//                }
//                else if(lugar.Comida.equals("Hamburguesas")){
//                    poiMarker.setIcon(hamburguesaIcon);
//                }
//                else if(lugar.Comida.equals("Restaurante-Sushi")){
//                    poiMarker.setIcon(sushiIcon);
//                }
//                else if(lugar.Comida.equals("Pizzeria")){
//                    poiMarker.setIcon(pizzaIcon);
//                }
                poiMarkers.add(poiMarker);
                System.out.println("direccion 123 "+lugar.Direccion+"  "+ lugar.Longitud+ "  "+lugar.Latitud+"  "+lugar.Puntaje);
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
        mapa.invalidate();
    }

//    public void recibirDatos(){
//        Bundle extras = this.getIntent().getExtras();
//        this.nombre = extras.getString("nombre");
//        this.myLatitud = Double.parseDouble(extras.getString("latitud"));
//        this.myLongitud = Double.parseDouble(extras.getString("longitud"));
//    }
}
