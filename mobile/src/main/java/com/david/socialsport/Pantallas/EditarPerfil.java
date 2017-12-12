package com.david.socialsport.Pantallas;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.david.socialsport.Adapters.ArrayAdapterIconos;
import com.david.socialsport.Dialogs.DialogCambioContrasena;
import com.david.socialsport.Objetos.Evento;
import com.david.socialsport.Objetos.Usuario;
import com.david.socialsport.R;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by david on 27/06/2017.
 */

public class EditarPerfil extends AppCompatActivity {

    String userID;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    ActionBar menuBar;

    String imgURL;

    CircleImageView imagenUsuario;
    EditText nombreUsuario, correoUsuario, apellidosUsuario;
    static EditText fechaUsuario;
    RadioGroup sexoUsuario;
    Button editar, cambiarPass, aceptar;

    static int usuarioYear, usuarioMonth, usuarioDay;


    private static final int OPEN_REQUEST_CODE = 41;
    private static final int CAMERA_REQUEST = 1888;

    boolean imagenBoolean;
    Bitmap bitmap;
    StorageReference imagesRef;

    int MIS_PERMISOS_CAMERA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        menuBar = getSupportActionBar();
        menuBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_editar_perfil);

        userID = getIntent().getStringExtra("usuarioID");
        myRef = database.getReference().child("usuario").child(userID);

        imagenUsuario = (CircleImageView) findViewById(R.id.imagenUsuario);
        imagenUsuario.setEnabled(false);

        nombreUsuario = (EditText) findViewById(R.id.editTextNombre);
        nombreUsuario.setEnabled(false);

        apellidosUsuario = (EditText) findViewById(R.id.editTextApellidos);
        apellidosUsuario.setEnabled(false);

        correoUsuario = (EditText) findViewById(R.id.editTextCorreo);
        correoUsuario.setEnabled(false);

        fechaUsuario = (EditText) findViewById(R.id.editFecha);
        fechaUsuario.setEnabled(false);

        sexoUsuario = (RadioGroup) findViewById(R.id.perfilSexo);
        sexoUsuario.setEnabled(false);

        editar = (Button) findViewById(R.id.boton_editar);
        cambiarPass = (Button) findViewById(R.id.boton_cambiar_pass);
        aceptar = (Button) findViewById(R.id.boton_aceptar);

        cambiarPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditarPerfil.this, DialogCambioContrasena.class));
            }
        });

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editar.setVisibility(View.GONE);
                cambiarPass.setVisibility(View.GONE);
                aceptar.setVisibility(View.VISIBLE);

                imagenUsuario.setEnabled(true);
                nombreUsuario.setEnabled(true);
                apellidosUsuario.setEnabled(true);
                fechaUsuario.setEnabled(true);
                fechaUsuario.setFocusable(false);
                sexoUsuario.setEnabled(true);

                imagenUsuario.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String[] items = new String[]{getString(R.string.galeria), getString(R.string.camara)};
                        final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery, R.drawable.ic_menu_camera};
                        ListAdapter adapter = new ArrayAdapterIconos(EditarPerfil.this, items, icons);

                        new AlertDialog.Builder(EditarPerfil.this)
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

            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarPerfil();

                editar.setVisibility(View.VISIBLE);
                cambiarPass.setVisibility(View.VISIBLE);
                aceptar.setVisibility(View.GONE);
                imagenUsuario.setEnabled(false);
                nombreUsuario.setEnabled(false);
                apellidosUsuario.setEnabled(false);
                fechaUsuario.setEnabled(false);
                fechaUsuario.setFocusable(true);
                sexoUsuario.setEnabled(false);
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                String nombreCompleto = firebaseUser.getDisplayName();
                String espacio = " ";
                String nombre = nombreCompleto.substring(0, nombreCompleto.indexOf(espacio));
                String apellidos = nombreCompleto.substring(nombreCompleto.indexOf(espacio) + 1, nombreCompleto.length());

                String imgDir = String.valueOf(firebaseUser.getPhotoUrl());
                String email = firebaseUser.getEmail();

                mostrarDatos(nombre, apellidos, imgDir, email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void editarPerfil() {
        String nombre = nombreUsuario.getText().toString();
        String apellidos = apellidosUsuario.getText().toString();
        String espacio = " ";
        final String nombreCompleto = nombre + espacio + apellidos;


        final String fechaU = fechaUsuario.getText().toString();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //creacion de usuario
        final String key = myRef.child("usuario").push().getKey();
        
        //upload de imagen
        if(imagenBoolean){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://socialsport-e98f4.appspot.com");
            imagesRef = storageRef.child("fotosPerfil").child(key + ".png");
            imagenUsuario.setDrawingCacheEnabled(true);
            imagenUsuario.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(getBaseContext(), "ALGO SALIO MAL", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nombreCompleto)
                            .setPhotoUri(downloadUrl)
                            .build();

                    firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Log.d(TAG, "User profile updated.");
                                    }
                                }
                            });

                }
            });
        }

        // myRef.child("nombre").setValue(nombreCompleto);
        myRef.child("fechaNacimiento").setValue(fechaU);
    }

    public void mostrarDatos(String nombre, String apellidos, String img, String email) {
        imgURL = img;
        nombreUsuario.setText(nombre);
        apellidosUsuario.setText(apellidos);
        correoUsuario.setText(email);
        if (img != null && !img.isEmpty()) {
            Glide.with(this).load(img).into(imagenUsuario);
        } else {
            imagenUsuario.setImageResource(R.drawable.user_rojo);
        }
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

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            usuarioDay = day;
            usuarioMonth = month;
            usuarioYear = year;
            fechaUsuario.setText(SimpleDateFormat.getDateInstance().format(new Date(year - 1900, month, day)));
        }
    }


    private void imagenCamara() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
        imagenBoolean = true;
    }

    private void pedirPermisos() {
        if (ContextCompat.checkSelfPermission(EditarPerfil.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(EditarPerfil.this,
                    Manifest.permission.CAMERA)) {

                ActivityCompat.requestPermissions(EditarPerfil.this,
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
                            imagenUsuario.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            // Handle error here
                        }
                    }
                    break;
                case CAMERA_REQUEST:
                    Bitmap photo = (Bitmap) resultData.getExtras().get("data");
                    bitmap = photo;
                    imagenUsuario.setImageBitmap(photo);
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
