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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class RegistroTwitterFirebase extends Activity {

    private static final String TAG = "TwitterActivity";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private TextView estado;                   // TextView para mostrar el estado de la sesion
    private TextView userID;                   // TextView que muestrael id en firebase del usuario

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private TwitterLoginButton mLoginButton;   // LoginButton del SDK Twittwe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Necesario inicializar la autentificacion de la App con Twitter antes de inflar el Layout
        // le pasamos el id de la app que hemos creado en Twitter y la key
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_id),
                getString(R.string.twitter_key));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        // Inicializamos Twitter
        Twitter.initialize(twitterConfig);

        // Asignamos el LayOut para la clase
        setContentView(R.layout.registro_twitter_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // TextViews
        estado = (TextView) findViewById(R.id.status);
        userID = (TextView) findViewById(R.id.detail);

        // Boton de cerrar sesion
        Button logOutButton = (Button) findViewById(R.id.bt_twitter_logout);

        // Botton del SDK de Twitter
        mLoginButton = (TwitterLoginButton) findViewById(R.id.button_twitter_login);

        // Al pulsar sobre el boton de Login de Twitter nos abre la pantalla de OAuth para la
        // autentificacion con al cuenta Twitter
        mLoginButton.setCallback(new Callback<TwitterSession>() {

            // Si hay llamada a la Sesion de Twitter pasamos el metodo para gestionar el usuario
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            // Si ha habido un error, informamos al usuario
            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI(null);

            }
        });

        // Cerramos la sesion de Firebase, la sesion de Twitter y actualizamos la vista
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                updateUI(null);
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav);
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
    }

    // Manejamos la sesion del usuario
    private void handleTwitterSession(TwitterSession session) {

        // Obtenemos las credenciales del usuario registrado a través del OAuth de Twitter
        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login correcto, actualizamos la UI
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);


                        } else {
                            // Si falla el login damos un mensaje al usuario
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);

                        }
                    }
                });
    }

    // Pasamos el resultado de la actividad al boton de login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {

            // Establecemos el nombre que el usuario tiene en Twitter
            estado.setText(getString(R.string.twitter_status_fmt, user.getDisplayName()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userID.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.button_twitter_login).setVisibility(View.GONE);
            findViewById(R.id.bt_twitter_logout).setVisibility(View.VISIBLE);

        } else {
            // Cerramos la sesion y restablecemos los elementos de la interfaz
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            estado.setText(R.string.signed_out);
            userID.setText(null);

            findViewById(R.id.button_twitter_login).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_twitter_logout).setVisibility(View.GONE);
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

        // Si lo hay, cerramos la sesion y su sesion en twitter
        if (currentUser != null){
            mAuth.signOut();
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
        }else{
            updateUI(null);
        }
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void registroEmailView(View v){
        Intent i = new Intent(RegistroTwitterFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroTwitterFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(RegistroTwitterFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
}
