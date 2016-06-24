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

        base_url = new SharedPrefsManager(EditProfileActivity.this).getBaseUrl();

        /*Adding Toolbar to Main screen*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        profileData = new SharedPrefsManager(getApplicationContext()).getEditProfileSharedPrefs();
        newPicture = profileData.getAsString("picture");

        /*Erfassen der Views mit denen interagiert werden soll*/
        _interests = (EditText) findViewById(R.id.etInterests);
        _more = (EditText) findViewById(R.id.etMore);
        _editPicture = (ImageButton) findViewById(R.id.bEditPicture);
        _submit = (Button) findViewById(R.id.bSubmit);
        _profilePicture = (ImageView) findViewById(R.id.ivProfilePicture);
        _mainContent = (CoordinatorLayout) findViewById(R.id.main_content);

        _interests.setText(profileData.getAsString("interests"));
        _more.setText(profileData.getAsString("more"));

        if(profileData.getAsString("picture").equals("null")) {
            int placeholder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_96dp", null, null);
            _profilePicture.setImageResource(placeholder);
        }else {
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(profileData.getAsString("picture")));
            roundDrawable.setCircular(true);
            _profilePicture.setImageDrawable(roundDrawable);
        }

        _editPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });

        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final boolean   interestsChanged = !profileData.getAsString("interests").equals(_interests.getText().toString()),
                                moreChanged      = !profileData.getAsString("more").equals(_more.getText().toString()),
                                pictureChanged   = !profileData.getAsString("picture").equals(newPicture);

                if(interestsChanged || moreChanged || pictureChanged) {

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(EditProfileActivity.this, R.style.AppTheme_Dialog_Alert);

                    builder.setMessage("Möchtest du dein Profil wirklich aktualisieren?");
                    builder.setPositiveButton("Aktualisieren", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            updateProfile(interestsChanged, moreChanged, pictureChanged);

                        }

                    });

                    builder.setNegativeButton("Abbruch", null);
                    builder.show();
                }else{

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

    private void updateProfile(final boolean interestsChanged, final boolean moreChanged, final boolean pictureChanged){

        String url = base_url+"/users/"+profileData.getAsString("user_id");


        StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.contains("success_message")) {
                            new SharedPrefsManager(getApplicationContext()).setEditProfileSharedPrefs(newPicture, _interests.getText().toString(), _more.getText().toString());

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
            /*Daten welche der Post-Request mitgegeben werden*/
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

        Volley.newRequestQueue(getApplicationContext()).add(postRequest);

    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // http://stackoverflow.com/a/30004714
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {

            Uri pickedImage = data.getData();

            InputStream imageStream = null;

            try {
                imageStream = getContentResolver().openInputStream(pickedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);

            Bitmap croppedImage = centerCropImage(bitmap);
            newPicture = EncodeDecodeBase64.encodeToBase64(croppedImage, Bitmap.CompressFormat.JPEG, 80);

            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), croppedImage);
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
