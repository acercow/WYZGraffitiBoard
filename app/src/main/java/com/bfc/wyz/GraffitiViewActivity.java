package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
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

public class GraffitiViewActivity extends Activity implements View.OnClickListener {

    static final String TAG = GraffitiViewActivity.class.getSimpleName();

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
                Toast.makeText(GraffitiViewActivity.this, "Ready to showLayers? e -> " + e, Toast.LENGTH_SHORT).show();
            }
        }, false);


        GraffitiView.GraffitiData graffitiData = null;
        if (getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN) instanceof String) {
            String string = (String) getIntent().getSerializableExtra(KEY_GRAFFITI_BEAN);
            GraffitiBean graffitiBean = GraffitiBean.fromJson(string);
            graffitiData = new GraffitiView.GraffitiData(SimpleGraffitiBitmapProvider.getInstance(mDownloader), graffitiBean);
        } else {
            graffitiData = new GraffitiView.GraffitiData(SimpleGraffitiBitmapProvider.getInstance(mDownloader), 0, true);
        }

        //select a bean
        mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());
        mGraffitiView.setCallbacks(new GraffitiView.ICallback() {
            @Override
            public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
                Log.e(TAG, "onDataChanged -> " + graffitiView.getGraffitiData().getCurrentNoteTotalNumber());
            }

            @Override
            public void onMessage(int msg) {
                switch (msg) {
                    case GraffitiView.ICallback.MSG_GRAFFITI_DATA_VIEW_INSTALLED:
                        Log.e(TAG, " write padding -> " + mGraffitiView.getGraffitiData().getWritePaddingRectF());
                        break;
                }
            }
        });

        mGraffitiView.installData(graffitiData);
        mGraffitiView.setEnabled(true);

        if (graffitiData.isReadMode()) {

            AnimationSet animationSet = new AnimationSet(true);

            ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 1f);
            scaleAnimation.setDuration(1500);
            scaleAnimation.setInterpolator(new DecelerateInterpolator());

            AlphaAnimation fadeInAnimation = new AlphaAnimation(0F, 1F);
            fadeInAnimation.setDuration(1500);
            fadeInAnimation.setFillAfter(true);

            AlphaAnimation fadeOutAnimation = new AlphaAnimation(1F, 0F);
            fadeOutAnimation.setDuration(500);
            fadeOutAnimation.setFillAfter(true);
            fadeOutAnimation.setStartOffset(2500);

            Animation trembleAnimation = new AlphaAnimation(1F, 1F);
            trembleAnimation.setDuration(1100);
            trembleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mGraffitiView.getGraffitiData().startAnimationsIfExits();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(fadeInAnimation);
            animationSet.addAnimation(fadeOutAnimation);
            animationSet.addAnimation(trembleAnimation);


            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mGraffitiView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mGraffitiView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mGraffitiView.startAnimation(animationSet);

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
                    GraffitiViewActivity.jumpToThis(this, jsonObject.toString());
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


    public static final void jumpToThis(Context context, String bean) {
        Intent intent = new Intent(context, GraffitiViewActivity.class);
        intent.putExtra(KEY_GRAFFITI_BEAN, bean);
        context.startActivity(intent);
    }

}
