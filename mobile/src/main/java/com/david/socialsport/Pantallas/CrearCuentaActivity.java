package com.david.socialsport.Pantallas;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.david.socialsport.Adapters.ArrayAdapterIconos;
import com.david.socialsport.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Actividad que crea una cuenta con base en correo electrónico y contraseña, utilizando
 * Firebase.
 *
 * @author warrior.minds
 */
public class CrearCuentaActivity extends AppCompatActivity {

    // Variables que utiliza Firebase.
    /**
     * FirebaseAuth es el objeto que contiene el listener que escucha los cambios en la cuenta
     * y con el que se puede crear una cuenta nueva con correo electrónico y contraseña.
     */
    private FirebaseAuth autenticacionFirebase;
    /**
     * El listener escucha cambios en la cuenta. Cuando se crea una cuenta de manera exitosa,
     * se ejecuta el método onAuthStateChanged y se obtiene un FirebaseUser != null. Si el FirebaseUser
     * es null, quiere decir que no se inició sesión con el nuevo usuario.
     */
    private FirebaseAuth.AuthStateListener listenerAutenticacion;

    // Vistas
    private AutoCompleteTextView emailUsuario;
    private EditText nombre, apellidos, password, confirmarPassword;
    CircleImageView imagenUsuario;
    private View cargando;
    private View formaDeLogin;
    private Button botonCrearCuenta;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private static final int OPEN_REQUEST_CODE = 41;
    private static final int CAMERA_REQUEST = 1888;

    boolean imagenBoolean;
    Bitmap bitmap;
    StorageReference imagesRef;

    String email;

    private int MIS_PERMISOS_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        inicializarVistas();
        inicializarAutenticacion();

    }

    /**
     * Se necesita agregar el listener de Firebase en onStart().
     */
    @Override
    public void onStart() {
        super.onStart();
        autenticacionFirebase.addAuthStateListener(listenerAutenticacion);
    }

    /**
     * Quitar el listener antes de salir de la actividad.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (listenerAutenticacion != null) {
            autenticacionFirebase.removeAuthStateListener(listenerAutenticacion);
        }
    }

    /**
     * Método en el cual se inicializa Firebase.
     */
    private void inicializarAutenticacion() {
        /**
         * Obtener la instancia de FirebaseAuth.
         */
        autenticacionFirebase = FirebaseAuth.getInstance();

        /**
         * Crear el listener para escuchar los cambios en la cuenta.
         */
        listenerAutenticacion = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /**
                 * Obtener el usuario actual.
                 */
                FirebaseUser usuario = firebaseAuth.getCurrentUser();

                /**
                 * Si el usuario != null, quiere decir que se tiene una sesión iniciada. Si no,
                 * quiere decir que no se pudo iniciar sesión, o no se ha iniciado sesión aún.
                 */
                if (usuario != null) {
                    Toast.makeText(CrearCuentaActivity.this, "Usuario: " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                    deshabilitarCampos();
                    asignarUsuario(usuario);
                } else {
                    Toast.makeText(CrearCuentaActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Se inicializan todas las vistas de la actividad.
     */
    private void inicializarVistas() {
        configurarActionBar();
        nombre = (EditText) findViewById(R.id.loginNombre);
        apellidos = (EditText) findViewById(R.id.loginApellidos);
        password = (EditText) findViewById(R.id.loginPass);
        confirmarPassword = (EditText) findViewById(R.id.loginConfirmarPass);
        botonCrearCuenta = (Button) findViewById(R.id.boton_registro);
        emailUsuario = (AutoCompleteTextView) findViewById(R.id.loginEmail);
        emailUsuario.setAdapter(agregarEmailsAutocompletar());

        imagenUsuario = (CircleImageView) findViewById(R.id.imagenUsuario);
        Glide.with(this).load(R.drawable.user_rojo).into(imagenUsuario);

        imagenUsuario.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{getString(R.string.galeria), getString(R.string.camara)};
                final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery, R.drawable.ic_menu_camera};
                ListAdapter adapter = new ArrayAdapterIconos(CrearCuentaActivity.this, items, icons);

                new AlertDialog.Builder(CrearCuentaActivity.this)
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


        /**
         * Este Observador nos ayuda a saber si el usuario puso un email válido y ambas
         * contraseñas son iguales y con una longitud correcta.
         *
         * Si se cumplen los requisitos, se activa el botón para crear la cuenta.
         */
        TextWatcher observadorTexto = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!esPasswordValido(password.getText().toString())) {
                    password.setError(getString(R.string.passwords_tamaño));
                }

                if (!TextUtils.equals(password.getText().toString(), confirmarPassword.getText().toString())) {
                    confirmarPassword.setError(getString(R.string.passwords_son_diferentes));
                }

                if (esFormaValida(emailUsuario.getText().toString(), password.getText().toString(), confirmarPassword.getText().toString())) {
                    botonCrearCuenta.setEnabled(true);
                    confirmarPassword.setError(null);
                } else {
                    botonCrearCuenta.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        password.addTextChangedListener(observadorTexto);
        confirmarPassword.addTextChangedListener(observadorTexto);

        botonCrearCuenta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imagenBoolean && !TextUtils.isEmpty(emailUsuario.getText().toString()) && !TextUtils.isEmpty(password.getText().toString()))
                    crearCuenta(emailUsuario.getText().toString(), password.getText().toString());
                else
                    Toast.makeText(getApplicationContext(),"Falta algún campo", Toast.LENGTH_LONG).show();
            }
        });

        formaDeLogin = findViewById(R.id.forma_login);
        cargando = findViewById(R.id.progreso_login);

    }


    private void imagenCamara() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
        imagenBoolean = true;
    }

    private void pedirPermisos() {
        if (ContextCompat.checkSelfPermission(CrearCuentaActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(CrearCuentaActivity.this,
                    Manifest.permission.CAMERA)) {

                ActivityCompat.requestPermissions(CrearCuentaActivity.this,
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

    //Boton para ir atrás
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    /**
     * Crear una cuenta utilizando email y password utilizando Firebase.
     *
     * @param email
     * @param password
     */
    private void crearCuenta(String email, String password) {


       // if(!email.isEmpty()||!password.isEmpty()) {
            // Mostrar círculo de progreso y esconder los campos.
            mostrarProgreso(true);
            /**
             * Este método se utiliza para crear una cuenta con email y password. Se agrega un
             * onCompleteListener que nos indica si la creación de la cuenta fue exitosa.
             *
             * Si la creación de cuenta fue exitosa, se manda llamar el listener, en donde se puede
             * obtener el usuario creado.
             */
        autenticacionFirebase.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mostrarProgreso(false);

                        final String key = myRef.child("usuario").push().getKey();
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
                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombre.getText().toString() + " " + apellidos.getText().toString())
                                        .setPhotoUri(downloadUrl)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //Log.d(TAG, "User profile updated.");

                                                    enviarVerificacionEmail();

                                                }
                                            }
                                        });
                            }
                        });


                        if (!task.isSuccessful()) {
                            Toast.makeText(CrearCuentaActivity.this, "Hubo un error", Toast.LENGTH_SHORT).show();
                        } else {
                            // Deshabilita los campos si la creación fue exitosa, para evitar que se repita el proceso.
                            deshabilitarCampos();


                            AlertDialog.Builder dialog = new AlertDialog.Builder(CrearCuentaActivity.this);

                            dialog.setMessage(getString(R.string.hemos_enviado) + " " + getEmail().toString() + " " + getString(R.string.revise_bandeja))
                                    .setNeutralButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
     //   }else{
       //     Toast.makeText(CrearCuentaActivity.this, "Debe rellenar todos los campos ", Toast.LENGTH_SHORT).show();

        //}
    }

    public void enviarVerificacionEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                            }
                        }
                    });

        }
    }

    /**
     * Agrega el botón para regresar desde la Action Bar.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void configurarActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Este método nos indica si los valores de los campos son válidos.
     *
     * @param email
     * @param password
     * @param confirmarPassword
     * @return true si la forma es válida.
     */

    private boolean esFormaValida(String email, String password, String confirmarPassword) {
        if (esEmailValido(email) && esPasswordValido(password) && TextUtils.equals(password, confirmarPassword)) {
            return true;
        }
        return false;
    }

    /**
     * Este método nos indica si el email es válido. Por ahora solamente checa si contiene @.
     * Se podría utilizar una expresión regular.
     *
     * @param email
     * @return true si el email tiene un formato válido.
     */
    private boolean esEmailValido(String email) {
        return email.contains("@");
    }

    /**
     * Método que checa si el password es válido. Por ahora solamente checa que sea mayor a 5 caracteres.
     * Firebase pide que al menos sean 6 caracteres en el password.
     *
     * @param password
     * @return true si el password es válido.
     */
    private boolean esPasswordValido(String password) {
        return password.length() > 5;
    }

    /**
     * Este método esconde todos los campos para mostrar un círculo de progreso. De igual manera,
     * esconde el círculo de progreso y muestra de vuelta los campos.
     *
     * @param mostrar true si se muestra el círculo de progreso y se esconden los campos.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void mostrarProgreso(final boolean mostrar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
            formaDeLogin.animate().setDuration(shortAnimTime).alpha(
                    mostrar ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
                }
            });

            cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            cargando.animate().setDuration(shortAnimTime).alpha(
                    mostrar ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Método que obtiene los emails de las cuentas asociadas al teléfono. El usuario debe de haber
     * aceptado el permiso de Cuentas.
     *
     * @return adapter con las cuentas de email disponibles en el teléfono.
     */
    private ArrayAdapter<String> agregarEmailsAutocompletar() {
        /**
         * Si no se ha aceptado el permiso, regresar un adapter sin cuentas.
         */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        }

        /**
         * Obtener cuentas.
         */
        Account[] cuentas = AccountManager.get(this).getAccounts();
        List<String> emails = new ArrayList<>();

        if (cuentas != null && cuentas.length > 0) {
            for (Account account : cuentas) {
                if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                    emails.add(account.name);
                }
            }
        }

        return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emails);
    }


    /**
     * Método que deshabilita todos los campos.
     */
    private void deshabilitarCampos() {
        imagenUsuario.setEnabled(false);
        nombre.setEnabled(false);
        apellidos.setEnabled(false);
        emailUsuario.setEnabled(false);
        password.setEnabled(false);
        confirmarPassword.setEnabled(false);
        botonCrearCuenta.setEnabled(false);
    }

    /**
     * Método que obtiene el email del usuario creado y lo asigna a una text view.
     *
     * @param usuarioFirebase
     */
    private void asignarUsuario(FirebaseUser usuarioFirebase) {
        emailUsuario.setText(usuarioFirebase.getEmail());
        setEmail(usuarioFirebase.getEmail());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

