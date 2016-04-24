package com.codekarhu.whatswithme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by @author ${user} on ${date}
 * <p>
 * ${file_name}
 */
public class RecordCircle extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintRecord = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable micDrawable;
    private float scale;
    private float amplitude;
    private float animateToAmplitude;
    private float animateAmplitudeDiff;
    private long lastUpdateTime;

    public RecordCircle(Context context) {
        super(context);
        paint.setColor(0xff5795cc);
        paintRecord.setColor(0x0d000000);
        micDrawable = getResources().getDrawable(R.drawable.mic_pressed);
    }

    public RecordCircle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint.setColor(0xff5795cc);
        paintRecord.setColor(0x0d000000);
        micDrawable = getResources().getDrawable(R.drawable.mic_pressed);
    }

    public void setAmplitude(double value) {
        animateToAmplitude = (float) Math.min(100, value) / 100.0f;
        animateAmplitudeDiff = (animateToAmplitude - amplitude) / 150.0f;
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float value) {
        scale = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = getMeasuredWidth() / 2;
        int cy = getMeasuredHeight() / 2;
        float sc;
        float alpha;
        if (scale <= 0.5f) {
            alpha = sc = scale / 0.5f;
        } else if (scale <= 0.75f) {
            sc = 1.0f - (scale - 0.5f) / 0.25f * 0.1f;
            alpha = 1;
        } else {
            sc = 0.9f + (scale - 0.75f) / 0.25f * 0.1f;
            alpha = 1;
        }
        long dt = System.currentTimeMillis() - lastUpdateTime;
        if (animateToAmplitude != amplitude) {
            amplitude += animateAmplitudeDiff * dt;
            if (animateAmplitudeDiff > 0) {
                if (amplitude > animateToAmplitude) {
                    amplitude = animateToAmplitude;
                }
            } else {
                if (amplitude < animateToAmplitude) {
                    amplitude = animateToAmplitude;
                }
            }
            invalidate();
        }
        lastUpdateTime = System.currentTimeMillis();
        if (amplitude != 0) {
            canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, (Utils.dp(42) + Utils.dp(20) * amplitude) * scale, paintRecord);
        }
        canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, Utils.dp(42) * sc, paint);
        micDrawable.setBounds(cx - micDrawable.getIntrinsicWidth() / 2, cy - micDrawable.getIntrinsicHeight() / 2, cx + micDrawable.getIntrinsicWidth() / 2, cy + micDrawable.getIntrinsicHeight() / 2);
        micDrawable.setAlpha((int) (255 * alpha));
        micDrawable.draw(canvas);
    }
}
