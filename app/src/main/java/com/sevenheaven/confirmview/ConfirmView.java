package com.sevenheaven.confirmview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

/**
 * Created by caifangmao on 15/8/17.
 */
public class ConfirmView extends View {

    public enum ConfirmState{
        ConfirmStateSuccess, ConfirmStateFail, ConfirmStateProgressing
    }

    private int[] colors = {0xFF0099CC, 0xFF99CC00, 0xFFCC0099, 0xFFCC9900, 0xFF9900CC, 0xFF00CC99};
    private int colorCursor = 0;

    private ConfirmState mCurrentConfirmState = ConfirmState.ConfirmStateSuccess;

    private static final long NORMAL_ANIMATION_DURATION = 350L;
    private static final long ENDING_ANGLE_ANIMATION_DURATION = 500L;

    private static final long NORMAL_ANGLE_ANIMATION_DURATION = 1000L;
    private static final long NORMAL_CIRCLE_ANIMATION_DURATION = 2000L;

    private int mStrokeWidth;
    private Path mSuccessPath;
    private PathMeasure mPathMeasure;
    private ArrayList<Path> mRenderPaths;
    private int renderPathsSize;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface ConfirmViewListener{
        public void onConfirmStateChanged(ConfirmState state);
        public void onConfirmAnimationEnd(ConfirmState state);
    }

    private ConfirmViewListener mConfirmViewListener;


    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;

    private int mRadius;
    private int mSignRadius;

    private float mStartAngle;
    private float mEndAngle;
    private float mCircleAngle;
    private RectF oval;

    private float mPhare;

    private ValueAnimator mPhareAnimator;
    private ValueAnimator mStartAngleAnimator;
    private ValueAnimator mEndAngleAnimator;
    private ValueAnimator mCircleAnimator;

    public ConfirmView(Context context){
        this(context, null);
    }

    public ConfirmView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public ConfirmView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        mSuccessPath = new Path();
        mPathMeasure = new PathMeasure(mSuccessPath, false);

        mRenderPaths = new ArrayList<Path>();

        mStrokeWidth = 50;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xFF0099CC);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        oval = new RectF();
    }

    private void initPhareAnimation(){
        mPhareAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        mPhareAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                setPhare(value);
            }
        });

        mPhareAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mConfirmViewListener != null){
                    mConfirmViewListener.onConfirmAnimationEnd(mCurrentConfirmState);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mPhareAnimator.setDuration(NORMAL_ANIMATION_DURATION);
        mPhareAnimator.setInterpolator(new LinearInterpolator());
    }

    private void initAngleAnimation(){
        mStartAngleAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        mEndAngleAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        mCircleAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);

        mStartAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                setStartAngle(value);
            }
        });
        mEndAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                setEndAngle(value);
            }
        });

        mStartAngleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(mCurrentConfirmState == ConfirmState.ConfirmStateProgressing){
                    if (mEndAngleAnimator != null) {

                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                mEndAngleAnimator.start();
                            }
                        }, 400L);
                    }
                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mCurrentConfirmState != ConfirmState.ConfirmStateProgressing && mEndAngleAnimator != null && !mEndAngleAnimator.isRunning() && !mEndAngleAnimator.isStarted()){
                    startPhareAnimation();
                    mStartAngleAnimator.setDuration(ENDING_ANGLE_ANIMATION_DURATION);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mEndAngleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if(mStartAngleAnimator != null){

                    if(mCurrentConfirmState != ConfirmState.ConfirmStateProgressing){
                        mStartAngleAnimator.setDuration(NORMAL_ANIMATION_DURATION);
                    }

                    colorCursor++;

                    if(colorCursor >= colors.length) colorCursor = 0;

                    mPaint.setColor(colors[colorCursor]);


                    mStartAngleAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                setCircleAngle(value);
            }
        });

        mStartAngleAnimator.setDuration(NORMAL_ANGLE_ANIMATION_DURATION);
        mEndAngleAnimator.setDuration(NORMAL_ANGLE_ANIMATION_DURATION);
        mStartAngleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mEndAngleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mCircleAnimator.setDuration(NORMAL_CIRCLE_ANIMATION_DURATION);
        mCircleAnimator.setInterpolator(new LinearInterpolator());
        mCircleAnimator.setRepeatCount(-1);
    }

    public void startPhareAnimation(){
        if(mPhareAnimator == null){
            initPhareAnimation();
        }

        mPhare = 0;

        mPhareAnimator.setFloatValues(0.0F, 1.0F);

        mPhareAnimator.start();
    }

    public void startCircleAnimation(){
        if(mCircleAnimator == null || mStartAngleAnimator == null || mEndAngleAnimator == null){
            initAngleAnimation();
        }

        mStartAngleAnimator.setDuration(NORMAL_ANGLE_ANIMATION_DURATION);

        mStartAngleAnimator.start();
        mEndAngleAnimator.start();
        mCircleAnimator.start();
    }

    public void stopPhareAnimation(){
        if(mPhareAnimator != null){
            mPhareAnimator.end();
        }
    }

    public void setPhare(float phare){
        mPhare = phare;

        updatePhare();

        invalidate();
    }

    public void setStartAngle(float startAngle){
        this.mStartAngle = startAngle;

        Log.e("startAngle", ":" + startAngle);

        invalidate();
    }

    public void setEndAngle(float endAngle){
        this.mEndAngle = endAngle;

        invalidate();
    }

    public void setCircleAngle(float circleAngle){
        this.mCircleAngle = circleAngle;

        invalidate();
    }

    public void setConfirmState(ConfirmState state){
        if(mCurrentConfirmState != state){
            mCurrentConfirmState = state;

            if(mConfirmViewListener != null){
                mConfirmViewListener.onConfirmStateChanged(mCurrentConfirmState);
            }

            if(mPhareAnimator != null && mPhareAnimator.isRunning()){
                mPhareAnimator.end();
            }

            switch(state){
                case ConfirmStateFail:
                case ConfirmStateSuccess:
                    updatePath();
                    if(mCircleAnimator != null && mCircleAnimator.isRunning()){
                        float tempCircleValue = (Float) mCircleAnimator.getAnimatedValue();

                        mCircleAnimator.end();
                        mCircleAngle = tempCircleValue;
                    }
                    break;
                case ConfirmStateProgressing:
                    mCircleAngle = 0;
                    break;
            }

        }
    }

    public void animatedConfirmState(ConfirmState state){
        setConfirmState(state);

        switch(state){
            case ConfirmStateProgressing:
                startCircleAnimation();
                break;
            case ConfirmStateSuccess:
            case ConfirmStateFail:
                if((mStartAngleAnimator == null || !mStartAngleAnimator.isRunning() || !mStartAngleAnimator.isStarted()) &&
                        (mEndAngleAnimator == null || !mEndAngleAnimator.isRunning() || !mEndAngleAnimator.isStarted())){
                    mStartAngle = 360;
                    mEndAngle = 0;
                    startPhareAnimation();
                }
                break;
        }
    }

    private void updatePhare(){
        if(mSuccessPath != null && renderPathsSize > 0){

            float seg = 1.0F / renderPathsSize;

            int i = 0;

            do{

                float offset = mPhare - seg * i;

                offset = offset < 0 ? 0 : offset;

                offset *= renderPathsSize;

                Log.d("i:" + i + ",seg:" + seg, "offset:" + offset + ", mPhare:" + mPhare + ", size:" + renderPathsSize);

                boolean success = mPathMeasure.getSegment(0, offset * mPathMeasure.getLength(), mRenderPaths.get(i), true);

                if(success) mRenderPaths.get(i).rLineTo(0, 0);

                Log.d("success", success ? "yes" : "no");

                PathMeasure p = new PathMeasure(mRenderPaths.get(i), false);

                Log.d("pathMeasure", p.getLength() + "");

                i++;
            }while(renderPathsSize > 1 && mPathMeasure.nextContour() && i < renderPathsSize);

            if(renderPathsSize > 1) mPathMeasure.setPath(mSuccessPath, false);
        }
    }

    private void updatePath(){

        int offset = (int) (mSignRadius * 0.15F);

        switch(mCurrentConfirmState){
            case ConfirmStateSuccess:
                mSuccessPath.reset();
                mSuccessPath.moveTo(mCenterX - mSignRadius, mCenterY + offset);
                mSuccessPath.lineTo(mCenterX - offset, mCenterY + mSignRadius - offset);
                mSuccessPath.lineTo(mCenterX + mSignRadius, mCenterY - mSignRadius + offset);

                renderPathsSize = 1;

                break;
            case ConfirmStateFail:
                mSuccessPath.reset();

                float failRadius = mSignRadius * 0.8F;

                mSuccessPath.moveTo(mCenterX - failRadius, mCenterY - failRadius);
                mSuccessPath.lineTo(mCenterX + failRadius, mCenterY + failRadius);

                mSuccessPath.moveTo(mCenterX + failRadius, mCenterY - failRadius);
                mSuccessPath.lineTo(mCenterX - failRadius, mCenterY + failRadius);

                renderPathsSize = 2;

                break;
            default:
                mSuccessPath.reset();
        }

        mPathMeasure.setPath(mSuccessPath, false);

        mRenderPaths.clear();
        do{
            mRenderPaths.add(new Path());
        }while(mPathMeasure.nextContour());

        //set path again because we just called nextContour method for measurement
        mPathMeasure.setPath(mSuccessPath, false);
    }

    public float getPhare(){
        return mPhare;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mCenterX = w / 2;
        mCenterY = h / 2;

        mRadius = mCenterX > mCenterY ? mCenterY : mCenterX;
        mSignRadius = (int) (mRadius * 0.55F);

        int realRadius = mRadius - (mStrokeWidth / 2);

        oval.left = mCenterX - realRadius;
        oval.top = mCenterY - realRadius;
        oval.right = mCenterX + realRadius;
        oval.bottom = mCenterY + realRadius;

        updatePath();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        switch(mCurrentConfirmState){
            case ConfirmStateFail:
            case ConfirmStateSuccess:
                for(int i = 0; i < renderPathsSize; i++){
                    Path p = mRenderPaths.get(i);

                    if(p != null){
                        canvas.drawPath(p, mPaint);
                    }
                }
            case ConfirmStateProgressing:

                float offsetAngle = mCircleAngle * 360;

                float startAngle = mEndAngle * 360;

                float sweepAngle = mStartAngle * 360;

                if(startAngle == 360) startAngle = 0;

                sweepAngle = sweepAngle - startAngle;

                startAngle += offsetAngle;

                if(sweepAngle < 0) sweepAngle = 1;

                canvas.drawArc(oval, startAngle, sweepAngle, false, mPaint);
                break;
        }

    }
}
