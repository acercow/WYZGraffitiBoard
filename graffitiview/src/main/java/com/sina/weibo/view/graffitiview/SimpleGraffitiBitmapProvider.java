package com.sina.weibo.view.graffitiview;

import android.accounts.NetworkErrorException;
import android.graphics.Bitmap;
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
 * Implement for {@link GraffitiView.IBitmapProvider}
 * <p>
 * <p>
 * 1,Network download
 * 2,Memory cache
 * 3,Disk cache coming soon
 */

public class SimpleGraffitiBitmapProvider implements GraffitiView.IBitmapProvider {

    public static final String TAG = SimpleGraffitiBitmapProvider.class.getSimpleName();

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


        /**
         * Loading cache of url
         *
         * @param url
         * @return
         */
        Bitmap loadCache(String url);
    }


    private Map<String, Bitmap> mCaches = new HashMap<>();

    /**
     * Download urls listener.
     */
    private class BitmapsDownloadTask implements IBitmapDownloader.IBitmapDownloadListener {

        final String TAG = BitmapsDownloadTask.class.getSimpleName();

        int mRetryCounter = 3;

        List<IBitmapDownloader.IBitmapDownloadListener> mListeners = new ArrayList<>();

        List<String> mUrls;
        int mCompleteCounter;

        GraffitiView.IBitmapProvider mBitmapManager;
        Exception mLastException;

        public BitmapsDownloadTask(GraffitiView.IBitmapProvider bitmapCache, List<String> urls) {
            mBitmapManager = bitmapCache;
            mUrls = new ArrayList<>(urls);
            mCompleteCounter = urls.size();
            Log.e(TAG, "mUrls -> " + mUrls + " mCompleteCounter -> " + mCompleteCounter);
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
            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onStart(url);
                }
            }
        }

        @Override
        public void onComplete(String url, Bitmap bitmap, Throwable e) {
            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onComplete(url, bitmap, e);
                }
            }
            //complete counter
            mCompleteCounter--;

            if (bitmap != null) {
                mUrls.remove(url);
            }

            if (mCompleteCounter == 0) {
                if (isAllDownloaded()) {
                    mLastException = null;
                } else {
                    //error when
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("The flowing urls can not be downloaded:\n");
                    for (String value : mUrls) {
                        stringBuilder.append(value + "\n");
                    }
                    mLastException = new NetworkErrorException(stringBuilder.toString());
                }
                onComplete(mLastException);
            }
        }

        @Override
        public void onComplete(Throwable e) {
            Log.e(TAG, "Finally download all e -> " + e);
            for (IBitmapDownloader.IBitmapDownloadListener listener : mListeners) {
                if (listener != null) {
                    listener.onComplete(e);
                }
            }

            if (e != null && mRetryCounter > 0 && mBitmapManager != null) {
                Log.e(TAG, "Failure checked, retry ...");
                start();
                mRetryCounter--;
                return;
            }

            //stopAndClear all listeners
            stopAndClear();
        }

        /**
         * Start task
         */
        public void start() {
            Log.v(TAG, "start");
            if (mBitmapManager != null) {
                for (String url : mUrls) {
                    download(url, BitmapsDownloadTask.this);
                }
            }
        }

        /**
         * Stop and clear task
         */
        public void stopAndClear() {
            mListeners.clear();
            mListeners = null;
            mRetryCounter = 0;
            mCompleteCounter = 0;
            mBitmapManager = null;
        }

        /**
         * Is all urls passed downloaded or not.
         *
         * @return
         */
        public boolean isAllDownloaded() {
            return mUrls.size() == 0;
        }

        /**
         * Is task finished or not.
         *
         * @return
         */
        public boolean isFinished() {
            return mCompleteCounter == 0 || mBitmapManager == null || mListeners == null;
        }
    }

    private BitmapsDownloadTask mBitmapsDownloadTask;

    private IBitmapDownloader mBitmapDownloader;

    public SimpleGraffitiBitmapProvider(IBitmapDownloader downloader) {
        if (downloader == null) {
            throw new IllegalArgumentException("IBitmapDownloader can not be null !");
        }
        mBitmapDownloader = downloader;
    }

    @Override
    public Bitmap getBitmap(final String url) {
        Log.v(TAG, "getBitmap url -> " + url);
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Bitmap cache = null;
        cache = getCache(url);
        if (cache != null) {
            return cache;
        }

        //bug protection for ImageDownloader
        cache = mBitmapDownloader.loadCache(url);
        if (cache != null) {
            cache(url, cache);
            return getBitmap(url);
        }

        download(url, null);
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
        Bitmap cache = mBitmapDownloader.loadCache(url);
        if (cache != null) {
            listener.onStart(url);
            listener.onComplete(url, cache, null);
            return;
        }

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
            Log.e(TAG, "LoadsBitmapTask already in flight. isAllDownloaded -> " + mBitmapsDownloadTask.isAllDownloaded());
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


    private static SimpleGraffitiBitmapProvider mInstance;

    public static SimpleGraffitiBitmapProvider getInstance(IBitmapDownloader downloader) {
        if (mInstance == null) {
            mInstance = new SimpleGraffitiBitmapProvider(downloader);
        }
        return mInstance;
    }

}
