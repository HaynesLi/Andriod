package com.paltech.dronesncars.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ScanResultsView extends View {

    Paint mRectPaint;
    RectF[] mRectList;
    Bitmap mBitmap;
    ArrayList<double[]> mBoxList;

    public ScanResultsView(Context context) {
        super(context);
        init(null);
    }

    public ScanResultsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void init(@Nullable AttributeSet set) {
        mRectPaint = new Paint();
        mRectPaint.setStrokeWidth(10f);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.RED);
    }

    public void setBgAndBBL(Bitmap bitmap, ArrayList<double[]> bBoxList){
        mBitmap = bitmap;
        mBoxList = bBoxList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {

//        canvas.setBitmap(mBitmap);
        }
        if (mBoxList != null) {
            for (int i = 0 ; i < mBoxList.size(); i++) {
                RectF rectF = new RectF();
                rectF.left = (float) (getWidth()* mBoxList.get(i)[0]);
                rectF.top = (float) (getHeight()* mBoxList.get(i)[1]);
                rectF.right = (float) (getWidth()* mBoxList.get(i)[2]);
                rectF.bottom = (float) (getHeight()* mBoxList.get(i)[3]);

                canvas.drawRect(rectF, mRectPaint);
                invalidate();
            }
        }

    }

}
