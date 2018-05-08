package com.bfc.wyz;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.weibo.DrawStatusListener;
import com.sina.weibo.view.GraffitiBoardView;

/**
 * @author shichao5
 * @date 2018/5/3
 * @describ
 */

public class GraffitiManager implements View.OnClickListener, DrawStatusListener {
    private Context mContext;
    private View mRootView;
    private TextView mClearText, mWarnText, mGuideText;
    private ImageView mCloseImage;
    private GraffitiBoardView mGraffitiBoardView;
    private OnDismissListener mOnDismissListener;

    public GraffitiManager(View rootView) {
        mRootView = rootView;
        mRootView.setAlpha(0.6f);
        mContext = rootView.getContext();
    }

    public void init() {
        mClearText = (TextView) mRootView.findViewById(R.id.tv_clear);
        mWarnText = (TextView) mRootView.findViewById(R.id.tv_warn);
        mGuideText = (TextView) mRootView.findViewById(R.id.tv_guide);
        mCloseImage = (ImageView) mRootView.findViewById(R.id.iv_close);
        mGraffitiBoardView = (GraffitiBoardView) mRootView.findViewById(R.id.graffiti_board);
        mGraffitiBoardView.setDrawStatusListener(this);
        mClearText.setOnClickListener(this);
        mCloseImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_clear) {
            mGraffitiBoardView.clearCanvas();
        } else if (id == R.id.iv_close) {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss();
            }
        }
    }

    @Override
    public void onStatusChange(DrawStatus drawStatus, int giftNum, int coinNum) {
        switch (drawStatus) {
            case DEFAULT:
                mClearText.setTextColor(mContext.getResources().getColor(R.color.color_clear_gray));
                mWarnText.setText("");
                mGuideText.setVisibility(View.VISIBLE);
                break;
            case START:
                mGuideText.setVisibility(View.GONE);
                mClearText.setTextColor(mContext.getResources().getColor(android.R.color.white));
                mWarnText.setText("至少要画10个礼物，也可选择其他礼物~");
                break;
            case FINISH:
                mWarnText.setText("画了" + giftNum + "个礼物，消耗" + coinNum + "金币");
                break;
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
