package com.prologdigital.prologicvod;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by DanM on 08/05/2016.
 */
public class UIAnimation{
    public int animXdirection = 750;
    public int animYdirection = 850;
    public boolean additionalCoursesOpen = false;
    public int screenHeight;

    public void setLayerSize(DisplayMetrics displaymetrics, LinearLayout main, FrameLayout hover){
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        //LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        ViewGroup.LayoutParams params = main.getLayoutParams();
        screenHeight = height;
        params.height = height;
        params.width = width;
        main.setLayoutParams(params);
        //hover.setLayoutParams(params);
    }

    public void splashFade(final View view){
        Animation animation;
        animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.splash);
        animation.setDuration(2000);
        animation.setInterpolator(new MVAccelerateDecelerateInterpolator());
        animation.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setAlpha(0);
            }
        });
        view.startAnimation(animation);

    }

    public void moveLeft(final LinearLayout mainLayout){
        TranslateAnimation anim = new TranslateAnimation(0, animXdirection, 0, 0);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(700);
        anim.setFillAfter(true);
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                mainLayout.clearAnimation();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainLayout.getLayoutParams();
                params.leftMargin += animXdirection;
                mainLayout.setLayoutParams(params);
                animXdirection = animXdirection*-1;
            }
        });
        mainLayout.startAnimation(anim);
    }
    public void moveDown(final LinearLayout mainLayout, final ImageButton arrowButton){
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, animYdirection);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(700);
        anim.setFillAfter(true);
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                //moving layout to new position
                mainLayout.clearAnimation();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainLayout.getLayoutParams();
                params.topMargin += animYdirection;
                mainLayout.setLayoutParams(params);
                //changing arrow button direction
                if (animYdirection > 0) arrowButton.setImageResource(R.drawable.chevron_yellow_up);
                else arrowButton.setImageResource(R.drawable.chevron_yellow_down);
                animYdirection = animYdirection*-1;
            }
        });
        mainLayout.startAnimation(anim);
    }
    public void fadeHover(final FrameLayout hoverLayer, final boolean checkXY){
        float upgradeAlphaFrom;
        float upgradeAlphaTo;

        if (checkXY){
            upgradeAlphaFrom = (animXdirection > 0) ? 0 : 1;
            upgradeAlphaTo = (animXdirection < 0) ? 0 : 1;
        } else {
            upgradeAlphaFrom = (animYdirection > 0) ? 0 : 1;
            upgradeAlphaTo = (animYdirection < 0) ? 0 : 1;
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(upgradeAlphaFrom, upgradeAlphaTo);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                if (checkXY){
                    if (animXdirection < 0) hoverLayer.removeAllViews();
                } else {
                    if (animYdirection < 0) hoverLayer.removeAllViews();
                }
            }
        });
        hoverLayer.startAnimation(alphaAnimation);
        hoverLayer.setAlpha(1);
    }

    public class MVAccelerateDecelerateInterpolator implements Interpolator {
        // easeInOutQuint
        public float getInterpolation(float t) {
            float x;
            if (t<0.5f){
                x = t*2.0f;
                return 0.5f*x*x*x*x*x;
            }
            x = (t-0.5f)*2-1;
            return 0.5f*x*x*x*x*x+1;
        }
    }
}
