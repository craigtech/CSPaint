package project.cspaint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;

    // Paint encapsulates the color and style information about drawn elements
    private final Paint mPaint;

    // store strokes drawn in array
    private final ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    // constructors
    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        // below methods smooths the drawings
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 0xff=255 in decimal
        mPaint.setAlpha(0xff);
    }

    // instantiate the bitmap
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        // set initial brush color
        currentColor = Color.BLACK;
        // set initial brush size
        strokeWidth = 20;
    }

    // set current color of stroke
    public void setColor(int color) {
        currentColor = color;
    }

    // set stroke width
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void undo() {
        //checks if empty, the remove method will return an error
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void clear() {
        //checks if empty, the remove method will return an error
        if (paths.size() != 0) {
            paths.clear();
            invalidate();
        }
    }

    // this methods returns the current bitmap
    public Bitmap save() {
        if (paths.size() != 0)
            return mBitmap;
        else return null;
    }

    // method for drawing
    @Override
    protected void onDraw(Canvas canvas) {
        // save the current state of the canvas before drawing the background
        canvas.save();
        // default colour of the canvas
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);

        // iterate over the list of paths and draw each on the canvas
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);

            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    // methods to manage touch response

    // create a new Stroke and add it to the paths list
    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        paths.add(fp);

        //  remove any curve or line from the path
        mPath.reset();
        // set the starting point of the line being drawn
        mPath.moveTo(x, y);
        // save the current coordinates of the finger
        mX = x;
        mY = y;
    }

    // checks if finger movement exceeds tolerance defined
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        // quadTo() smooths curves
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // draws the line to the end position
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    // determine type of motion and call appropriate method
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}