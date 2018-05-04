package com.bfc.wyzgraffitiboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by fishyu on 2018/5/4.
 */

public class MonitorActivity extends Activity {

    static final String KEY_CONTENT = "content";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_activity);
        TextView textView = findViewById(R.id.monitor);

        //show
        String value = getIntent().getStringExtra(KEY_CONTENT);
        textView.setText(value);
    }


    public static final void show(Context context, String content) {
        Intent intent = new Intent(context, MonitorActivity.class);
        intent.putExtra(KEY_CONTENT, content);
        context.startActivity(intent);
    }
}
