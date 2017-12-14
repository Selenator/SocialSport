package com.david.socialsport.Pantallas;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.david.socialsport.Adapters.AdapterAmigos;
import com.david.socialsport.Adapters.AdapterAmigosGrupo;
import com.david.socialsport.Adapters.AdapterBuscarAmigos;
import com.david.socialsport.Adapters.ArrayAdapterIconos;
import com.david.socialsport.Objetos.Grupo;
import com.david.socialsport.Objetos.Usuario;
import com.david.socialsport.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by david on 13/12/17.
 */

public class PantallaCrearGrupo extends AppCompatActivity {

    private String grupoId, usuarioId, nombreGrupo, imagenGRupo;

    EditText textGrupo;
    CircleImageView imagenCircleGrupo;
    FloatingActionButton botonGuardar;
    CheckBox check;
    ActionBar menuBar;
    ListView listView;
    AdapterAmigosGrupo adapter;

    private static final int OPEN_REQUEST_CODE = 41;
    private static final int CAMERA_REQUEST = 1888;

    boolean imagenBoolean;
    Bitmap bitmap;
    StorageReference imagesRef;

    int MIS_PERMISOS_CAMERA;

    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase database = FirebaseDatabase.getInstance().getInstance();
    DatabaseReference miReferencia = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_grupo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        menuBar = getSupportActionBar();
        if (menuBar != null) {
            menuBar.setDisplayHomeAsUpEnabled(true);

        }

        textGrupo=(EditText) findViewById(R.id.nombreGRupo);
        imagenCircleGrupo=(CircleImageView) findViewById(R.id.imagenGrupo);
        botonGuardar=(FloatingActionButton) findViewById(R.id.boton_guardar);
        check = (CheckBox) findViewById(R.id.usuario_grupo_check);

        adapter = new AdapterAmigosGrupo(getApplicationContext(),savedInstanceState);
        listView = (ListView) findViewById(R.id.listaAmigos);

        listView.setAdapter(adapter);
        cargarAmigos();





        imagenCircleGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{getString(R.string.galeria), getString(R.string.camara)};
                final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery, R.drawable.ic_menu_camera};
                ListAdapter adapter = new ArrayAdapterIconos(PantallaCrearGrupo.this, items, icons);

                new AlertDialog.Builder(PantallaCrearGrupo.this)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0:
                                        imagenGaleria();
                                        break;
                                    case 1:
                                        pedirPermisos();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
            }
        });

        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textGrupo.getText() != null && !textGrupo.getText().toString().trim().isEmpty()) {
                    ProgressDialog.show(PantallaCrearGrupo.this, "Creando", "Creando Grupo");
                    crearGrupo();
                } else {
                    Snackbar.make(v, "Creo que te olvidas del nombre!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

    }
    private void crearGrupo() {
        final String key = miReferencia.child("grupo").push().getKey();


        if(imagenBoolean){
            crearConImagen(key);
        }
        else{
            crear(key, null);
        }


    }

    //Un poco mas de sutileza para no repetir codigo
    private void crear(String key, Uri downloadUrl) {
        miReferencia.child("grupo").child(key).child("nombre").setValue(textGrupo.getText().toString());
        miReferencia.child("usuario").child(userID).child("grupo").child(key).setValue(true);
        if(downloadUrl != null)
                miReferencia.child("grupo").child(key).child("imagen").setValue(downloadUrl.toString());
        //recorre el array con los integrantes y los añade a la BBDD
        for(String idIn:adapter.getIdsIntegrantes())
            miReferencia.child("grupo").child(key).child("integrantes").child(idIn).setValue(true);
        Toast.makeText(getBaseContext(), "EXITO", Toast.LENGTH_LONG).show();
        finish();
    }

    private void crearConImagen(final String key) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://socialsport-e98f4.appspot.com");
        StorageReference imagesRef = storageRef.child("grupos").child(key+".png");
        imagenCircleGrupo.setDrawingCacheEnabled(true);
        imagenCircleGrupo.buildDrawingCache();
        //Bitmap bitmap = escudo.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getBaseContext(),"ALGO SALIO MAL",Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                crear(key, downloadUrl);
            }
        });
    }
    public void cargarAmigos() {
        miReferencia.addListenerForSingleValueEvent(new ValueEventListener() {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void imagenCamara() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
        imagenBoolean = true;
    }

    private void pedirPermisos() {
        if (ContextCompat.checkSelfPermission(PantallaCrearGrupo.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(PantallaCrearGrupo.this,
                    Manifest.permission.CAMERA)) {

                ActivityCompat.requestPermissions(PantallaCrearGrupo.this,
                        new String[]{Manifest.permission.CAMERA},
                        MIS_PERMISOS_CAMERA);

            }
        } else
            imagenCamara();

    }

    private void imagenGaleria() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
        imagenBoolean = true;
    }

    //Cargamos la nueva imagen en el Circle Image View
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case OPEN_REQUEST_CODE:
                    if (resultData != null) {
                        currentUri = resultData.getData();
                        try {
                            bitmap = getBitmapFromUri(currentUri);
                            imagenCircleGrupo.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            // Handle error here
                        }
                    }
                    break;
                case CAMERA_REQUEST:
                    Bitmap photo = (Bitmap) resultData.getExtras().get("data");
                    bitmap = photo;
                    imagenCircleGrupo.setImageBitmap(photo);
                    break;
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
