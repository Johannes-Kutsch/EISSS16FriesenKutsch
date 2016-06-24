package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Process;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.database.sqlite.SQLiteCursor;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    ViewPager viewPager;
    CoordinatorLayout mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContent = (CoordinatorLayout) findViewById(R.id.main_content);

        /*Sichere die Empfangenen Daten in Variablen*/
        if (getIntent().getBooleanExtra("cameFromLogin", false)) {
            Intent loginIntent = getIntent();
            String user_id = loginIntent.getStringExtra("user_id"),
                    picture = loginIntent.getStringExtra("picture"),
                    firstName = loginIntent.getStringExtra("firstName"),
                    lastName = loginIntent.getStringExtra("lastName"),
                    interests = loginIntent.getStringExtra("interests"),
                    more = loginIntent.getStringExtra("more");

            SharedPrefsManager spm = new SharedPrefsManager(this);
            spm.setLoggedInSharedPrefs(user_id, picture, firstName, lastName, interests, more);

            getIntent().removeExtra("cameFromLogin");
        }

        /* Custom Toolbar und Title werden erfasst */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        /* Custom Toolbar wird gesetzt */
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {

            /* Deaktiviere Titel da Custom Titel */
            actionBar.setDisplayShowTitleEnabled(false);
        }

        /* ViewPager wird mit Fragmenten gefüllt. Tabs werden erzeugt */
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        /* Aktuelle Seite wird auf 1 (mitte) gesetzt. Animation deaktiviert */
        viewPager.setCurrentItem(1, false);

        /* Tabs werden in die Toolbar eingebettet */
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        if (tabs != null) {
            tabs.setupWithViewPager(viewPager);
        }

        /* Ein Trip wurde eingetragen und es wird zur MainActivity zurückgekehrt */
        if(getIntent().getBooleanExtra("trip_created", false)){

            /* Setze den ViewPager auf Seite 2 (Fahrten) */
            viewPager.setCurrentItem(2, true);

            /* Erzeuge positive Snackbar mit Message für den Benutzer */
            Snackbar snackbar = Snackbar.make(mainContent, "Trip erfolgreich angelegt\nDu wirst benachrichtig sobald sich ein Match findet", Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            snackbar.show();

        }

        /* Benutzer hat sich Erfolgreich gematcht und kehrt zur MainActivity zurück */
        if(getIntent().getBooleanExtra("matching_success", false)){

            /* Setze den ViewPager auf Seite 0 (Chats) */
            viewPager.setCurrentItem(0, true);

            /* Erhalte MatchName aus den Intent Extras */
            String matchName = getIntent().getStringExtra("matchName");

            /* Erzeuge positive Snackbar mit Message für den Benutzer */
            Snackbar snackbar = Snackbar.make(mainContent, "Du hast dich erfolgreich bei "+matchName+" eingetragen", Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            snackbar.show();

        }

    }

    /* Menü wird erzeugt */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Füge Settings Ressource hinzu (Profil, Einstellungen, Abmelden) */
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* Menü onClick Listener */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){

            /* Settings wurde gewählt */
            case R.id.action_settings:
                break;

            /* Profil wurde gewählt */
            case R.id.action_profile:

                /* EditProfile Activity wird gestartet */
                Intent editProfileIntent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(editProfileIntent);
                break;

            /* Abmelden wurde gewählt */
            case R.id.action_signout:

                /* Die LoginActivity wird gestartet. Alle anderen Activitiys werden geschlossen */
                Intent mainIntent = new Intent(getApplicationContext(), LoginActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    /* Hardware Zurück Button wird gedrückt */
    @Override
    public void onBackPressed() {
        /* Wenn das Suchmaske Fragment ausgewählt ist und zurück gedrückt wird => finish() Activity */
        if (viewPager.getCurrentItem() == 1) {
            super.onBackPressed();

        /* Sonst geh zurück zum Suchmaske Fragment */
        } else {
            viewPager.setCurrentItem(1, true);
        }
    }

    /* Der Adapter wird mit den Fragmenten samt Titel gefüllt und anschließend mit dem viewPager verbunden */
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "CHATS");
        adapter.addFragment(new SuchmaskeFragment(), "SUCHMASKE");
        adapter.addFragment(new FahrtenFragment(), "FAHRTEN");
        viewPager.setAdapter(adapter);
    }

    /* Custom Adapter für den ViewPager */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        /* Gibt Fragment an der Position aus */
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        /* Gibt die Größe der Fragment Liste aus */
        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /* Füge Fragment zu List<Fragment> und Titel zu List<String> hinzu */
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        /* Gib Titel an der Position in der Liste aus */
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
