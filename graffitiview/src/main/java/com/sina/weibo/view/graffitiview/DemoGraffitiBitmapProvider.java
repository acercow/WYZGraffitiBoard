package com.sina.weibo.view.graffitiview;

import android.accounts.NetworkErrorException;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fishyu on 2018/5/10.
 * <p>
 * <p>
 * <p>
 * Demo implement for {@link GraffitiView.IBitmapProvider}
 * <p>
 * <p>
 * 1,Network download
 * 2,Memory cache
 * 3,Disk cache coming soon
 */

public class DemoGraffitiBitmapProvider implements GraffitiView.IBitmapProvider {

    private static final String TAG = DemoGraffitiBitmapProvider.class.getSimpleName();

    /**
     * Bitmap downloader
     */
    public interface IBitmapDownloader {


        /**
         * Download listener
         */
        interface IBitmapDownloadListener {

            /**
             * ImageLoading start
             *
             * @param url
             */
            void onStart(String url);

            /**
             * ImageLoading complete
             *
             * @param url
             * @param bitmap Bitmap we want
             * @param e      not null if download failed
             */
            void onComplete(String url, Bitmap bitmap, Throwable e);

            /**
             * For batch of urls
             *
             * @param e
             */
            void onComplete(Throwable e);


        }

        /**
         * Download image, sync call
         *
         * @param url
         * @param listener
         * @return
         */
        void download(String url, IBitmapDownloadListener listener);

    }

    private final Map<String, Bitmap> mCaches = new HashMap<>();

    /**
     * Download urls listener.
     */
    private final class BitmapsDownloadTask implements IBitmapDownloader.IBitmapDownloadListener {

        List<IBitmapDownloader.IBitmapDownloadListener> mListeners = new ArrayList<>();

        final InternalMap mUrls = new InternalMap();
        RetryRobot mRetryRobot;

        GraffitiView.IBitmapProvider mBitmapManager;
        Exception mLastException;

        final int WAITING = 1;
        final int SUCCESS = 2;
        final int FAILED = 3;

        /**
         * Status HashMap
         */
        private final class InternalMap extends HashMap<String, Integer> {

            int waitingCount = 0;
            int successCount = 0;

            /**
             * Value must be {@link #WAITING},{@link #SUCCESS},{@link #FAILED}
             *
             * @param key
             * @param value
             * @return
             */
            @Override
            public Integer put(String key, Integer value) {
                if (value < WAITING || value > FAILED) {
                    throw new IllegalArgumentException("Invalid value -> " + value);
                }
                Integer result = super.put(key, value);
                onDataChanged();
                return result;
            }


            void onDataChanged() {
                int waiting = 0;
                int success = 0;
                for (Integer value : values()) {
                    if (value == WAITING) {
                        waiting++;
                    } else if (value == SUCCESS) {
                        success++;
                    }
                }
                waitingCount = waiting;
                successCount = success;
            }

            /**
             * Is all urls result back.
             *
             * @return
             */
            boolean isAllFinished() {
                return waitingCount == 0;
            }

            /**
             * Is all urls download success.
             *
             * @return
             */
            boolean isAllSuccess() {
                return successCount == size();
            }
        }


        /**
         * Retry Robot
         */
        private final class RetryRobot implements Runnable {

            final int MAX_RETRY_COUNT = 3;

            int mRetryCounter = MAX_RETRY_COUNT;
            final long TimeOut = 30 * 1000;
            boolean mCanceled = false;

            final Handler mInternalHandler = new Handler();

            @Override
            public void run() {
                if (mCanceled) {
                    return;
                }
                retry();
            }

            void start() {
                mInternalHandler.removeCallbacks(this);
                mInternalHandler.postDelayed(this, TimeOut);
                mCanceled = false;
            }

            void cancel() {
                mInternalHandler.removeCallbacks(this);
                mCanceled = true;
            }

            boolean retry() {
                mInternalHandler.removeCallbacks(this);
                if (!isAllDownloaded()) {
                    //retry
                    if (mRetryCounter-- > 0) {
                        Log.e(TAG, "starting the " + (MAX_RETRY_COUNT - mRetryCounter) + "st retry !");
                        BitmapsDownloadTask.this.start();
                        return true;
                    }
                }
                return false;
            }

        }

        public BitmapsDownloadTask(GraffitiView.IBitmapProvider bitmapCache, List<String> urls) {
            mBitmapManager = bitmapCache;
            mRetryRobot = new RetryRobot();
            mUrls.clear();
            for (String url : urls) {
                mUrls.put(url, WAITING);
            }
            Log.e(TAG, "mUrls -> " + mUrls);
        }

        public void addListener(IBitmapDownloader.IBitmapDownloadListener listener) {
            if (isFinished()) {
                return;
            }
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        @Override
        public void onStart(String url) {
            if (isFinished()) {
                return;
            }
            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onStart(url);
                }
            }
        }

        @Override
        public void onComplete(String url, Bitmap bitmap, Throwable e) {
            if (isFinished()) {
                return;
            }
            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onComplete(url, bitmap, e);
                }
            }
            mUrls.put(url, bitmap != null ? SUCCESS : FAILED);

            if (mUrls.isAllFinished()) {
                if (isAllDownloaded()) {
                    mLastException = null;
                } else {
                    //error when
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("The flowing urls can not be downloaded:\n");
                    for (String value : mUrls.keySet()) {
                        int status = mUrls.get(value);
                        if (status == FAILED) {
                            stringBuilder.append(value);
                            stringBuilder.append("\n");
                        }
                    }
                    mLastException = new NetworkErrorException(stringBuilder.toString());
                }

                onComplete(mLastException);
            }
        }

        @Override
        public void onComplete(Throwable e) {
            Log.e(TAG, "Finally download all e -> " + e);
            if (isFinished()) {
                return;
            }

            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onComplete(e);
                }
            }

            if (e != null) {
                Log.e(TAG, "Failure checked, retry ...");
                if (mRetryRobot.retry()) {
                    Log.e(TAG, "Retry Success.");
                    return;
                } else {
                    Log.e(TAG, "Retry Failed.");
                }
            }

            //stopAndClear all listeners
            stopAndClear();
        }

        /**
         * Start task
         */
        public void start() {
            Log.v(TAG, "start");
            if (!isFinished()) {
                for (String url : mUrls.keySet()) {
                    int status = mUrls.get(url);
                    if (status != SUCCESS) {
                        mUrls.put(url, WAITING);
                        download(url, BitmapsDownloadTask.this);
                    }
                }
                mRetryRobot.start();
            }
        }

        /**
         * Stop and clear task
         */
        public void stopAndClear() {
            mListeners.clear();
            mListeners = null;
            mBitmapManager = null;
            mRetryRobot.cancel();
            mRetryRobot = null;
        }

        /**
         * Is all urls passed downloaded or not.
         *
         * @return
         */
        public boolean isAllDownloaded() {
            return mUrls.isAllSuccess();
        }

        /**
         * Is task finished or not.
         *
         * @return
         */
        public boolean isFinished() {
            return mBitmapManager == null || mListeners == null || mRetryRobot == null;
        }
    }

    private BitmapsDownloadTask mBitmapsDownloadTask;

    private IBitmapDownloader mBitmapDownloader;

    private DemoGraffitiBitmapProvider(IBitmapDownloader downloader) {
        if (downloader == null) {
            throw new IllegalArgumentException("IBitmapDownloader can not be null !");
        }
        mBitmapDownloader = downloader;
    }

    @Override
    public Object getBitmap(final String id) {
        Log.v(TAG, "getBitmap url -> " + id);
        if (TextUtils.isEmpty(id)) {
            return null;
        }

//        Bitmap cache = null;
//        cache = getCache(id);
//        if (cache != null) {
//            return cache;
//        }

        //return all bitmaps.
        if (isBitmapsReady()) {
            List<Bitmap> bitmaps = new ArrayList<>(mCaches.values());
            return bitmaps.toArray(new Bitmap[]{});
        }


        download(id, null);
        return null;
    }

    @Override
    public boolean isBitmapsReady() {
        return (mBitmapsDownloadTask != null && mBitmapsDownloadTask.isAllDownloaded());
    }

    /**
     * Cache url
     *
     * @param url
     * @param bitmap
     */
    public void cache(String url, Bitmap bitmap) {
        Log.v(TAG, "cache url -> " + url + " bitmap -> " + bitmap);
        if (TextUtils.isEmpty(url) || bitmap == null) {
            return;
        }
        mCaches.put(url, bitmap);
    }

    /**
     * Getting cache
     *
     * @param url
     * @return
     */
    public Bitmap getCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (mCaches.containsKey(url)) {
            return mCaches.get(url);
        }
        return null;
    }

    /**
     * Clear all caches
     */
    public void clear() {
        mCaches.clear();
        if (mBitmapsDownloadTask != null) {
            mBitmapsDownloadTask.stopAndClear();
            mBitmapsDownloadTask = null;
        }
    }

    /**
     * Downloading url.
     *
     * @param url
     * @param listener
     */
    public void download(String url, final IBitmapDownloader.IBitmapDownloadListener listener) {
        //no cache, download it
        IBitmapDownloader.IBitmapDownloadListener internalListener = new IBitmapDownloader.IBitmapDownloadListener() {
            @Override
            public void onStart(String url) {
                Log.v(TAG, "onStart -> " + url);
                if (listener != null) {
                    listener.onStart(url);
                }
            }

            @Override
            public void onComplete(String url, Bitmap bitmap, Throwable e) {
                Log.v(TAG, "onComplete url -> " + url + " bitmap -> " + bitmap + " e -> " + e);
                if (listener != null) {
                    listener.onComplete(url, bitmap, e);
                }
                cache(url, bitmap);
            }

            @Override
            public void onComplete(Throwable e) {
                if (listener != null) {
                    listener.onComplete(e);
                }
            }
        };
        mBitmapDownloader.download(url, internalListener);
    }

    /**
     * Loading bitmaps all once
     *
     * @param urls
     * @param listener
     * @param forceReload
     */
    public void download(final List<String> urls, final IBitmapDownloader.IBitmapDownloadListener listener, boolean forceReload) {
        Log.e(TAG, "download urls -> " + urls + " forceReload -> " + forceReload);
        if (urls == null) {
            Log.e(TAG, "\t urls is null!");
            listener.onComplete(new IllegalArgumentException("urls is null"));
            return;
        }

        if (forceReload && mBitmapsDownloadTask != null) {
            Log.e(TAG, "\t forceReload, clear current task");
            mBitmapsDownloadTask.stopAndClear();
            mBitmapsDownloadTask = null;
        }

        if (mBitmapsDownloadTask != null) {
            Log.e(TAG, "LoadsBitmapTask already in flight. isAllFinished -> " + mBitmapsDownloadTask.isAllDownloaded());
            if (mBitmapsDownloadTask.isFinished()) {
                listener.onComplete(mBitmapsDownloadTask.mLastException);
            } else {
                Log.v(TAG, "add listener to task ...");
                mBitmapsDownloadTask.addListener(listener);
            }
            return;
        }

        //downloads all
        mBitmapsDownloadTask = new BitmapsDownloadTask(this, urls);
        mBitmapsDownloadTask.addListener(new IBitmapDownloader.IBitmapDownloadListener() {
            @Override
            public void onStart(String url) {
                if (listener != null) {
                    listener.onStart(url);
                }
            }

            @Override
            public void onComplete(String url, Bitmap bitmap, Throwable e) {
                if (listener != null) {
                    listener.onComplete(url, bitmap, e);
                }
            }

            @Override
            public void onComplete(Throwable e) {
                if (listener != null) {
                    listener.onComplete(e);
                }
            }
        });
        Log.e(TAG, "LoadsBitmapTask start successfully!");
        mBitmapsDownloadTask.start();
    }


    private static DemoGraffitiBitmapProvider mInstance;

    public static DemoGraffitiBitmapProvider getInstance(IBitmapDownloader downloader) {
        if (mInstance == null) {
            mInstance = new DemoGraffitiBitmapProvider(downloader);
        }
        return mInstance;
    }

}
