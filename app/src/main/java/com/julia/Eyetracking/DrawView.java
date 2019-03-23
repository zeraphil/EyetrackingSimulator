package com.julia.Eyetracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;


/**
 * Custom view class to visually represent eye tracking data on the background view of the app
 */
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

    /**
     * Update the drawable eyes to represent the generated positions
     * @param id
     * @param position
     * @param pupilDiameter
     */
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

    /**
     * Set the paints and other parameters of the eye objects
     */
    private void initialize()
    {
        leftEyePaint = new Paint();
        leftEyePaint.setColor(Color.BLUE);
        rightEyePaint = new Paint();
        rightEyePaint.setColor(Color.RED);

        leftEye = new DrawableEye();
        rightEye = new DrawableEye();
    }


    /**
     * Override draw to draw the eyes with the updated data parameters
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (leftEye != null && leftEye.isVisible()) {
            canvas.drawCircle(leftEye.getPosition().x, leftEye.getPosition().y, leftEye.getPupilDiameter(), leftEyePaint);
        }
        if(rightEye != null && rightEye.isVisible()) {
            canvas.drawCircle(rightEye.getPosition().x, rightEye.getPosition().y, rightEye.getPupilDiameter(), rightEyePaint);
        }

    }

}
