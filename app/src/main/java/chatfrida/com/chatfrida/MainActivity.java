package chatfrida.com.chatfrida;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;


public class MainActivity extends AppCompatActivity implements AIListener{
    Lugar lugar;
    RecyclerView recyclerView;
    EditText txtMsj;
    ImageView btnMapa;
    RelativeLayout addBtn;
    DatabaseReference ref;
    DatabaseReference BDLugares;
    FirebaseRecyclerAdapter<ChatMensaje,ChatRecycler> adapter;
    Boolean flagFab = true;
    String nombre;
    String comida;
    private TextToSpeech mTextToSpeech;
    private AIService aiService;
     String myLatitud;
     String myLongitud;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        txtMsj = findViewById(R.id.txtMsj);
        addBtn = findViewById(R.id.addBtn);
        recyclerView.setHasFixedSize(true);
        btnMapa = findViewById(R.id.btnMapa);
        BDLugares = FirebaseDatabase.getInstance().getReference("Lugares");
        recibirDatos();
        generarDistancia();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        ref = FirebaseDatabase.getInstance().getReference("Usuarios/"+nombre);
        ref.keepSynced(false);

        final AIConfiguration config = new AIConfiguration("4de3402e39624c3aaceebabe2b3b4b36",
                                           AIConfiguration.SupportedLanguages.Spanish,
                                           AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);
        final AIRequest aiRequest = new AIRequest();

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = txtMsj.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMensaje chatMensaje = new ChatMensaje(message, "user");
                    ref.child("chat/").push().setValue(chatMensaje);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest,Void,AIResponse>(){
                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {
                                Result result = response.getResult();
                                final String reply = result.getFulfillment().getSpeech();
                                final Metadata metadata = result.getMetadata();
//                                ChatMensaje chatMensaje = new ChatMensaje(reply, "bot");
//                                ref.child("chat").push().setValue(chatMensaje);
                                if (metadata != null) {
                                    if(metadata.getIntentName().equals("Ubicacion")){
                                        obtenerUbicacion(reply);
                                    }
                                    else if(metadata.getIntentName().equals("Comidas")){
                                             generarListaUbicaciones(result, reply);
                                    } else
                                        if (metadata.getIntentName().equals("Comidas - yes - yes - custom")) {
                                            generarUbicacionCercana(result, reply);
                                        }
                                    else{
                                        ChatMensaje chatMensaje = new ChatMensaje(reply, "bot");
                                        ref.child("chat").push().setValue(chatMensaje);
                                    }
                                }
                            }
                        }
                    }.execute(aiRequest);
                }
                else {
                    aiService.startListening();
                }
                txtMsj.setText("");
            }
        });

        txtMsj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView)findViewById(R.id.btn_image);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
                Bitmap img2 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);

                if (s.toString().trim().length() != 0 && flagFab){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img1);
                    flagFab = false;
                }
                else if (s.toString().trim().length() == 0){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img2);
                    flagFab = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMensaje, ChatRecycler>(ChatMensaje.class,R.layout.msglist,ChatRecycler.class,ref.child("chat")) {
            @Override
            protected void populateViewHolder(ChatRecycler viewHolder, ChatMensaje model, int position) {
                if (model.getMsgUser().equals("user")) {
                    viewHolder.rightText.setText(model.getMsgText());
                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                }
                else {
                    viewHolder.leftText.setText(model.getMsgText());
                    viewHolder.rightText.setVisibility(View.GONE);
                    viewHolder.leftText.setVisibility(View.VISIBLE);
                }
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public void recibirDatos(){
        Bundle extras = this.getIntent().getExtras();
        this.nombre = extras.getString("nombre");
        this.myLatitud = extras.getString("latitud");
        this.myLongitud = extras.getString("longitud");
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

        final Result result = response.getResult();
        String message = result.getResolvedQuery();
        ChatMensaje chatMensaje0 = new ChatMensaje(message, "user");
        ref.child("chat").push().setValue(chatMensaje0);

        mTextToSpeech.speak(result.getFulfillment().getSpeech(), TextToSpeech.QUEUE_FLUSH, null, null);
        final String reply = result.getFulfillment().getSpeech();
        final Metadata metadata = result.getMetadata();

        if (metadata != null) {
            if(metadata.getIntentName().equals("Ubicacion")){
                obtenerUbicacion(reply);
            }
            else
                if(metadata.getIntentName().equals("Comidas")){
                generarListaUbicaciones(result, reply);
            }
            else
                if(metadata.getIntentName().equals("Comidas - yes - yes - custom")){
                    generarUbicacionCercana(result, reply);
                }
                else{
                    ChatMensaje chatMensaje = new ChatMensaje(reply, "bot");
                    ref.child("chat").push().setValue(chatMensaje);
            }
        }
    }


    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        Toast.makeText(this, "Escuchando", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {
        Toast.makeText(this, "mensaje finalizado", Toast.LENGTH_SHORT).show();
    }
    public void onClickMap(View view){
        Intent siguiente = new Intent(MainActivity.this, MapasActivity.class);
        Bundle mibundle = new Bundle();
        mibundle.putString("nombre",nombre);
        mibundle.putString("latitud", myLatitud);
        mibundle.putString("longitud", myLongitud);
        siguiente.putExtras(mibundle);
        startActivity(siguiente);
    }

    public void generarDistancia(){
        Query q = BDLugares.orderByChild("Comida");
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String datos="";
                double la1 = 0;
                double lo1 = 0;
                double la2 = 0;
                double lo2 = 0;
                double dist = 0;
                int count = 0;
                String c;
                MedirDistancia distancia = new MedirDistancia();
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()) {
                    count++;

                    lugar = datasnapshot.getValue(Lugar.class);
                    la1 = Double.parseDouble(String.valueOf(myLatitud));
                    lo1 = Double.parseDouble(String.valueOf(myLongitud));
                    la2 = lugar.Latitud;
                    lo2 = lugar.Longitud;

                    dist = distancia.obtenerDistancia(la1,lo1,la2,lo2);
                    System.out.println("ditancia " + dist);

                    c = String.valueOf(count);
                    BDLugares.child(c).child("Distancia").setValue(dist);
                }

                System.out.println("my latitud " + la1);
                System.out.println("my longitud " + lo1);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void generarUbicacionCercana(Result result, final String reply) {
        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            System.out.println("parameters");
            for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
                this.comida = String.valueOf(entry.getValue());
                this.comida = this.comida.substring(1,this.comida.length()-1);

            }
            Query q = BDLugares.orderByChild("Comida").equalTo(comida).limitToLast(1);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String nombre,horario,distancia, puntaje="";
                    ChatMensaje chatMensaje = new ChatMensaje(reply +"  ", "bot");
                    ref.child("chat").push().setValue(chatMensaje);
                    for(DataSnapshot datasnapshot: dataSnapshot.getChildren()) {
                        lugar = datasnapshot.getValue(Lugar.class);
                        nombre = lugar.Nombre;
                        puntaje = String.valueOf(lugar.Puntaje);
                        distancia = String.format("%.2f", lugar.Distancia);
                        horario = String.valueOf(lugar.HorarioInicio) + " - "+String.valueOf(lugar.HorarioFin)+" hrs.";
                        chatMensaje = new ChatMensaje(nombre+",\n Distancia : "+distancia+"km \n Horario : "+horario+
                                "\n Puntaje : "+puntaje, "bot");
                        ref.child("chat").push().setValue(chatMensaje);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void obtenerUbicacion(final String reply) {
        ref.child("Ubicacion/Direccion").addValueEventListener(new ValueEventListener() {
            String ubicacion;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ubicacion = dataSnapshot.getValue().toString();
                ChatMensaje chatMensaje = new ChatMensaje(reply +" : "+ ubicacion, "bot");
                ref.child("chat").push().setValue(chatMensaje);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void generarListaUbicaciones(Result result, final String reply) {
        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            System.out.println("parameters");
            for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
                this.comida = String.valueOf(entry.getValue());
                this.comida = comida.substring(1,comida.length()-1);
            }
            Query q = BDLugares.orderByChild("Comida").equalTo(this.comida);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String nombre,horario,distancia, puntaje="";
                    ChatMensaje chatMensaje = new ChatMensaje(reply +" : ", "bot");
                    ref.child("chat").push().setValue(chatMensaje);
                    for(DataSnapshot datasnapshot: dataSnapshot.getChildren()) {
                        lugar = datasnapshot.getValue(Lugar.class);
                        nombre = lugar.Nombre;
                        puntaje = String.valueOf(lugar.Puntaje);
                        distancia = String.format("%.2f", lugar.Distancia);
                        horario = String.valueOf(lugar.HorarioInicio) + " - "+String.valueOf(lugar.HorarioFin)+" hrs.";
                        chatMensaje = new ChatMensaje(nombre+",\n Distancia : "+distancia+"km \n Horario : "+horario+
                                "\n Puntaje : "+puntaje, "bot");
                        ref.child("chat").push().setValue(chatMensaje);
                    }
                    chatMensaje = new ChatMensaje("¿Quieres hacer una Busqueda mas Especifica?", "bot");
                    ref.child("chat").push().setValue(chatMensaje);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}