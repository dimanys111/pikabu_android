package com.dima.pikabu.src;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.dima.pikabu.MainActivity.activity;


public class GifImageView extends View {

    private InputStream mInputStream;
    private Movie mMovie;
    private int mWidth, mHeight;
    private long mStart;
    private float mScale;
    private Context mContext;

    public GifImageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        if (attrs.getAttributeName(1).equals("background")) {
            int id = Integer.parseInt(attrs.getAttributeValue(1).substring(1));
            setGifImageResource(id);
        }
    }

    private void init() {
        mMovie = Movie.decodeStream(mInputStream);
        mWidth = mMovie.width();
        mHeight = mMovie.height();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        requestLayout();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) Math.ceil((float) width * (float) mHeight / (float) mWidth);
        setMeasuredDimension(width, height);
			/*
			 * Calculate horizontal scaling
			 */
        float scaleH = 1f;
        int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

        if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
            scaleH = (float) mWidth / (float) maximumWidth;
        }
			/*
			 * calculate vertical scaling
			 */
        float scaleW = 1f;
        int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

        if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
            int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
            scaleW = (float) mHeight / (float) maximumHeight;
        }

			/*
			 * calculate overall scale
			 */
        mScale = 1f / Math.max(scaleH, scaleW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = SystemClock.uptimeMillis();

        if (mStart == 0) {
            mStart = now;
        }

        if (mMovie != null) {

            int duration = mMovie.duration();
            if (duration == 0) {
                duration = 1000;
            }

            int relTime = (int) ((now - mStart) % duration);

            mMovie.setTime(relTime);

            canvas.save();
            canvas.scale(mScale, mScale);
            mMovie.draw(canvas, 0, 0);
            canvas.restore();
            invalidate();
        }
    }

    public void setGifImageResource(int id) {
        mInputStream = mContext.getResources().openRawResource(id);
        init();
    }

    public void setGifImageUri(Uri uri) {
        try {
            mInputStream = mContext.getContentResolver().openInputStream(uri);
            init();
        } catch (FileNotFoundException e) {
            Log.e("GIfImageView", "File not found");
        }
    }
}
