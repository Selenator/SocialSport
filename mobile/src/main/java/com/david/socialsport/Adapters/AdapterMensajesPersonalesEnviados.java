package com.david.socialsport.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.socialsport.Objetos.Comentarios;
import com.david.socialsport.R;
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

public class AdapterMensajesPersonalesEnviados extends ArrayAdapter<Comentarios> {

    private TextView nombreUsuarioMensaje, horaMensaje, textoMensaje;
    private CircleImageView imagenUsuarioMensaje;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private Bundle savedInstanceState;

    private String nombreUsuario, imagenUsuario;

    public AdapterMensajesPersonalesEnviados(@NonNull Context context, Bundle savedInstanceState) {
        super(context, 0, new ArrayList<Comentarios>());
        this.savedInstanceState = savedInstanceState;
    }


    public
    @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ficha_mensaje_personal, parent, false);
        }

        final Button botonAceptar = (Button) convertView.findViewById(R.id.boton_aceptar);
        final Button botonDeclinar = (Button) convertView.findViewById(R.id.boton_declinar);
        botonAceptar.setVisibility(View.GONE);
        botonDeclinar.setVisibility(View.GONE);

        final Comentarios mensaje = getItem(position);

        final DatabaseReference usuario = myRef.child("usuario").child(mensaje.getIdUsuarioRecibe());
        final View finalConvertView = convertView;
        usuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nombreUsuario=dataSnapshot.child("nombre").getValue(String.class);
                imagenUsuario =(dataSnapshot.child("imagen").getValue(String.class));

                nombreUsuarioMensaje = (TextView) finalConvertView.findViewById(R.id.chat_nombre_usuario);
                nombreUsuarioMensaje.setText(nombreUsuario);

                horaMensaje = (TextView) finalConvertView.findViewById(R.id.chat_fecha_hora);
                Date fechaHora = mensaje.getFecha_hora();
                horaMensaje.setText(new SimpleDateFormat("dd MMM").format(fechaHora) + " " + getContext().getString(R.string.a_las) + " " + new SimpleDateFormat("HH:mm").format(fechaHora));

                textoMensaje = (TextView) finalConvertView.findViewById(R.id.chat_texto_usuario);
                textoMensaje.setText(mensaje.getComentario());

                imagenUsuarioMensaje = (CircleImageView) finalConvertView.findViewById(R.id.chat_imagen_usuario);
                Picasso.with(getContext()).load(imagenUsuario).into(imagenUsuarioMensaje);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        return convertView;
    }
}
