package com.bfc.wyzgraffitiboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 图层绘制
 * <p>
 * TODO
 * 1，坐标系统
 * 2，动画系统
 */
class GraffitiLayerView extends View {

    static final boolean OPTIMIZE_CHECK_DATA = false;

    public static final int MASK_REDRAW_ALL = 0x1 << 0;

    private int mTag = 0;

    private boolean mAllowMerge = true;

    private Path mPath;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mGiftIcon;
    private float mProportion;
    private Matrix mMatrix;

    public GraffitiLayerView(Context context, GraffitiLayerData data) {
        super(context);
        initDrawParams();
        setTag(data);
        mGiftIcon = BitmapFactory.decodeResource(getResources(), data.mIconRes);
    }


    private void initDrawParams() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPath = new Path();
        BitmapShader bitmapShader = new BitmapShader(mGiftIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        mPaint.setShader(bitmapShader);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xFF992277);
    }


    /**
     * Getting layer data
     *
     * @return
     */
    public GraffitiLayerData getLayerData() {
        return (GraffitiLayerData) getTag();
    }


    protected boolean isForceDrawAll() {
        return (mTag & MASK_REDRAW_ALL) == 0;
    }

    /**
     * Force draw all
     */
    public void forceDrawAll() {
        mTag |= MASK_REDRAW_ALL;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isForceDrawAll()) {
            //TODO clean the canvas ??
        }

        int size = getLayerData().getCount();
        for (int i = size - 1; i >= 0; i--) {
            GraffitiLayerData.GraffitiNote note = getLayerData().getNotes().get(i);
            if (note.mDrawn && !isForceDrawAll()) {
                return;
            }
            note.mDrawn = true;
            drawNote(canvas, note);
        }
    }

    /**
     * Draw note
     *
     * @param canvas
     */
    protected void drawNote(Canvas canvas, GraffitiLayerData.GraffitiNote note) {
        // shall we have a better plan ?
        mCanvas.drawBitmap(mGiftIcon, note.mX, note.mY, null);

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


}
