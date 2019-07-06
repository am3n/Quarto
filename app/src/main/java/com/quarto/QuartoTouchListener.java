package com.quarto;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

class QuartoTouchListener implements View.OnTouchListener {

    private int qid;
    private QuartoListener quartoListener;
    private ScaleAnimation scaleIn, scaleOut;
    private float leftMargin, topMargin;

    QuartoTouchListener(int qid, QuartoListener quartoListener) {
        this.qid = qid;
        this.quartoListener = quartoListener;

        scaleIn =  new ScaleAnimation(
                1f,
                1.2f,
                1f,
                1.2f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );
        scaleIn.setInterpolator(new AccelerateInterpolator());
        scaleIn.setDuration(50);
        scaleIn.setFillAfter(true);

        scaleOut =  new ScaleAnimation(
                1.2f,
                1f,
                1.2f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );
        scaleOut.setInterpolator(new AccelerateInterpolator());
        scaleOut.setDuration(20);
        scaleOut.setFillAfter(true);

    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {

        final float rawX = event.getRawX();
        final float rawY = event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                leftMargin = rawX - lParams.leftMargin;
                topMargin = rawY - lParams.topMargin;
                view.startAnimation(scaleIn);
                break;

            case MotionEvent.ACTION_UP:
                view.startAnimation(scaleOut);
                quartoListener.onDrop(qid, view);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = (int) (rawX - leftMargin);
                layoutParams.topMargin = (int) (rawY - topMargin);
                view.setLayoutParams(layoutParams);
                view.requestLayout();
                break;
        }

        return true;
    }
}