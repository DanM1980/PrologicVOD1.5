package com.prologdigital.prologvod;

import android.util.DisplayMetrics;
import android.util.Log;
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
    public int animXdirection = 1;
    public int animYdirection = 1;
    public int screenWidth;
    public int screenHeight;
    public int pdfLayoutHeight = 0;
    public int pdfStartMargin = 0;
    public int pdfEndMargin = 0;
    public int pdfEndAnimation = 0;
    public boolean pdfViewerOpen = false;
    public boolean pdfViewerFullscreen = false;
    public boolean videoFullscreen = false;

    public void setLayerSize(DisplayMetrics displaymetrics, LinearLayout main, Boolean setHeight){
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        ViewGroup.LayoutParams params = main.getLayoutParams();
        screenWidth = width;
        screenHeight = height;
        if (setHeight) params.height = height;
        params.width = width;
        main.setLayoutParams(params);
    }

    public void splashFade(final View view){
        Animation animation;
        animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.splash);
        animation.setDuration(3000);
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

    public void moveLeft(final LinearLayout mainLayout, final ImageButton showButton, final ImageButton hideButton, int marginWidth){
        final int marginLeft = marginWidth * animXdirection;
        TranslateAnimation anim = new TranslateAnimation(0, marginLeft, 0, 0);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(1500);
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
                params.leftMargin += marginLeft;
                mainLayout.setLayoutParams(params);
                final int fadeFrom = (animXdirection > 0) ? 1 : 0;
                final int fadeTo = (animXdirection > 0) ? 0 : 1;
                AlphaAnimation alphaAnimation = new AlphaAnimation(fadeFrom, fadeTo);
                alphaAnimation.setDuration(500);
                alphaAnimation.setFillAfter(false);
                alphaAnimation.setAnimationListener(new TranslateAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (fadeTo==1) showButton.setVisibility(View.VISIBLE);
                        else showButton.setVisibility(View.GONE);
                    }
                });
                showButton.startAnimation(alphaAnimation);
                alphaAnimation = new AlphaAnimation(fadeTo, fadeFrom);
                alphaAnimation.setDuration(500);
                alphaAnimation.setFillAfter(false);
                alphaAnimation.setAnimationListener(new TranslateAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (fadeFrom==1) hideButton.setVisibility(View.VISIBLE);
                        else hideButton.setVisibility(View.GONE);
                    }
                });
                hideButton.startAnimation(alphaAnimation);
                animXdirection = animXdirection*-1;
            }
        });
        mainLayout.startAnimation(anim);
    }
    public void moveDown(final LinearLayout mainLayout, final ImageButton arrowButton, int marginHeight){
        if (!videoPositionCenter) moveVideo((LinearLayout)mainLayout.findViewById(R.id.videoLayout), (LinearLayout) mainLayout.findViewById(R.id.lessonTextContainerLayout), false);
        final int marginTop = marginHeight * animYdirection;
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, marginTop);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(1500);
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
                params.topMargin += marginTop;
                mainLayout.setLayoutParams(params);
                //fading arrow button direction
                int fadeFrom = (animYdirection > 0) ? 1 : 0;
                int fadeTo = (animYdirection > 0) ? 0 : 1;
                AlphaAnimation alphaAnimation = new AlphaAnimation(fadeFrom, fadeTo);
                alphaAnimation.setDuration(500);
                alphaAnimation.setFillAfter(true);
                arrowButton.startAnimation(alphaAnimation);
                animYdirection = animYdirection*-1;
            }
        });
        mainLayout.startAnimation(anim);
    }
    public void scrollDown(final FrameLayout hoverLayer){
        int layoutTopMargin = (hoverLayer.getHeight() * -1);
        TranslateAnimation anim = new TranslateAnimation(0, 0, layoutTopMargin, 0);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(2000);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        hoverLayer.startAnimation(anim);
        hoverLayer.setAlpha(1);
    }
    public void scrollUp(final FrameLayout hoverLayer){
        int layoutTopMargin = (hoverLayer.getHeight() * -1);
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, layoutTopMargin);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(2000);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup inclusionViewGroup = (ViewGroup) hoverLayer;
                inclusionViewGroup.removeAllViews();
                hoverLayer.clearAnimation();
            }
        });
        hoverLayer.startAnimation(anim);
    }
    public void scrollPDF(final LinearLayout pdfLayout, String toPosition){
        //LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) hoverLayer.getLayoutParams();
        pdfLayout.clearAnimation();
        if (toPosition.equals("open")){
            pdfStartMargin = screenHeight;
            pdfEndMargin = Math.round(screenHeight/2)+70;
            pdfEndAnimation = Math.round(screenHeight/2)+70 * -1;
            pdfLayoutHeight = Math.round(screenHeight/2)+70;
        } else if(toPosition.equals("close")){
            pdfStartMargin = Math.round(screenHeight/2)+70;
            pdfEndMargin = screenHeight;
            pdfEndAnimation = Math.round(screenHeight/2)+70;
            pdfLayoutHeight = 0;
        } else if(toPosition.equals("fullscreen")){
            pdfStartMargin = Math.round(screenHeight/2);
            pdfEndMargin = 0;
            pdfEndAnimation = Math.round(screenHeight/2) * -1;
            pdfLayoutHeight = screenHeight;
        } else if (toPosition.equals("middle")){
            pdfStartMargin = 0;
            pdfEndMargin = Math.round(screenHeight/2)+70;
            pdfEndAnimation = Math.round(screenHeight/2)+70;
            pdfLayoutHeight = Math.round(screenHeight/2)+70;
        }
        if (pdfLayout.getLayoutParams().height < pdfLayoutHeight) pdfLayout.getLayoutParams().height = pdfLayoutHeight;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) pdfLayout.getLayoutParams();
        params.topMargin = pdfStartMargin;
        pdfLayout.setLayoutParams(params);

        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, pdfEndAnimation);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.setDuration(2000);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                //moving layout to new position
                pdfLayout.clearAnimation();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) pdfLayout.getLayoutParams();
                params.topMargin = pdfEndMargin;
                pdfLayout.setLayoutParams(params);
                pdfLayout.getLayoutParams().height = pdfLayoutHeight;
            }
        });
        pdfLayout.startAnimation(anim);
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
        alphaAnimation.setDuration(1000);
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

    public void fadeLoading(final LinearLayout loadingLayout, boolean inOut){
        if (inOut){
            loadingLayout.setVisibility(View.VISIBLE);
            loadingLayout.clearAnimation();
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(700);
            alphaAnimation.setFillAfter(true);
            loadingLayout.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    loadingLayout.clearAnimation();

                }
            });
        }
    }

    public boolean videoPositionCenter = true;
    public void moveVideo(final LinearLayout videoLayout, final LinearLayout textLayout, boolean removeVideo){
        if (removeVideo && videoPositionCenter){
            final int videoPosition = screenWidth-100;
            TranslateAnimation animVideo = new TranslateAnimation(0, videoPosition, 0, 0);
            animVideo.setInterpolator(new MVAccelerateDecelerateInterpolator());
            animVideo.setDuration(1500);
            animVideo.setFillBefore(true);
            animVideo.setFillAfter(true);
            animVideo.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    //moving layout to new position
                    videoLayout.clearAnimation();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoLayout.getLayoutParams();
                    params.leftMargin = videoPosition;
                    videoLayout.setLayoutParams(params);
                }
            });
            videoLayout.startAnimation(animVideo);

            final int textPosition = videoLayout.getHeight() * -1;
            TranslateAnimation animText = new TranslateAnimation(0, 0, 0, textPosition);
            animText.setInterpolator(new MVAccelerateDecelerateInterpolator());
            animText.setDuration(1500);
            animText.setFillBefore(true);
            animText.setFillAfter(true);
            animText.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    //moving layout to new position
                    textLayout.clearAnimation();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textLayout.getLayoutParams();
                    params.topMargin = textPosition;
                    textLayout.setLayoutParams(params);
                    videoPositionCenter = false;
                }
            });
            textLayout.startAnimation(animText);
        } else if (!removeVideo && !videoPositionCenter){
            final int videoPosition = (screenWidth-100)*-1;
            TranslateAnimation animVideo = new TranslateAnimation(0, videoPosition, 0, 0);
            animVideo.setInterpolator(new MVAccelerateDecelerateInterpolator());
            animVideo.setDuration(1500);
            animVideo.setFillBefore(true);
            animVideo.setFillAfter(true);
            animVideo.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    //moving layout to new position
                    videoLayout.clearAnimation();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoLayout.getLayoutParams();
                    params.leftMargin = 0;
                    videoLayout.setLayoutParams(params);
                }
            });
            videoLayout.startAnimation(animVideo);

            final int textPosition = videoLayout.getHeight();
            TranslateAnimation animText = new TranslateAnimation(0, 0, 0, textPosition);
            animText.setInterpolator(new MVAccelerateDecelerateInterpolator());
            animText.setDuration(1500);
            animText.setFillBefore(true);
            animText.setFillAfter(true);
            animText.setAnimationListener(new TranslateAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    //moving layout to new position
                    textLayout.clearAnimation();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textLayout.getLayoutParams();
                    params.topMargin = 0;
                    textLayout.setLayoutParams(params);
                    videoPositionCenter = true;
                }
            });
            textLayout.startAnimation(animText);
        }
    }

    public static void fadeButton(final ImageButton button, final boolean showHide){
        int fadeFrom = (showHide) ? 1 : 0;
        int fadeTo = (showHide) ? 0 : 1;
        Log.d("fadeButton", "");
        AlphaAnimation alphaAnimation = new AlphaAnimation(fadeFrom, fadeTo);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(false);
        alphaAnimation.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!showHide) button.setVisibility(View.VISIBLE);
                else button.setVisibility(View.GONE);
            }
        });
        button.startAnimation(alphaAnimation);
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
