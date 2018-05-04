package com.sina.weibo.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.bfc.wyz.R;
import com.sina.weibo.DrawStatusListener;

public class GraffitiBoardView extends View {
    private static final String TAG = GraffitiBoardView.class.getSimpleName();
    private static final int MIN_GIFT_NUM = 10; //最少可以发送的涂鸦礼物数量
    private Path mPath;
    private Paint mPaint;
    private float downX, downY;
    private float tempX, tempY;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mGiftIcon;
    private float mProportion;
    private Matrix mMatrix;
    private float mCanvasScale;
    private boolean mStartDraw; //是否开始绘制
    private DrawStatusListener mDrawStatusListener;
    private int mGiftNum;


    public GraffitiBoardView(Context context) {
        super(context);
        init();
    }

    public GraffitiBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraffitiBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setStrokeWidth(10);
//        mPath = new Path();
        mGiftIcon = BitmapFactory.decodeResource(getResources(), R.drawable.shield_icon);
//        BitmapShader bitmapShader = new BitmapShader(mGiftIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
////        mPaint.setShader(bitmapShader);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(0xFF992277);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "onMeasure: heightSize: " + heightSize + " widthSize: " + widthSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap == null) {
            mCanvasScale = w / 1080F;
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            Resources resources = getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            float density = displayMetrics.density;
            int densityDpi = displayMetrics.densityDpi;
            System.out.println("----> density=" + density);
            System.out.println("----> densityDpi=" + densityDpi);
            Log.i(TAG, "Gift Icon w: " + mGiftIcon.getWidth() + " h: " + mGiftIcon.getHeight());

            Matrix matrix = new Matrix();
            matrix.postScale(mCanvasScale / density * 3, mCanvasScale / density * 3);

            mGiftIcon = Bitmap.createBitmap(mGiftIcon, 0, 0, mGiftIcon.getWidth(), mGiftIcon.getHeight(), matrix, true);
        }
        mCanvas = new Canvas(mBitmap);
        TextView textView = new TextView(getContext());
        textView.draw(mCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tempX = pointX;
                tempY = pointY;
                drawBitmap(mGiftIcon, pointX - mGiftIcon.getWidth() / 2, pointY - mGiftIcon.getHeight() / 2, null);
                return true;
            case MotionEvent.ACTION_MOVE:

                float distance = (float) Math.sqrt(Math.pow(pointX - tempX, 2) + Math.pow(pointY - tempY, 2));
                if (distance > 90F * mCanvasScale) {
                    float ratio = distance / (90F * mCanvasScale);
                    float gapX = (pointX - tempX) / ratio;
                    float gapY = (pointY - tempY) / ratio;
                    for (int i = 1; i <= ratio; i++) {
                        drawBitmap(mGiftIcon, tempX + gapX - mGiftIcon.getWidth() / 2, tempY + gapY - mGiftIcon.getHeight() / 2, null);
                        tempX = tempX + gapX;
                        tempY = tempY + gapY;
                    }
                }

//                mCanvas.drawPath(mPath, mPaint);
                break;
            default:
                return false;
        }
        // Force a view to draw again
        invalidate();
        return false;
    }


    void printSamples(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        for (int h = 0; h < historySize; h++) {
            System.out.printf("At time %d:", ev.getHistoricalEventTime(h));
            for (int p = 0; p < pointerCount; p++) {
                System.out.printf("  pointer %d: (%f,%f)",
                        ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        System.out.printf("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            System.out.printf("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
    }

    private void drawBitmap(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint) {
        mCanvas.drawBitmap(bitmap, left, top, paint);
        if (mDrawStatusListener != null) {
            if (!mStartDraw) {
                mStartDraw = true;
                mDrawStatusListener.onStatusChange(DrawStatusListener.DrawStatus.START, mGiftNum, mGiftNum * 10);
            }
            if (mGiftNum >= MIN_GIFT_NUM) {
                mDrawStatusListener.onStatusChange(DrawStatusListener.DrawStatus.FINISH, mGiftNum, mGiftNum * 10);
            }
        }
        mGiftNum++;
    }

    public void setDrawStatusListener(DrawStatusListener drawStatusListener) {
        mDrawStatusListener = drawStatusListener;
    }

    public void clearCanvas() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();

        resetStatus();
    }

    private void resetStatus() {
        mGiftNum = 0;
        mStartDraw = false;
        if (mDrawStatusListener != null) {
            mDrawStatusListener.onStatusChange(DrawStatusListener.DrawStatus.DEFAULT, 0, 0);
        }
    }
}


