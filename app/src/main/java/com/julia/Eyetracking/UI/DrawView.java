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
            this.leftEye.setPosition(normalizedPosition);
            this.leftEye.setPupilDiameter(pupilDiameter);
            this.leftEye.setVisible(true);
        }
        else
        {
            this.rightEye.setPosition(normalizedPosition);
            this.rightEye.setPupilDiameter(pupilDiameter);
            this.rightEye.setVisible(true);
        }

        invalidate();
    }

    /**
     * Method to clear the view from eye data
     */
    public void clearDrawView()
    {
        this.leftEye = new DrawableEye();
        this.rightEye = new DrawableEye();
        invalidate();
    }

    /**
     * Set the paints and other parameters of the eye objects
     */
    private void initialize()
    {
        this.leftEyePaint = new Paint();
        this.leftEyePaint.setColor(Color.BLUE);
        this.leftEyePaint.setAlpha(127);
        this.leftEyePaint.setAntiAlias(true);
        this.rightEyePaint = new Paint();
        this.rightEyePaint.setColor(Color.RED);
        this.rightEyePaint.setAlpha(127);
        this.rightEyePaint.setAntiAlias(true);

        this.leftEye = new DrawableEye();
        this.rightEye = new DrawableEye();
    }


    /**
     * Override draw to draw the eyes with the updated data parameters
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (this.leftEye != null && this.leftEye.isVisible()) {
            canvas.drawCircle(this.leftEye.getPosition().x*this.getWidth(), (1-this.leftEye.getPosition().y) *this.getHeight(), this.leftEye.getPupilDiameter(), this.leftEyePaint);
        }
        if(this.rightEye != null && this.rightEye.isVisible()) {
            canvas.drawCircle(this.rightEye.getPosition().x *this.getWidth(), (1-this.rightEye.getPosition().y) * this.getHeight(), this.rightEye.getPupilDiameter(), this.rightEyePaint);
        }


    }

}
