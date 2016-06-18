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

        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Setting ViewPager for each Tabs
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(1, false);

        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        if (tabs != null) {
            tabs.setupWithViewPager(viewPager);
        }

        getOverflowMenu();

        if(getIntent().getBooleanExtra("trip_created", false)){

            viewPager.setCurrentItem(2, true);

            Snackbar snackbar = Snackbar.make(mainContent, "Trip erfolgreich angelegt\nDu wirst benachrichtig sobald sich ein Match findet", Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            snackbar.show();

        }
        if(getIntent().getBooleanExtra("matching_success", false)){

            viewPager.setCurrentItem(2, true);

            String matchName = getIntent().getStringExtra("matchName");

            Snackbar snackbar = Snackbar.make(mainContent, "Du hast dich erfolgreich bei "+matchName+" eingetragen", Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            snackbar.show();

        }

    }

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Füge Layout menu.main (Slide Menu) hinzu*/
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /*Settings Toggle klick*/
        switch (id){
            case R.id.action_settings:
                break;
            case R.id.action_profile:
                Intent editProfileIntent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(editProfileIntent);
                break;
            case R.id.action_signout:
                Intent mainIntent = new Intent(getApplicationContext(), LoginActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            /*Wenn das SuchmaskeFragment ausgewählt ist und zurück gedrückt wird => finish() Activity*/
            super.onBackPressed();
        } else {
            /*Sonst geh zurück zur SuchmaskeFragment*/
            viewPager.setCurrentItem(1, true);
        }
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "CHATS");
        adapter.addFragment(new SuchmaskeFragment(), "SUCHMASKE");
        adapter.addFragment(new FahrtenFragment(), "FAHRTEN");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
