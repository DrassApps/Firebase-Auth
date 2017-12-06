package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistroEmailFirebase extends Activity {

    private static final String TAG = "EmailPassword";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private LinearLayout dummy_line;           // Dumy linear que ser el focus en onStart

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private String email, password;            // String que contiene el email y la pass
    private EditText emailEd, passwordEd;      // EditText de email y pass
    private TextView estado, userId;           // TextView de estado y userId del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_email_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // EditText
        emailEd = (EditText) findViewById(R.id.ed_email_em);
        passwordEd = (EditText) findViewById(R.id.ed_password_em);

        // TextView
        estado = (TextView) findViewById(R.id.ed_estado);
        userId = (TextView) findViewById(R.id.ed_userId);

        // Botones de gestion
        Button registro = (Button) findViewById(R.id.bt_registro_email);
        Button inisesion = (Button) findViewById(R.id.bt_iniciosesion_email);
        Button salirsesion = (Button) findViewById(R.id.bt_salir_email);
        Button verificarEmail = (Button) findViewById(R.id.bt_verificar_email);

        // Registrarse
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password) .addOnCompleteListener(RegistroEmailFirebase.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Si la creacion del usuario falla, damos un mensaje de error, en
                                // caso de registro correcto, obtenemos el usuario y actualizamos
                                // la vista con los datos del usuario

                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);

                                if (!task.isSuccessful()) {
                                    // No ha podido registrarse
                                    Toast.makeText(RegistroEmailFirebase.this,
                                            "fallos en el registro.",
                                            Toast.LENGTH_SHORT).show();

                                    updateUI(null);
                                }

                            }
                        });
            }
        });

        // Iniciar sesion
        inisesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                // verificar email al menos

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistroEmailFirebase.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if (user != null) {
                                    updateUI(user);
                                }
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegistroEmailFirebase.this,
                                            "Datos de acceso incorrectos.",
                                            Toast.LENGTH_SHORT).show();

                                    updateUI(null);
                                }
                            }
                        });
            }
        });

        // Cerrar Sesion
        salirsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });

        // Verificar Email, envida un email al correo para que el usuario verifique su registro
        verificarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_email);
        bt_menu_nav = (ImageView) findViewById(R.id.bt_menu_nav);

        // Configuración del NavigationView
        bundle = (NavigationView) findViewById(R.id.navview);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Añadimos el NavigationView a la vista
        if (bundle != null) {
            setupDrawerContent(bundle);
        }

        // El boton del lay abre el nav
        menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        // El botón dentro del nav, cierra el nav
        bt_menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.START);
            }
        });

        // Dumy line para que cuando se abra la aplicación el EditTet no salga focuseado
        dummy_line = (LinearLayout) findViewById(R.id.dummy_line);

        dummy_line.requestFocus();

    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario se ha registrado en Firebase (email)
            estado.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userId.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.VISIBLE);

        } else {

            // Hemos cerrado la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.signed_out);
            userId.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
        }
    }

    // Crea el Drawer
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        menuItem.getItemId();
                        return true;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Al iniciar la vista comprueba si hay un usuario activo logeado en la aplicacion
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si hay un usuario registrado activo en la aplicacion cuando se abre la vista
        // actualizamos la vista con los datos que tenemos en el firebase
        if (currentUser != null){
            updateUI(currentUser);
        }else{
            // no actualizamos
            updateUI(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Al cerrar la vista comprueba si hay algun usuario activo
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si lo hay, cerramos la sesion
        if (currentUser != null){
            mAuth.signOut();
        }else{
            updateUI(null);
        }
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void registroEmailView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
}
