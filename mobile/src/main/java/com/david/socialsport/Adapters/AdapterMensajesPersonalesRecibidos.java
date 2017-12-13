package com.david.socialsport.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.david.socialsport.Objetos.Comentarios;
import com.david.socialsport.Objetos.Evento;
import com.david.socialsport.Objetos.Usuario;
import com.david.socialsport.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by david on 29/06/2017.
 */

public class AdapterMensajesPersonalesRecibidos extends ArrayAdapter<Comentarios> {

    private TextView nombreUsuarioMensaje, horaMensaje, textoMensaje;
    private CircleImageView imagenUsuarioMensaje;
    private Button botonAceptar, botonDeclinar;
    private LinearLayout peticiones;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private Bundle savedInstanceState;

    private String nombreUsuario, imagenUsuario, usuarioEnvio;

    String amigo;


    public AdapterMensajesPersonalesRecibidos(@NonNull Context context, Bundle savedInstanceState) {
        super(context, 0, new ArrayList<Comentarios>());
        this.savedInstanceState = savedInstanceState;
    }


    public
    @NonNull
    View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ficha_mensaje_personal, parent, false);

        }


        final Comentarios mensaje = getItem(position);

        final DatabaseReference usuario = myRef.child("usuario").child(mensaje.getIdUsuarioRemitente());
        final View finalConvertView = convertView;
        usuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                nombreUsuario = dataSnapshot.child("nombre").getValue(String.class);
                imagenUsuario = dataSnapshot.child("imagen").getValue(String.class);

                nombreUsuarioMensaje = (TextView) finalConvertView.findViewById(R.id.chat_nombre_usuario);
                nombreUsuarioMensaje.setText(nombreUsuario);

                horaMensaje = (TextView) finalConvertView.findViewById(R.id.chat_fecha_hora);
                Date fechaHora = mensaje.getFecha_hora();
                horaMensaje.setText(new SimpleDateFormat("dd MMM").format(fechaHora) + " " + getContext().getString(R.string.a_las) + " " + new SimpleDateFormat("HH:mm").format(fechaHora));

                textoMensaje = (TextView) finalConvertView.findViewById(R.id.chat_texto_usuario);
                textoMensaje.setText(mensaje.getComentario());

                imagenUsuarioMensaje = (CircleImageView) finalConvertView.findViewById(R.id.chat_imagen_usuario);
                Picasso.with(getContext()).load(imagenUsuario).into(imagenUsuarioMensaje);

                peticiones = (LinearLayout) finalConvertView.findViewById(R.id.peticion);
                botonAceptar = (Button) finalConvertView.findViewById(R.id.boton_aceptar);
                botonAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                System.out.println(position);
                                usuarioEnvio = dataSnapshot.child("usuario").child(mensaje.getIdUsuarioRemitente()).child("nombre").getValue(String.class);
                                textoMensaje.setText(usuarioEnvio + " " + getContext().getString(R.string.ahora_amigos));
                                amigo=(dataSnapshot.child("usuario").child(mensaje.getIdUsuarioRemitente()).child("id").getValue().toString());
                                Usuario usr = new Usuario(amigo);
                                anadirAmigo(usr);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

                botonDeclinar = (Button) finalConvertView.findViewById(R.id.boton_declinar);
                if (!mensaje.getPeticion()) {
                    peticiones.setVisibility(View.INVISIBLE);
                } else {
                    peticiones.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        return convertView;
    }

    private void anadirAmigo(Usuario amigos) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.child("usuario").child(userID).child("amigos").child(amigo).setValue(true);
        myRef.child("usuario").child(amigo).child("amigos").child(userID).setValue(true);

        myRef.child("usuario").child(userID).child("mensaje").child("recibido").child(mensaje.getIdComentario()).removeValue();
        myRef.child("usuario").child(amigo).child("mensaje").child("enviado").child(mensaje.getIdComentario()).removeValue();
        myRef.child("mensaje").child(mensaje.getIdComentario()).removeValue();

    }
}
