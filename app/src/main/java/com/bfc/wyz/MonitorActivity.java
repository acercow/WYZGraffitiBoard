package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.sina.weibo.view.graffitiview.GraffitiBean;

/**
 * Created by fishyu on 2018/5/4.
 */

public class MonitorActivity extends Activity {

    static final String KEY_CONTENT = "content";

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_activity);
        mEditText = findViewById(R.id.monitor);

        //showLayers
        String value = getIntent().getStringExtra(KEY_CONTENT);
        mEditText.setText(value);
    }


    public static final void show(Context context, String content) {
        Intent intent = new Intent(context, MonitorActivity.class);
        intent.putExtra(KEY_CONTENT, content);
        context.startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String value = mEditText.getText().toString();
        GraffitiViewActivity.jumpToThis(this, GraffitiBean.fromJson(value));
    }
}
