package de.dtsharing.dtsharing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class FCMTestActivity extends AppCompatActivity {

    TextView tvContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcmtest);

        tvContainer = (TextView) findViewById(R.id.tvContainer);

        if(getIntent() != null){
            String body = getIntent().getStringExtra("body"),
                    tag = getIntent().getStringExtra("tag"),
                    title = getIntent().getStringExtra("title"),
                    collapseKey = getIntent().getStringExtra("collapseKey"),
                    data = getIntent().getStringExtra("data"),
                    type = getIntent().getStringExtra("type");
            tvContainer.setText("Title: "+title+"\nBody: "+body+"\nTag: "+tag+"\nType: "+type+"\ncollapseKey: "+collapseKey+"\nData: "+data);
        }
    }
}
