package com.pie.demo.view.weiget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.pie.demo.R;


public class VoiceImgView extends android.support.v7.widget.AppCompatButton {

    private int width;
    private int heigh;
    private GradientDrawable backDrawable;
    private boolean isMorphing;
    private int startAngle;
    private Paint paint;
    private Paint paint1;
    private ValueAnimator arcValueAnimator;
    private String title;

    public VoiceImgView(Context context) {
        super(context);
        init(context);
    }

    public VoiceImgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        isMorphing = false;
        backDrawable = new GradientDrawable();
        int colorDrawable = context.getResources().getColor(R.color.colorblue);
        backDrawable.setColor(colorDrawable);
        backDrawable.setCornerRadius(120);
        setBackground(backDrawable);

//        setText("开始录音");
        title = getText().toString().trim();
        setTextColor(Color.WHITE);

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.white));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(2);

        paint1 = new Paint();
        paint1.setColor(getResources().getColor(R.color.colorblue));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heighMode = MeasureSpec.getMode(heightMeasureSpec);
        int heighSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }
        if (heighMode == MeasureSpec.EXACTLY) {
            heigh = heighSize;
        }

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        RectF rectF = new RectF(getWidth() * 5 / 12, getHeight() / 7, getWidth() * 7 / 12, getHeight() - getHeight() / 7);

        if (isMorphing == true) {
            canvas.drawArc(rectF, startAngle, 270, false, paint);
        } else {
            canvas.drawArc(rectF, 0, 0, false, paint1);
        }
    }

    public void startAnim() {
        setText("");

        isMorphing = true;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(width, heigh);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int leftOffset = (width - value) / 2;
                int rightOffset = width - leftOffset;

                backDrawable.setBounds(leftOffset, 0, rightOffset, heigh);
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(backDrawable, "cornerRadius", 120, heigh / 2);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(valueAnimator, objectAnimator);
        animatorSet.start();

        showArc();
    }

    private void showArc() {
        arcValueAnimator = ValueAnimator.ofInt(0, 1080);
        arcValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        arcValueAnimator.setInterpolator(new LinearInterpolator());
        arcValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arcValueAnimator.setDuration(3000);
        arcValueAnimator.start();


    }

    public void stopAnim() {

        setText(title);

        isMorphing = false;
        if (arcValueAnimator != null) {
            arcValueAnimator.cancel();
        }
        invalidate();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(heigh, width);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();

                int leftOffset = (width - value) / 2;
                int rightOffset = width - leftOffset;

                backDrawable.setBounds(leftOffset, 0, rightOffset, heigh);
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(backDrawable, "cornerRadius", heigh / 2, 120);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(valueAnimator, objectAnimator);
        animatorSet.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isMorphing) {
                stopAnim();
                if (onVoiceButtonInterface != null) {
                    onVoiceButtonInterface.onStopVoice();
                }
            } else {
                startAnim();
                if (onVoiceButtonInterface != null) {
                    onVoiceButtonInterface.onStartVoice();
                }
            }

            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public interface onVoiceButtonInterface {
        void onStartVoice();

        void onStopVoice();
    }

    public onVoiceButtonInterface onVoiceButtonInterface;

    public void setOnVoiceButtonInterface(onVoiceButtonInterface onVoiceButtonInterface) {
        this.onVoiceButtonInterface = onVoiceButtonInterface;
    }
}
