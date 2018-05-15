package com.bfc.wyz;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.sina.weibo.view.graffitiview.GraffitiView;

/**
 * Created by zhaosen on 18-5-16.
 */

public class GraffitiUtils {


    public static void startGraffitAnim(final GraffitiView graffitiView) {

        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 1f);
        scaleAnimation.setDuration(1500);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());

        AlphaAnimation fadeInAnimation = new AlphaAnimation(0F, 1F);
        fadeInAnimation.setDuration(1500);
        fadeInAnimation.setFillAfter(true);

        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1F, 0F);
        fadeOutAnimation.setDuration(500);
        fadeOutAnimation.setFillAfter(true);
        fadeOutAnimation.setStartOffset(2500);

        Animation trembleAnimation = new AlphaAnimation(1F, 1F);
        trembleAnimation.setDuration(1100);
        trembleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                graffitiView.getGraffitiData().startAnimationsIfExits();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(fadeInAnimation);
        animationSet.addAnimation(fadeOutAnimation);
        animationSet.addAnimation(trembleAnimation);


        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                graffitiView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                graffitiView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        graffitiView.startAnimation(animationSet);

    }
}
