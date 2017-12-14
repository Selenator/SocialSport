package com.david.socialsport.Dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.david.socialsport.Adapters.AdapterAmigos;
import com.david.socialsport.Adapters.AdapterAmigosGrupo;
import com.david.socialsport.Adapters.AdapterAmigosInvitar;
import com.david.socialsport.Adapters.AdapterUsuarios;
import com.david.socialsport.Objetos.Comentarios;
import com.david.socialsport.Objetos.Evento;
import com.david.socialsport.Objetos.Usuario;
import com.david.socialsport.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by david on 20/06/2017.
 */

public class InvitarUsuarios extends Activity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private AdapterAmigosInvitar adapter;
    private String eventoID;
    private String userID;
    ListView listView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lista_usuarios_invitar);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        adapter = new AdapterAmigosInvitar(getApplicationContext(),savedInstanceState);

        eventoID = getIntent().getStringExtra("eventoID");
        //userID = getIntent().getStringExtra("userID");
        System.out.println(eventoID);
        listView = (ListView) findViewById(R.id.listaUsuarios);
        listView.setAdapter(adapter);
        //listView.setEmptyView(findViewById(android.R.id.empty));
        cargarAmigos();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //sacamos el amigo del adapter (pillo la referencia desde el propio listview pero deberia ser la misma que tienes en adapter)
                Usuario amigo = (Usuario) listView.getAdapter().getItem(position);
                //MIRA BIEN ESTA LINEA QUE ME HE INVENTADO LA ESTRUCTURA DE LA BBDD, lee el comentario del git
                myRef.child("usuario").child("amigo").child("mensajes").child("invitacionesEvento").child(eventoID).setValue(true);
                Toast.makeToast(context,"Invitacion Enviada", Toast.Length_SHORT).show();
                finish();
            }
        });

    }

    public void cargarAmigos() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                adapter.clear();
                GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> amigosId = dataSnapshot.child("usuario").child(userID).child("amigos").getValue(t);
                if (amigosId != null) {
                    for (final String id : amigosId.keySet()) {

                        Usuario usuario = dataSnapshot.child("usuario").child(id).child("amigos").getValue(Usuario.class);

                        if (usuario != null) {
                            usuario.setIdAmigo(id);
                            adapter.add(usuario);

                        } else {
                        }
                    }
                    adapter.sort(new Comparator<Usuario>() {
                        @Override
                        public int compare(Usuario o1, Usuario o2) {
                            return o2.getIdAmigo().compareTo(o1.getIdAmigo());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
    }


}
