package com.sina.weibo.view.graffitiview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;
import com.sina.weibo.view.graffitiview.data.GraffitiNoteData;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 图层绘制
 * <p>
 */
public class GraffitiLayerView extends View {

    final String TAG = GraffitiLayerView.class.getSimpleName() + this;

    private Bitmap mGiftIcon;
    private int mBlankCanvas = -1;

    public GraffitiLayerView(Context context, GraffitiLayerData data) {
        super(context);
        setTag(data);
        mGiftIcon = BitmapFactory.decodeResource(getResources(), data.getNoteDrawableRes());

        // installView animator
        getLayerData().installAnimator(new Runnable() {
            @Override
            public void run() {
                notifyDataChanged();
            }
        });
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
            if (w >= 0 && h >= 0) {
                if (getLayerData().installView(w, h)) {
                    if (getLayerData().isShowMode()) {
                        getLayerData().installNotes();
                        notifyDataChanged();
                    }
                }
            }
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
        if (mGiftIcon != null && !mGiftIcon.isRecycled()) {
            try {
                mGiftIcon.recycle();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            getLayerData().startAnimatorIfExits();
        } else {
            getLayerData().stopAnimatorIfExits();
        }
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getLayerData().isForceDrawAll()) {
            //TODO clean the canvas ??
//            Log.e(TAG, " restore canvas to -> " + mBlankCanvas);
        }

        int size = getLayerData().getNotes().size();
        for (int i = size - 1; i >= 0; i--) {
            GraffitiNoteData note = getLayerData().getNotes().get(i);
            note.mDrawn = false;
            if (note.mDrawn && !getLayerData().isForceDrawAll()) {
                break;
            }
            drawNote(canvas, note);
            note.mDrawn = true;
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
    protected void drawNote(Canvas canvas, GraffitiNoteData note) {
        onDrawNote(canvas, note, mGiftIcon, note.getCalculateRectF());
    }


    /**
     * Do what u want to draw your note
     *
     * @param canvas
     * @param note
     * @param bitmap
     * @param rectF
     */
    protected void onDrawNote(Canvas canvas, GraffitiNoteData note, Bitmap bitmap, RectF rectF) {
        canvas.drawBitmap(bitmap, null, rectF, null);
    }


}
