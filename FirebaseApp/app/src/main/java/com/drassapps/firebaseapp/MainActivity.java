package com.drassapps.firebaseapp;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.twitter.sdk.android.core.TwitterCore;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;      // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;          // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void registroEmailView(View v){
        Intent i = new Intent(MainActivity.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }

    public void registroGoogleView(View v){
        Intent i = new Intent(MainActivity.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }

    public void registroFacebookView(View v){
        Intent i = new Intent(MainActivity.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }

    public void registroTwitterView(View v){
        Intent i = new Intent(MainActivity.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
}

