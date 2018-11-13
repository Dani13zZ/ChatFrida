package chatfrida.com.chatfrida;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    Button btnAceptar;
    EditText txtNombre;
    String nombre;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference BD;
    DatabaseReference chatUsr;
    String direccion;
    double latitud;
    double longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnAceptar = (Button) findViewById(R.id.btnLogin);
        txtNombre = (EditText) findViewById(R.id.txtNombre);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 10000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void onClick(View view){
        Intent siguiente = new Intent(LoginActivity.this, MainActivity.class);
        nombre = txtNombre.getText().toString();

        if (validarNombre(nombre) == true) {
            Toast.makeText(this, nombre, Toast.LENGTH_LONG).show();
            BD = database.getReference("Usuarios/"+nombre+"/Ubicacion");
            chatUsr = database.getReference("Usuarios/"+nombre+"/chat");
            chatUsr.removeValue();
            ChatMensaje chatMessage = new ChatMensaje("Bienvenido "+nombre, "bot");
            chatUsr.push().setValue(chatMessage);
            BD.child("Latitud").setValue(latitud);
            BD.child("Longitud").setValue(longitud);
            BD.child("Direccion").setValue(direccion);

            Bundle mibundle = new Bundle();
            mibundle.putString("nombre",nombre);
            siguiente.putExtras(mibundle);
            startActivity(siguiente);

        }else{
            Toast.makeText(this, "el nombre no debe estar vacio", Toast.LENGTH_SHORT).show();
            txtNombre.setText("");
        }
    }


    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(LoginActivity.LOCATION_SERVICE);
        Localizacion localizacion = new Localizacion();
        localizacion.setLoginActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) localizacion);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) localizacion);

    }

    public void setLocation(Location location) {
        if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();
                    direccion = DirCalle.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean validarNombre(String nombre){
        if (nombre.equals("")){
            return  false;
        }else
            return true;
    }

    public class Localizacion implements LocationListener {
        LoginActivity loginActivity;

        public LoginActivity getLoginActivity() {
            return loginActivity;
        }

        public void setLoginActivity(LoginActivity loginActivity) {
            this.loginActivity = loginActivity;
        }

        @Override
        public void onLocationChanged(Location location) {
            location.getLatitude();
            location.getLongitude();
            loginActivity.setLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(loginActivity, "GPS Desaactivado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast.makeText(loginActivity, "GPS Activado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }



}








