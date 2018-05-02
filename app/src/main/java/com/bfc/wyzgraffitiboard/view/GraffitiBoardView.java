package com.bfc.wyzgraffitiboard.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    private float mCanvasScale;


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

            Resources resources=getResources();
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
                mCanvas.drawBitmap(mGiftIcon, pointX - mGiftIcon.getWidth() / 2, pointY - mGiftIcon.getHeight() / 2, null);
                return true;
            case MotionEvent.ACTION_MOVE:

                float distance = (float) Math.sqrt(Math.pow(pointX - tempX, 2) + Math.pow(pointY - tempY, 2));
                if (distance > 90F * mCanvasScale) {
                    float ratio = distance / (90F * mCanvasScale);
                    float gapX = (pointX - tempX) / ratio;
                    float gapY = (pointY - tempY) / ratio;
                    for (int i = 1; i <= ratio; i++) {
                        mCanvas.drawBitmap(mGiftIcon, tempX + gapX - mGiftIcon.getWidth() / 2, tempY + gapY - mGiftIcon.getHeight() / 2, null);
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
}


