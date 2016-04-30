package de.dtsharing.dtsharing;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //<--           onCreate Start          -->
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Erzeuge Slide Navigation*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        /*Synchronisiere Button Status*/
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displayView(R.id.nav_main);
    }
    //<--           onCreate End            -->

    //<--           onBackPressed Start            -->
    @Override
    public void onBackPressed() {
        /*Aktionen die beim Drücken des Zurück Buttons ausgeführt werden*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //<--           onBackPressed End            -->

    //<--           onCreateOptionsMenu Start           -->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Füge Layout menu.main (Slide Menu) hinzu*/
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //<--           onCreateOptionsMenu End         -->

    //<--           onOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /*Settings Toggle klick*/
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           onOptionsItemSelected End         -->

    //<--           onNavigationItemSelected Start           -->
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        /*Erhalte itemId des geklickten Objektes und rufe displayView auf*/
        int id = item.getItemId();
        displayView(id);
        return true;
    }
    //<--           onNavigationItemSelected End           -->

    //<--           displayView Start           -->
    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        /*Switch Case der ItemId und somit der Aktionen nach Klick auf ein Item*/
        switch (viewId) {
            case R.id.nav_main:
                fragment = new MainFragment();
                title  = "Main";
                break;
            case R.id.nav_login:
                fragment = new LoginFragment();
                title = "Login";
                break;
            case R.id.nav_register:
                fragment = new RegisterFragment();
                title = "Register";
                break;
            case R.id.nav_database:
                fragment = new DatabaseFragment();
                title = "Database";
                break;
            case R.id.nav_location:
                fragment = new LocationFragment();
                title = "Location";
                break;

        }

        /*Falls das Fragment vorhanden ersetze den ViewContainer mit neuem Fragment*/
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_frame, fragment);
            ft.commit();
        }

        /*Ändere den Titel*/
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        /*Schließe die Slide Navigation*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }
    //<--           displayView End         -->
}
