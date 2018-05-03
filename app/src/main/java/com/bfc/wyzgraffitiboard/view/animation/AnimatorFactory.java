package com.bfc.wyzgraffitiboard.view.animation;

import com.bfc.wyzgraffitiboard.view.data.GraffitiLayerDataObject;
import com.bfc.wyzgraffitiboard.view.test.GraffitiLayerLogicView;

/**
 * Created by fishyu on 2018/5/2.
 */

public class AnimatorFactory {

    public final static int SCALE = 1;
    public final static int TRANSLATE = 2;
    public final static int ALPHA = 3;
    public final static int ROTATE = 4;

    public static AbstractBaseAnimator create(GraffitiLayerDataObject data, GraffitiLayerLogicView view) {
        return new ScaleAnimator(data, view, 1000, 1.0f, 0.5f);
    }

}
