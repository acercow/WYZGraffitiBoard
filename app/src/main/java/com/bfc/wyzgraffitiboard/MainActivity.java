package com.bfc.wyzgraffitiboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bfc.wyzgraffitiboard.graffitimanager.DrawStatusListener;
import com.bfc.wyzgraffitiboard.view.GraffitiBoardView;

public class MainActivity extends AppCompatActivity {

    private TextView mClearText, mWarnText, mGuideText;
    private ImageView mCloseImage;
    private GraffitiBoardView mGraffitiBoardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mClearText = (TextView) findViewById(R.id.tv_clear);
        mWarnText = (TextView) findViewById(R.id.tv_warn);
        mGuideText = (TextView) findViewById(R.id.tv_guide);
        mCloseImage = (ImageView) findViewById(R.id.iv_close);
        mGraffitiBoardView = (GraffitiBoardView) findViewById(R.id.graffiti_board);

        mGraffitiBoardView.setDrawStatusListener(new DrawStatusListener() {
            @Override
            public void onStatusChange(DrawStatus drawStatus, int giftNum, int coinNum) {
                switch (drawStatus) {
                    case DEFAULT:
                        mClearText.setTextColor(getResources().getColor(R.color.color_clear_gray));
                        mWarnText.setText("");
                        mGuideText.setVisibility(View.VISIBLE);
                        break;
                    case START:
                        mGuideText.setVisibility(View.GONE);
                        mClearText.setTextColor(getResources().getColor(android.R.color.white));
                        mWarnText.setText("至少要画10个礼物，也可选择其他礼物~");
                        break;
                    case FINISH:
                        mWarnText.setText("画了" + giftNum + "个礼物，消耗" + coinNum + "金币");
                        break;
                }
            }
        });

        mClearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraffitiBoardView.clearCanvas();
            }
        });
        mCloseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
