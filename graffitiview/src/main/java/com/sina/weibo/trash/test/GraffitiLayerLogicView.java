package com.sina.weibo.trash.test;

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
import android.util.Log;

import com.sina.weibo.trash.logic.LogicView;
import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;
import com.sina.weibo.view.graffitiview.data.GraffitiNoteData;


/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 图层绘制
 * <p>
 */
public class GraffitiLayerLogicView extends LogicView {

    private GraffitiLayerData mLayerData;

    private Path mPath;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mGiftIcon;
    private float mProportion;
    private Matrix mMatrix;

    public GraffitiLayerLogicView(Context context, GraffitiLayerData data) {
        super(context);
        mLayerData = data;

        mGiftIcon = BitmapFactory.decodeResource(getContext().getResources(), getLayerData().getNoteDrawableRes());

        initDrawParams();

        if (getLayerData().isHasAnimation()) {
            getLayerData().installAnimator(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
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
        return mLayerData;
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
        if (w >= 0 && h >= 0) {
            getLayerData().installView(w, h);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getLayerData().startAnimatorIfExits();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getLayerData().stopAnimatorIfExits();
    }

    private int mBlankCanvas = -1;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBlankCanvas <= 0) {
            mBlankCanvas = canvas.save();
            Log.e(TAG, " mBlankCanvas -> " + mBlankCanvas);
        }

        if (getLayerData().isForceDrawAll()) {
            //TODO clean the canvas ??
            Log.e(TAG, " restore canvas to -> " + mBlankCanvas);
            canvas.restoreToCount(mBlankCanvas);
        }

        int size = getLayerData().getNotes().size();
        for (int i = size - 1; i >= 0; i--) {
            GraffitiNoteData note = getLayerData().getNotes().get(i);
            note.mDrawn = false;
            if (note.mDrawn && !getLayerData().isForceDrawAll()) {
                return;
            }
            drawNote(canvas, note);
            note.mDrawn = true;
        }

        //if force draw all, reset it's status
        getLayerData().finishForceDrawAll(false);

        canvas.save();
    }

    /**
     * Called when draw your note
     *
     * @param canvas
     * @param note   Note information
     */
    protected void drawNote(Canvas canvas, GraffitiNoteData note) {
        Log.v(TAG, "drawNote note -> " + note);
        onDrawNote(canvas, mGiftIcon, note.getCalculateRectF(), note);
    }


    protected void onDrawNote(Canvas canvas, Bitmap bitmap, RectF rectF, GraffitiNoteData note) {
        Log.e(TAG, "onDrawNote rectF -> " + rectF + " bitmap -> " + bitmap);
        canvas.drawBitmap(bitmap, null, rectF, null);
    }


}
