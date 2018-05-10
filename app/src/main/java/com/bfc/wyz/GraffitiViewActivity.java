package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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

    static final String TAG = "ViewActivity";

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

        GraffitiView.GraffitiData graffitiData = null;
        if (getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN) instanceof GraffitiBean) {
            GraffitiBean graffitiBean = (GraffitiBean) getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN);
            graffitiData = new GraffitiView.GraffitiData(graffitiBean);

        }


        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        setContentView(R.layout.graffiti_act);

        // init resource manager
        GraffitiView.GraffitiResourcesManager.init(mDownloader);

        mGraffitiView = (GraffitiView) findViewById(R.id.graffiti_view);
        mGraffitiView.setEnabled(false);

        //select a bean
        mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());
        mGraffitiView.setOnDataChangedCallback(new GraffitiView.ICallback() {
            @Override
            public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
                Log.e(TAG, "onDataChanged -> " + graffitiView.getGraffitiData().getCurrentTotalNote());
            }

            @Override
            public void onMessage(int msg) {

            }
        });

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
            }
        }, false);

        mGraffitiView.setEnabled(true);
        mGraffitiView.installData(graffitiData);


        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setDuration(1000);
        mGraffitiView.startAnimation(scaleAnimation);

    }


    @Override
    public void onClick(View v) {
        GraffitiBean bean = new GraffitiBean(mGraffitiView.getGraffitiData());

        try {
            JSONObject jsonObject = new JSONObject(bean.toJson());
            switch (v.getId()) {
                case R.id.to_bean:
                    MonitorActivity.show(this, jsonObject.toString(2));
                    break;

                case R.id.from_bean:
                    GraffitiViewActivity.jumpToThis(this, GraffitiBean.fromJson(jsonObject.toString()));
                    break;

                case R.id.undo_last:
                    mGraffitiView.getGraffitiData().clearLayers();
                    mGraffitiView.notifyDataChanged();
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public static final void jumpToThis(Context context, GraffitiBean bean) {
        Intent intent = new Intent(context, GraffitiViewActivity.class);
        intent.putExtra(KEY_GRAFFITI_BEAN, bean);
        context.startActivity(intent);
    }

}
