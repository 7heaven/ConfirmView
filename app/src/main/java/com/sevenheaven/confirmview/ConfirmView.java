package com.sevenheaven.confirmview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

/**
 * Created by caifangmao on 15/8/17.
 */
public class ConfirmView extends View {

    public enum ConfirmState{
        ConfirmStateSuccess, ConfirmStateFail, ConfirmStateProgressing
    }

    private ConfirmState mCurrentConfirmState = ConfirmState.ConfirmStateSuccess;

    private long NORMAL_ANIMATION_DURATION = 500L;

    private int mStrokeWidth;
    private Path mSuccessPath;
    private PathMeasure mPathMeasure;
    private ArrayList<Path> mRenderPaths;
    private int renderPathsSize;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;

    private int mRadius;
    private int mSignRadius;

    private int mStartAngle;
    private int mEndAngle;
    private RectF oval;

    private float mPhare;

    private ValueAnimator mPhareAnimator;

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

        mStrokeWidth = 20;

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

        mPhareAnimator.setDuration(NORMAL_ANIMATION_DURATION);
        mPhareAnimator.setInterpolator(new LinearInterpolator());
    }

    public void startPhareAnimation(){
        if(mPhareAnimator == null){
            initPhareAnimation();
        }

        mPhare = 0;

        mPhareAnimator.setFloatValues(0.0F, 1.0F);

        mPhareAnimator.start();
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

    public void setConfirmState(ConfirmState state){
        if(mCurrentConfirmState != state){
            mCurrentConfirmState = state;
            if(mPhareAnimator != null && mPhareAnimator.isRunning()){
                mPhareAnimator.end();
            }

            updatePath();
        }
    }

    public void animatedConfirmState(ConfirmState state){
        setConfirmState(state);
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
                canvas.drawArc(oval, mStartAngle, mEndAngle - mStartAngle, false, mPaint);
                break;
        }

    }
}
