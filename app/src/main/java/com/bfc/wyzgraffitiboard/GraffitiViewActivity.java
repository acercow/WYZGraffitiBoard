package com.bfc.wyzgraffitiboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bfc.wyzgraffitiboard.view.GraffitiView;

public class GraffitiViewActivity extends AppCompatActivity {

    private GraffitiView mGraffitiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graffiti_act);

        mGraffitiView = (GraffitiView) findViewById(R.id.graffiti_view);
        mGraffitiView.init(null);
    }
}
