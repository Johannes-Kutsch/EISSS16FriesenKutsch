package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private final static int RESULT_LOAD_IMG = 1;
    String imgDecodableString, base_url;

    EditText _interests, _more;
    ImageView _profilePicture;
    ImageButton _editPicture;
    Button _submit;
    CoordinatorLayout _mainContent;

    ContentValues profileData;

    String newPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        /* Die base_url wird sowie die EditProfileDaten werden aus den SharedPrefs bezogen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(EditProfileActivity.this);
        base_url = sharedPrefsManager.getBaseUrl();
        profileData = sharedPrefsManager.getEditProfileSharedPrefs();
        newPicture = profileData.getAsString("picture");

        /* Erfassen der Toolbar und title Views */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        /* Setzen der Custom Toolbar */
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /*Erfassen der Views mit denen interagiert werden soll*/
        _interests = (EditText) findViewById(R.id.etInterests);
        _more = (EditText) findViewById(R.id.etMore);
        _editPicture = (ImageButton) findViewById(R.id.bEditPicture);
        _submit = (Button) findViewById(R.id.bSubmit);
        _profilePicture = (ImageView) findViewById(R.id.ivProfilePicture);
        _mainContent = (CoordinatorLayout) findViewById(R.id.main_content);

        /* EditTexts werden mit den derzeitigen Daten gefüllt */
        _interests.setText(profileData.getAsString("interests"));
        _more.setText(profileData.getAsString("more"));

        /* Besitzt der Benutzer kein Bild ist dieses "null" und somit wird ein default picture gewählt */
        if(profileData.getAsString("picture").equals("null")) {
            int placeholder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_96dp", null, null);
            _profilePicture.setImageResource(placeholder);

        /* Besitzt der Benutzer ein Bild wird dieses in eine rundes Drawable umgewandelt. Als Bildquelle wird die decodeBase64 Methode angegeben, welche als Parameter den String
         * des Bildes enthält und somit eine Bitmap darstellt. Anschließend wird das Bild gesetzt */
        }else {
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(profileData.getAsString("picture")));
            roundDrawable.setCircular(true);
            _profilePicture.setImageDrawable(roundDrawable);
        }

        /* onClick für den editPicture Button */
        _editPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Es wird ein Picker Intent gestartet, welches Medien von der SD Karte auswählen lassen soll */
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });

        /* onClick für den submit Button. Es wird überprüft ob Informationen verändert wurden und nur bei Änderungen das Profil aktualisiert */
        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final boolean   interestsChanged = !profileData.getAsString("interests").equals(_interests.getText().toString()),
                                moreChanged      = !profileData.getAsString("more").equals(_more.getText().toString()),
                                pictureChanged   = !profileData.getAsString("picture").equals(newPicture);

                /* Falls mind. eine Information verändert wurde */
                if(interestsChanged || moreChanged || pictureChanged) {

                    /* Zeige einen AlertDialog um eine Bestätigung des Benutzers zu erhalten */
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(EditProfileActivity.this, R.style.AppTheme_Dialog_Alert);

                    builder.setMessage("Möchtest du dein Profil wirklich aktualisieren?");
                    builder.setPositiveButton("Aktualisieren", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            /* Wird der Dialog positiv beantwortet starte den PUT-Request an den Server */
                            updateProfile(interestsChanged, moreChanged, pictureChanged);

                        }

                    });

                    builder.setNegativeButton("Abbruch", null);
                    builder.show();

                /* Wurde keine Information geändert */
                }else{

                    /* Weise den Benutzer darauf hin, dass keine Änderungen vorgenommen wurden */
                    Snackbar snackbar = Snackbar.make(_mainContent, "Es wurden keine Änderungen vorgenommen", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    snackbar.show();

                }

            }
        });

    }

    /* PUT-Request an den Server um die Benutzerdaten zu aktualisieren */
    private void updateProfile(final boolean interestsChanged, final boolean moreChanged, final boolean pictureChanged){

        String url = base_url+"/users/"+profileData.getAsString("user_id");


        StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        /* Bei Erfolgreicher Response des Servers */
                        if(response.contains("success_message")) {

                            /* Daten in den SharedPrefs aktualisieren */
                            new SharedPrefsManager(getApplicationContext()).setEditProfileSharedPrefs(newPicture, _interests.getText().toString(), _more.getText().toString());

                            /* Den Benutzer durch eine positive Snackbar darauf hinweisen, dass sein Profil aktualisiert wurde */
                            Snackbar snackbar = Snackbar.make(_mainContent, "Profil wurde aktualisiert", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null);
                            View snackBarView = snackbar.getView();
                            snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.positive));
                            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            snackbar.show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        })
        {
            /*Daten welche der PUT-Request mitgegeben werden*/
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                if (interestsChanged) params.put("interests", _interests.getText().toString());
                if (moreChanged) params.put("more", _more.getText().toString());
                if (pictureChanged) params.put("picture", newPicture);
                Log.d("EditProfileActivity", "Parameter: "+params);
                return params;
            }

        };

        /* Request wird der Volley Queue angehangen und ausgeführt */
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);

    }

    /* Bild wird skaliert, sodass es in einen Rahmen von 512x512 passt und anschließend gecropped, sodass es quadratisch ist. Der mittlere Bildausschnitt steht dabei
     * im Fokus. Die Größe von 512x512 wurde gewählt, da man somit bei der Umwandlung einen kürzeren String erhält. Ein weiterer Punkt ist, dass die Bilder momentan nicht
     * im Vollbild dargestellt werden können und eine kleine Auflösung für die Profilbilder somit ausreichend ist*/
    public Bitmap centerCropImage(Bitmap bitmap){

        /*http://stackoverflow.com/a/30609107   Hat als Inspiration gedient, jedoch Nullpointerexception mit unterschiedlichen Galerie Apps
        * http://stackoverflow.com/a/20177611   Hat die Nullpointerexception "behoben"
        * Bild wird auf maximal 1024x1024 runter skaliert (aspect ratio wird beachtet)*/
        int maxHeight = 512;
        int maxWidth = 512;
        float scale = Math.min(((float)maxHeight / bitmap.getWidth()), ((float)maxWidth / bitmap.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        /*http://stackoverflow.com/a/6909144
        * Bild wird gecropped (centered)*/
        if (bitmap.getWidth() >= bitmap.getHeight()){

            bitmap = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth()/2 - bitmap.getHeight()/2,
                    0,
                    bitmap.getHeight(),
                    bitmap.getHeight()
            );

        }else{

            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),
                    bitmap.getWidth()
            );
        }
        return bitmap;
    }

    /* onActivityResult wird benötigt, da beim onClick auf editPicture ein MediaPicker gestartet wird, dessen result
     * hier erhält und welches weiterverarbeitet wird */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // http://stackoverflow.com/a/30004714
        /* Entspricht der requestCode dem des MediaPickers, ist der resultCode positiv und data ungleich null
         * wurde ein Bild gewählt welches nun weiter behandelt werden kann */
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {

            /* Der Pfad des Bildes wird extrahiert */
            Uri pickedImage = data.getData();

            /* Es wird ein InputStream erstellt, aus welchem das Bild bezogen werden kann */
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(pickedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /* Es wird eine Bitmap aus dem InputStream decodiert und erstmals in einer Variable festgehalten */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);

            /* Das Bild wird anschließend auf eine maximale Größe von 512x512 gebracht und center cropped */
            bitmap = centerCropImage(bitmap);
            /* Zusätzlich wird das Bild bereits in Base64 encodiert und der Variable newPicture zugewiesen, welche zuvor das alte Bild beinhaltet hat */
            newPicture = EncodeDecodeBase64.encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 80);

            /* Das neue Bild wird anschließend abgerundet und der ImageView zugewiesen */
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundDrawable.setCircular(true);
            _profilePicture.setImageDrawable(roundDrawable);
        }
    }


    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->
}
