package com.bfc.wyzgraffitiboard.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bfc.wyzgraffitiboard.R;

public class GraffitiBoardView extends View {
    private static final String TAG = GraffitiBoardView.class.getSimpleName();
    private Path mPath;
    private Paint mPaint;
    private float downX, downY;
    private float tempX, tempY;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mGiftIcon;
    private float mProportion;
    private Matrix mMatrix;


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
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPath = new Path();
        mGiftIcon = BitmapFactory.decodeResource(getResources(), R.drawable.shield_icon);
        BitmapShader bitmapShader = new BitmapShader(mGiftIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        mPaint.setShader(bitmapShader);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xFF992277);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mBitmap != null) {
            if ((mBitmap.getHeight() > heightSize) && (mBitmap.getHeight() > mBitmap.getWidth())) {
                widthSize = heightSize * mBitmap.getWidth() / mBitmap.getHeight();
            } else if ((mBitmap.getWidth() > widthSize) && (mBitmap.getWidth() > mBitmap.getHeight())) {
                heightSize = widthSize * mBitmap.getHeight() / mBitmap.getWidth();
            } else {
                heightSize = mBitmap.getHeight();
                widthSize = mBitmap.getWidth();
            }
        }
        Log.d(TAG, "onMeasure: heightSize: " + heightSize + " widthSize: " + widthSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(new Rect(0, 0, 400, 400), mPaint);
//        canvas.drawPath(mPath, mPaint);
//        canvas.drawColor(Color.BLUE);
        float proportion = (float) canvas.getHeight() / mBitmap.getHeight();
        if (proportion < 1) {
            mProportion = proportion;
            mMatrix.reset();
            mMatrix.postScale(proportion, proportion);
            mMatrix.postTranslate((canvas.getWidth() - mBitmap.getWidth() * proportion) / 2, 0);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        } else {
            mProportion = 0;
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                mPath.moveTo(pointX, pointY);
                tempX = pointX;
                tempY = pointY;
                mCanvas.drawBitmap(mGiftIcon, pointX - mGiftIcon.getWidth(), pointY - mGiftIcon.getHeight(), null);
                return true;
            case MotionEvent.ACTION_MOVE:
//                mPath.lineTo(pointX, pointY);
//                final int historySize = event.getHistorySize();
//                final int pointerCount = event.getPointerCount();
//                for (int h = 0; h < historySize; h++) {
//                        pointX = event.getHistoricalX(h);
//                        pointY = event.getHistoricalY(h);
//                        double distance = (int) Math.sqrt(Math.pow(pointX - tempX, 2) + Math.pow(pointY - tempY, 2));
//                        if (distance > -1) {
//                            mCanvas.drawBitmap(mGiftIcon, pointX, pointY, null);
//                            tempX = pointX;
//                            tempY = pointY;
//                    }
//                System.out.printf("At time %d:", event.getEventTime());
//                for (int p = 0; p < pointerCount; p++) {
//                    System.out.printf("  pointer %d: (%f,%f)",
//                            event.getPointerId(p), event.getX(p), event.getY(p));
//                }

                float distance = (float) Math.sqrt(Math.pow(pointX - tempX, 2) + Math.pow(pointY - tempY, 2));
                if (distance > 120) {
                    float ratio = distance / 120F;
                    float gapX = (pointX - tempX) / ratio;
                    float gapY = (pointY - tempY) / ratio;
                    for (int i = 1; i <= ratio; i++) {
                        mCanvas.drawBitmap(mGiftIcon, tempX + gapX - mGiftIcon.getWidth(), tempY + gapY - mGiftIcon.getHeight(), null);
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
}


