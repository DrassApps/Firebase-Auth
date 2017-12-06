package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegistroFacebookFirebase extends Activity {

    private static final String TAG = "FacebookLogin";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private CallbackManager mCallbackManager;  // CallBack de Facebook

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private TextView estado, userId;           // Estado y userId de la sesió

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_facebook_firebase);

        //FacebookSdk.sdkInitialize(getApplicationContext());

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inicializamos el botón de login de Facebook
        mCallbackManager = CallbackManager.Factory.create();

        // TextVies
        estado = (TextView) findViewById(R.id.facebook_estado);
        userId = (TextView) findViewById(R.id.facebook_userId);

        // Logout de Facebook
        Button logoutFacebook = (Button) findViewById(R.id.bt_salir_facebook);

        // Login en Facebook
        LoginButton facebooksingin = (LoginButton) findViewById(R.id.bt_facebook);

        // Pedimos los permisos al usuario
        facebooksingin.setReadPermissions("email", "public_profile");

        // Establecemos un callback para el OAuth
        facebooksingin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Si la url ha sido correcta gestionamos el OAuth
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            // Cancelada
            @Override
            public void onCancel() {
                Log.i(TAG, "facebook:onCancel");
            }

            // Error (conexion por ejemplo)
            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "facebook:onError", error);
            }
        });


        // Cerramos la sesion de Facebook
        logoutFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });


        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_facebook);
        bt_menu_nav = (ImageView) findViewById(R.id.bt_menu_nav_facebook);

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
    }

    // Gestionamos la respues que nos envia el OAuth, le pasamos un token de usuario de Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        // Cogemos las credenciales del usuario
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        // Lo registramos en Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si el registro ha sido correcto
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // No ha podido registrarse en Firebase
                            Toast.makeText(RegistroFacebookFirebase.this, "Oups.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

    // Pasamos el resultado de la actividad al boton de login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario se ha registrado en Facebook
            estado.setText(getString(R.string.google_status_fmt, user.getEmail()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userId.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.bt_salir_facebook).setVisibility(View.VISIBLE);
        } else {
            // Hemos cerrado la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.signed_out);
            userId.setText(null);

            findViewById(R.id.bt_salir_facebook).setVisibility(View.INVISIBLE);
        }
    }

    // Crea el Drawer
    private void setupDrawerContent(NavigationView navigationView)
    {
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
        // Al iniciar la vista comprueba si hay un usuario activo logeado en la aplicación
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si hay un usuario registrado activo en la aplicación cuando se abre la vista
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

        // Si lo hay, cerramos la sesión
        if (currentUser != null){
            mAuth.signOut();
        }else{
            updateUI(null);
        }
    }

    // Métodos para pasar a las otras actividades desde el menú
    public void registroEmailView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
}
