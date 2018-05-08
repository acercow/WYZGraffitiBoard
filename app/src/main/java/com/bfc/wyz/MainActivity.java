package com.bfc.wyz;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graffiti_board);

        GraffitiManager mGraffitiManager = new GraffitiManager(findViewById(R.id.layout_graffiti_container));
        mGraffitiManager.init();
        mGraffitiManager.setOnDismissListener(new GraffitiManager.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
    }

}
