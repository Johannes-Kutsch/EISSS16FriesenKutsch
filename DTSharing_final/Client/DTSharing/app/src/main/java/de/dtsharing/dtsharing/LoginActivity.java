package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    Adapter adapter;
    android.support.v7.app.ActionBar actionBar;
    ViewPager viewPager;
    Toolbar toolbar;
    TextView toolbar_title;
    ProgressDialog progressDialog;
    Snackbar snackbar;

    SharedPreferences prefs;

    private MyStationStatusReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Thread myThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                new MyFirebaseInstanceIDService().onTokenRefresh();
            }
        });
        myThread2.start();

        prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        new SharedPrefsManager(getApplicationContext()).setLoggedOutSharedPrefs();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Setting ViewPager for each Tabs
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(1, false);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != 1) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeButtonEnabled(true);
                    toolbar_title.setText(adapter.getPageTitle(position).toString());
                    toolbar_title.setGravity(Gravity.LEFT);
                } else {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setHomeButtonEnabled(false);
                    toolbar_title.setText(adapter.getPageTitle(position).toString());
                    toolbar_title.setGravity(Gravity.CENTER);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                final String message = new SharedPrefsManager(getApplicationContext()).getStopsVersion() == 0 ? "DTSharing wird für die erste Verwendung vorbereitet" : "Fahrplandaten werden aktualisiert";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(LoginActivity.this,
                                R.style.AppTheme_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    }
                });

                IntentFilter filter = new IntentFilter(MyStationStatusReceiver.STATUS_RESPONSE);
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                receiver = new MyStationStatusReceiver();
                registerReceiver(receiver, filter);

                Intent databaseServiceIntent = new Intent(LoginActivity.this, DatabaseStationService.class);
                startService(databaseServiceIntent);
            }
        });
        myThread.start();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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

    public void setCurrentPage(int page) {
        viewPager.setCurrentItem(page, true);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ForgotPasswordFragment(), "Kennwort vergessen");
        adapter.addFragment(new LoginFragment(), "DTSharing");
        adapter.addFragment(new SignupFragment(), "Registrieren");
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

    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                viewPager.setCurrentItem(1, true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->


    public class MyStationStatusReceiver extends BroadcastReceiver {

        public static final String STATUS_RESPONSE = "de.dtsharing.dtsharing.intent.action.STATUS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean statusBoolean = intent.getBooleanExtra("finished", false),
                    errorBoolean = intent.getBooleanExtra("error", false);
            if (statusBoolean || errorBoolean) {
                progressDialog.dismiss();
                unregisterReceiver(receiver);
                if (errorBoolean){
                    snackbar = Snackbar.make(findViewById(R.id.viewpager), "Keine Verbindung zum Server möglich", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    snackbar.show();
                }
            }
        }
    }
}
