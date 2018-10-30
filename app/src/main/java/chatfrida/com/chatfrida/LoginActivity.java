package chatfrida.com.chatfrida;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    Button btnAceptar;
    EditText txtNombre;
    String nombre;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference BD;
    DatabaseReference chatUsr;

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS ;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnAceptar = (Button) findViewById(R.id.btnLogin);
        txtNombre = (EditText) findViewById(R.id.txtNombre);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    public void onClick(View view){
        Intent siguiente = new Intent(LoginActivity.this, MainActivity.class);
        nombre = txtNombre.getText().toString();
        if (validar_nombre(nombre) == true) {

            Toast.makeText(this, nombre, Toast.LENGTH_LONG).show();
            BD = database.getReference("Usuarios/"+nombre+"/Ubicacion");
            chatUsr = database.getReference("Usuarios/"+nombre+"/chat");
            chatUsr.removeValue();

            ChatMessage chatMessage = new ChatMessage("Bienvenido "+nombre, "bot");
            chatUsr.push().setValue(chatMessage);

            obtener_cordenadas();
            Bundle mibundle =new Bundle();
            mibundle.putString("nombre",nombre);

            siguiente.putExtras(mibundle);
            startActivity(siguiente);
        }
        else {
            Toast.makeText(this, "el nombre no debe estar vacio", Toast.LENGTH_SHORT).show();
            txtNombre.setText("");
        }
    }

    public void obtener_cordenadas() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.e("latitud: ",+ location.getLatitude()+"longitud: "+location.getLongitude());
                            BD.child("Latitud").setValue(location.getLatitude());
                            BD.child("Longitud").setValue(location.getLongitude());
                        }
                    }
                });
    }
    public boolean validar_nombre(String nombre){
        if (nombre.equals("")){
            return  false;
        }else
            return true;
    }
}
