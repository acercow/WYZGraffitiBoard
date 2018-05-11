package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.sina.weibo.view.graffitiview.SimpleGraffitiBitmapProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class GraffitiViewActivity extends Activity implements View.OnClickListener {

    static final String TAG = "ViewActivity";

    private GraffitiView mGraffitiView;

    static SimpleGraffitiBitmapProvider.IBitmapDownloader mDownloader = new SimpleGraffitiBitmapProvider.IBitmapDownloader() {
        @Override
        public void download(final String url, final SimpleGraffitiBitmapProvider.IBitmapDownloader.IBitmapDownloadListener listener) {
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

        @Override
        public Bitmap loadCache(String url) {
            Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(url);
            if (bitmap != null && !bitmap.isRecycled()) {
                return bitmap;
            }

            File f = ImageLoader.getInstance().getDiskCache().get(url);
            if (f != null && f.exists()) {
                bitmap = BitmapFactory.decodeFile(f.getPath());
                if (bitmap != null) {
                    ImageLoader.getInstance().getMemoryCache().put(url, bitmap);
                }
                return bitmap;
            }

            return null;
        }

    };

    static final String KEY_GRAFFITI_BEAN = "key_graffiti_bean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graffiti_act);
        mGraffitiView = (GraffitiView) findViewById(R.id.graffiti_view);
        mGraffitiView.setEnabled(false);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));

        SimpleGraffitiBitmapProvider.getInstance(mDownloader);
        SimpleGraffitiBitmapProvider.getInstance(mDownloader).download(GraffitiBean.GraffitiLayerBean.mTestUrls, new SimpleGraffitiBitmapProvider.IBitmapDownloader.IBitmapDownloadListener() {
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


        GraffitiView.GraffitiData graffitiData = null;
        if (getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN) instanceof GraffitiBean) {
            GraffitiBean graffitiBean = (GraffitiBean) getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN);
            graffitiData = new GraffitiView.GraffitiData(SimpleGraffitiBitmapProvider.getInstance(mDownloader), graffitiBean);
        } else {
            graffitiData = new GraffitiView.GraffitiData(SimpleGraffitiBitmapProvider.getInstance(mDownloader), 0);
        }

        //select a bean
        mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());
        mGraffitiView.setCallbacks(new GraffitiView.ICallback() {
            @Override
            public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
                Log.e(TAG, "onDataChanged -> " + graffitiView.getGraffitiData().getCurrentTotalNote());
            }

            @Override
            public void onMessage(int msg) {

            }
        });

        mGraffitiView.installData(graffitiData);
        mGraffitiView.setEnabled(true);

        if (graffitiData.isReadMode()) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
            scaleAnimation.setDuration(1000);
            mGraffitiView.startAnimation(scaleAnimation);
        }
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
