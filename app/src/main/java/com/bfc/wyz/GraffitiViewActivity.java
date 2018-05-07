package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sina.weibo.view.graffitiview.GraffitiBean;
import com.sina.weibo.view.graffitiview.GraffitiView;

import org.json.JSONException;
import org.json.JSONObject;

public class GraffitiViewActivity extends Activity implements View.OnClickListener {

    private GraffitiView mGraffitiView;

    static final String KEY_GRAFFITI_BEAN = "key_graffiti_bean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graffiti_act);
        mGraffitiView = (GraffitiView) findViewById(R.id.graffiti_view);


        GraffitiView.GraffitiData graffitiData = null;
        if (getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN) instanceof GraffitiBean) {
            GraffitiBean graffitiBean = (GraffitiBean) getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN);
            graffitiData = new GraffitiView.GraffitiData(graffitiBean);
        }

        mGraffitiView.installData(graffitiData);

        //select a bean
        mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());

    }

    @Override
    public void onClick(View v) {
        GraffitiBean bean = new GraffitiBean(mGraffitiView.getGraffitiData());

        switch (v.getId()) {
            case R.id.to_bean:
                try {
                    JSONObject jsonObject = new JSONObject(bean.toJson());
                    MonitorActivity.show(this, jsonObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.from_bean:
                GraffitiViewActivity.jumpToThis(this, bean);
                break;

            case R.id.undo_last:

                mGraffitiView.getGraffitiData().clearLayers();
                mGraffitiView.notifyDataChanged();

                break;
        }
    }


    public static final void jumpToThis(Context context, GraffitiBean bean) {
        Intent intent = new Intent(context, GraffitiViewActivity.class);
        intent.putExtra(KEY_GRAFFITI_BEAN, bean);
        context.startActivity(intent);
    }

}
