package com.bfc.wyzgraffitiboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import com.bfc.wyzgraffitiboard.animation.AbstractBaseAnimator;
import com.bfc.wyzgraffitiboard.animation.AnimatorFactory;
import com.bfc.wyzgraffitiboard.coordinates.ICoordinateConverter;
import com.bfc.wyzgraffitiboard.coordinates.SimpleCoordinateConverter;
import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;
import com.bfc.wyzgraffitiboard.data.GraffitiNoteData;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 图层绘制
 * <p>
 */
public class GraffitiLayerView extends View {

    private ICoordinateConverter mCoordinateConverter;

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

        if (getLayerData().hasAnimation()) {
            mAnimator = AnimatorFactory.create(getLayerData(), this);
        }
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


    /**
     * Update view
     */
    public void notifyDataChanged() {
        invalidate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            mCoordinateConverter = new SimpleCoordinateConverter(getLayerData(), this);
        }
    }


    private AbstractBaseAnimator mAnimator;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.stop();
        }
    }

    private int mBlankCanvas = -1;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBlankCanvas <= 0) {
            mBlankCanvas = canvas.save();
        }

        if (getLayerData().isForceDrawAll()) {
            //TODO clean the canvas ??
            canvas.restoreToCount(mBlankCanvas);
        }

        int size = getLayerData().getCount();
        for (int i = size - 1; i >= 0; i--) {
            GraffitiNoteData note = getLayerData().getNotes().get(i);
            if (note.mDrawn && !getLayerData().isForceDrawAll()) {
                return;
            }
            note.mDrawn = true;
            onDrawNote(canvas, note, mCoordinateConverter.convert(note.getCalculateRectF(), null));
        }

        //if force draw all, reset it's status
        getLayerData().finishForceDrawAll(false);
    }

    /**
     * Called when draw your note
     *
     * @param canvas
     * @param note   Note information
     */
    protected void onDrawNote(Canvas canvas, GraffitiNoteData note, RectF rectF) {
        // shall we have a better plan ?

        mCanvas.drawBitmap(mGiftIcon, null, rectF, null);

//        float proportion = (float) canvas.getHeight() / mBitmap.getHeight();
//        if (proportion < 1) {
//            mProportion = proportion;
//            mMatrix.reset();
//            mMatrix.postScale(proportion, proportion);
//            mMatrix.postTranslate((canvas.getWidth() - mBitmap.getWidth() * proportion) / 2, 0);
//            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
//        } else {
//            mProportion = 0;
//            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
//        }
    }


}
