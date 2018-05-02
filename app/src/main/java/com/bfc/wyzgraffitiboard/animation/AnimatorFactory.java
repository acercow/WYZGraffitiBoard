package com.bfc.wyzgraffitiboard.animation;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;
import com.bfc.wyzgraffitiboard.view.GraffitiLayerView;

/**
 * Created by fishyu on 2018/5/2.
 */

public class AnimatorFactory {

    public final static int SCALE = 1;
    public final static int TRANSLATE = 2;
    public final static int ALPHA = 3;
    public final static int ROTATE = 4;

    public static AbstractBaseAnimator create(GraffitiLayerData data, GraffitiLayerView view) {
        return new ScaleAnimator(data, view, 600, 1.0f, 0f);
    }
}
