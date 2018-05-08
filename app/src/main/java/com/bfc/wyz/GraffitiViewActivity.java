package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sina.weibo.view.graffitiview.GraffitiBean;
import com.sina.weibo.view.graffitiview.GraffitiView;

import org.json.JSONException;
import org.json.JSONObject;

public class GraffitiViewActivity extends Activity implements View.OnClickListener {

    static final String TAG = GraffitiViewActivity.class.getSimpleName();

    private GraffitiView mGraffitiView;

    static GraffitiView.GraffitiResourcesManager.IBitmapDownloader mDownloader = new GraffitiView.GraffitiResourcesManager.IBitmapDownloader() {
        @Override
        public void download(final String url, final GraffitiView.GraffitiResourcesManager.IBitmapManager.IBitmapListener listener) {
            ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    if (listener != null) {
                        listener.onStart(url);
                    }
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    if (listener != null) {
                        listener.onComplete(url, null, failReason != null ? failReason.getCause() : new Exception("onLoadingFailed"));
                    }
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (listener != null) {
                        listener.onComplete(url, bitmap, bitmap != null ? null : new Exception("bitmap == null ?"));
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    if (listener != null) {
                        listener.onComplete(url, null, new Exception("onLoadingCancelled"));
                    }
                }
            });
        }
    };

    static final String KEY_GRAFFITI_BEAN = "key_graffiti_bean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));

        setContentView(R.layout.graffiti_act);
        mGraffitiView = (GraffitiView) findViewById(R.id.graffiti_view);
        mGraffitiView.setEnabled(false);

        GraffitiView.GraffitiData graffitiData = null;
        if (getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN) instanceof GraffitiBean) {
            GraffitiBean graffitiBean = (GraffitiBean) getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN);
            graffitiData = new GraffitiView.GraffitiData(graffitiBean);
        }
        final GraffitiView.GraffitiData finalGraffitiData = graffitiData;

        // init resource manager
        GraffitiView.GraffitiResourcesManager.init(mDownloader);
        // load all resources
        GraffitiView.GraffitiResourcesManager.loadResources(GraffitiBean.GraffitiLayerBean.mTestUrls, new GraffitiView.GraffitiResourcesManager.IBitmapManager.IBitmapListener() {
            @Override
            public void onStart(String url) {
                Log.e(TAG, "onStart -> " + url);
            }

            @Override
            public void onComplete(String url, Bitmap bitmap, Throwable e) {
                Log.e(TAG, "onComplete -> " + url);
            }

            @Override
            public void onComplete(Throwable e) {
                Log.e(TAG, "onComplete e -> " + e);

                Toast.makeText(GraffitiViewActivity.this, "Ready to show", Toast.LENGTH_SHORT).show();

                mGraffitiView.setEnabled(true);

                mGraffitiView.installData(finalGraffitiData);

                //select a bean
                mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());
                mGraffitiView.setOnDataChangedCallback(new GraffitiView.IOnDataChangedCallback() {
                    @Override
                    public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
                        Log.e(TAG, "onDataChanged -> " + graffitiView.getGraffitiData().getCurrentTotalNote());
                    }
                });
            }
        }, false);


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
