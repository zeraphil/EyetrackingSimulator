package com.julia.Eyetracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {
    private DrawableEye leftEye = new DrawableEye();
    private DrawableEye rightEye = new DrawableEye();

    private Paint leftEyePaint;
    private Paint rightEyePaint;

    public DrawView(Context context, AttributeSet attrs){
        super(context, attrs);
        initialize();
    }

    public DrawView(Context context) {
        super(context);
        initialize();
    }

    private void initialize()
    {
        leftEyePaint = new Paint();
        leftEyePaint.setColor(Color.BLUE);
        rightEyePaint = new Paint();
        rightEyePaint.setColor(Color.RED);

        leftEye = new DrawableEye();
        rightEye = new DrawableEye();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (leftEye != null && leftEye.isVisible()) {
            canvas.drawCircle(leftEye.getPosition().x, leftEye.getPosition().y, leftEye.getPupilDiameter(), leftEyePaint);
        }
        if(rightEye != null && rightEye.isVisible()) {
            canvas.drawCircle(rightEye.getPosition().x, rightEye.getPosition().y, rightEye.getPupilDiameter(), rightEyePaint);
        }

    }

    public void updateEye(boolean id, PointF position, float pupilDiameter)
    {
        if (id) //false is left eye
        {
            leftEye.setPosition(position);
            leftEye.setPupilDiameter(pupilDiameter);
            leftEye.setVisible(true);
        }
        else
        {
            rightEye.setPosition(position);
            rightEye.setPupilDiameter(pupilDiameter);
            rightEye.setVisible(true);
        }

        invalidate();
    }
}
