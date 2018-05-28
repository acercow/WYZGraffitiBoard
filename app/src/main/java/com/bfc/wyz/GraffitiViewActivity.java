package com.bfc.wyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sina.weibo.view.graffitiview.DemoGraffitiBitmapProvider;
import com.sina.weibo.view.graffitiview.GraffitiBean;
import com.sina.weibo.view.graffitiview.GraffitiUtils;
import com.sina.weibo.view.graffitiview.GraffitiView;

import org.json.JSONException;
import org.json.JSONObject;

public class GraffitiViewActivity extends Activity implements View.OnClickListener {

    static final String TAG = GraffitiViewActivity.class.getSimpleName();

    private GraffitiView mGraffitiView;

//    static DemoGraffitiBitmapProvider.IBitmapDownloader mDownloader = new DemoGraffitiBitmapProvider.IBitmapDownloader() {
//        @Override
//        public void download(final String url, final DemoGraffitiBitmapProvider.IBitmapDownloader.IBitmapDownloadListener listener) {
//            ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String s, View view) {
//                    if (listener != null) {
//                        listener.onStart(url);
//                    }
//                }
//
//                @Override
//                public void onLoadingFailed(String s, View view, FailReason failReason) {
//                    if (listener != null) {
//                        listener.onComplete(url, null, failReason != null ? failReason.getCause() : new Exception("onLoadingFailed"));
//                    }
//                }
//
//                @Override
//                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                    if (listener != null) {
//                        listener.onComplete(url, bitmap, bitmap != null ? null : new Exception("bitmap == null ?"));
//                    }
//                }
//
//                @Override
//                public void onLoadingCancelled(String s, View view) {
//                    if (listener != null) {
//                        listener.onComplete(url, null, new Exception("onLoadingCancelled"));
//                    }
//                }
//            });
//        }
//
//    };


    static DemoGraffitiBitmapProvider.IBitmapDownloader mDownloader = new DemoGraffitiBitmapProvider.IBitmapDownloader() {
        @Override
        public void download(final String url, final IBitmapDownloadListener listener) {

            final ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(ImageRequest.fromUri(url), null);

            if (listener != null) {
                listener.onStart(url);
            }

            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                protected void onNewResultImpl(Bitmap bitmap) {
                    if (listener != null) {
                        listener.onComplete(url, bitmap, bitmap != null ? null : new Exception("bitmap == null ?"));
                    }
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    if (listener != null) {
                        listener.onComplete(url, null, new Exception("onFailureImpl"));
                    }
                }

            }, UiThreadImmediateExecutorService.getInstance());
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
        Fresco.initialize(this);

        DemoGraffitiBitmapProvider.getInstance(mDownloader);
        DemoGraffitiBitmapProvider.getInstance(mDownloader).download(GraffitiBean.GraffitiLayerBean.mTestUrls, new DemoGraffitiBitmapProvider.IBitmapDownloader.IBitmapDownloadListener() {
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
            graffitiData = new GraffitiView.GraffitiData(DemoGraffitiBitmapProvider.getInstance(mDownloader), graffitiBean);
        } else {
            graffitiData = new GraffitiView.GraffitiData(DemoGraffitiBitmapProvider.getInstance(mDownloader), 0, true);
        }

        //select a bean
        mGraffitiView.setDrawObject(GraffitiBean.GraffitiLayerBean.buildTest());
        mGraffitiView.setCallbacks(new GraffitiView.ICallback() {
            @Override
            public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject, int noteNumber) {
                Log.e(TAG, "onDataChanged -> " + noteNumber);
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
            GraffitiUtils.startGraffitiAnim(mGraffitiView);
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
