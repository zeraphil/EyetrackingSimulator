package com.julia.Eyetracking.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
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
     * @param normalizedPosition
     * @param pupilDiameter
     */
    public void updateEye(boolean id, PointF normalizedPosition, float pupilDiameter)
    {
        if (id) //false is left eye
        {
            leftEye.setPosition(normalizedPosition);
            leftEye.setPupilDiameter(pupilDiameter);
            leftEye.setVisible(true);
        }
        else
        {
            rightEye.setPosition(normalizedPosition);
            rightEye.setPupilDiameter(pupilDiameter);
            rightEye.setVisible(true);
        }

        invalidate();
    }

    /**
     * Method to clear the view from eye data
     */
    public void clearDrawView()
    {
        leftEye = new DrawableEye();
        rightEye = new DrawableEye();
        invalidate();
    }

    /**
     * Set the paints and other parameters of the eye objects
     */
    private void initialize()
    {
        leftEyePaint = new Paint();
        leftEyePaint.setColor(Color.BLUE);
        leftEyePaint.setAlpha(127);
        leftEyePaint.setAntiAlias(true);
        rightEyePaint = new Paint();
        rightEyePaint.setColor(Color.RED);
        rightEyePaint.setAlpha(127);
        rightEyePaint.setAntiAlias(true);

        leftEye = new DrawableEye();
        rightEye = new DrawableEye();
    }


    /**
     * Override draw to draw the eyes with the updated data parameters
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (leftEye != null && leftEye.isVisible()) {
            canvas.drawCircle(leftEye.getPosition().x*this.getWidth(), leftEye.getPosition().y *this.getHeight(), leftEye.getPupilDiameter(), leftEyePaint);
        }
        if(rightEye != null && rightEye.isVisible()) {
            canvas.drawCircle(rightEye.getPosition().x *this.getWidth(), rightEye.getPosition().y * this.getHeight(), rightEye.getPupilDiameter(), rightEyePaint);
        }


    }

}
