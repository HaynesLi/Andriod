package com.paltech.dronesncars.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.paltech.dronesncars.computing.ScanResult;
import com.paltech.dronesncars.computing.XMLParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ScanResultsView extends View {

    private final float TOUCH_BIAS = 40;
    private final int PIXEL_ARTIFACT_LIMIT = 10;
    private final String head = "<object>" +
            "<name>Sorrel</name>" +
            "<pose>Unspecified</pose>" +
            "<truncated>0</truncated>" +
            "<dificult>0</dificult>";
    private final String end = "</bndbox>" +
            "</object>";
    Paint mTextPaint;
    Paint mRectPaint;
    Paint mHighlightPaint;
    ArrayList<Rect> mRectList;
    boolean selected = false;
    int index_resize = -1;
    int edited_corner = -1;
    static final int PIC_WIDTH = 1920;
    static final int PIC_HEIGHT = 1080;

    private final static int TEXT_X = 0;
    private final static int TEXT_Y = 5;
    private final static int TEXT_WIDTH = 160;
    private final static int TEXT_HEIGHT = 30;

    private float scale_width_result_image, scale_height_result_image;

    public void setScale_width_result_image(float scale_width_result_image) {
        this.scale_width_result_image = scale_width_result_image;
    }

    public void setScale_height_result_image(float scale_height_result_image) {
        this.scale_height_result_image = scale_height_result_image;
    }

    Edit_state state;
    ArrayList<ScanResult> mScanResults;
    HashSet<Integer> mHighlightSet;

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
        mRectPaint.setStrokeWidth(8f);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.BLUE);

        mHighlightPaint = new Paint();
        mHighlightPaint.setStrokeWidth(8f);
        mHighlightPaint.setStyle(Paint.Style.STROKE);
        mHighlightPaint.setColor(Color.RED);

        mTextPaint = new Paint();

        state = Edit_state.None;
    }

    public void setScanResults(ArrayList<ScanResult> scanResults) {
        this.mScanResults = scanResults;
        mRectList = new ArrayList<>();
        mHighlightSet = new HashSet<>();
    }

    public boolean highlight(int position) {
        if (mScanResults != null) {
            if (mHighlightSet.contains(position)) {
                mHighlightSet.remove(position);
            } else {
                mHighlightSet.add(position);
            }
        }
        invalidate();
        return mHighlightSet.size() > 0;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScanResults != null) {
            mRectList.clear();
            for (int i = 0; i < mScanResults.size(); i++) {
                ScanResult result = mScanResults.get(i);
                //draw rectangle
                Rect rect = mScanResults.get(i).getRect();
                if (mHighlightSet.contains(i)) {
                    canvas.drawRect(rect, mHighlightPaint);
                } else {
                    canvas.drawRect(rect, mRectPaint);
                }
                mRectList.add(rect);

                //draw text background
                Path mPath = new Path();
                RectF mRectF = new RectF(rect.left + TEXT_WIDTH < getWidth() ? rect.left : rect.right - TEXT_WIDTH, rect.top > TEXT_HEIGHT ? rect.top - TEXT_HEIGHT : rect.bottom, rect.left + TEXT_WIDTH < getWidth() ? rect.left + TEXT_WIDTH : rect.right, rect.top > TEXT_HEIGHT ? rect.top : rect.bottom + TEXT_HEIGHT);
                mPath.addRect(mRectF, Path.Direction.CW);
                mTextPaint.setColor(Color.MAGENTA);
                canvas.drawPath(mPath, mTextPaint);

                //draw text words
                mTextPaint.setColor(Color.WHITE);
                mTextPaint.setStrokeWidth(0);
                mTextPaint.setStyle(Paint.Style.FILL);
                mTextPaint.setTextSize(32);
                canvas.drawText(String.format("%s  %.2f", result.getClassName(), result.getScore()), rect.left + TEXT_WIDTH < getWidth() ? rect.left + TEXT_X : rect.right - TEXT_WIDTH + TEXT_X, rect.top > TEXT_HEIGHT ? rect.top - TEXT_Y : rect.bottom + 5 * TEXT_Y, mTextPaint);

                invalidate();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);
        int x_d, y_d, x_m, y_m;
        switch (state) {
            case None:
                return value;
            case Add:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x_d = (int) event.getX();
                        y_d = (int) event.getY();
                        ScanResult result = new ScanResult("Weed", 1f, new Rect(x_d, y_d, x_d, y_d));
                        mScanResults.add(result);
                        invalidate();
                    case MotionEvent.ACTION_MOVE:
                        x_m = (int) event.getX();
                        y_m = (int) event.getY();
                        ScanResult current = mScanResults.get(mScanResults.size() - 1);
                        x_d = current.getRect().left;
                        y_d = current.getRect().top;
                        int left = x_d;
                        int top = y_d;
                        int right = x_m;
                        int bottom = y_m;
                        current.setRect(new Rect(left, top, right, bottom));
                        invalidate();
                        return true;
                }

            case Remove:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x_d = (int) event.getX();
                    y_d = (int) event.getY();
                    int index = -1;
                    for (int i = 0; i < mScanResults.size(); i++) {
                        if (touchedInRect(i, x_d, y_d)) {
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0 && index <= mScanResults.size()) {
                        mScanResults.remove(index);
                        invalidate();
                    }
                    return true;
                } else {
                    return value;
                }

            case Resize:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x_d = (int) event.getX();
                        y_d = (int) event.getY();
                        for (int i = 0; i < mScanResults.size(); i++) {
                            if (touchedNearRect(i, x_d, y_d)) {
                                index_resize = i;
                                break;
                            }
                        }
                        if (index_resize >= 0 && index_resize <= mScanResults.size()) {
                            Rect resized = mScanResults.get(index_resize).getRect();
                            double[] dis = new double[4];
                            selected = true;
                            dis[0] = Math.pow(x_d - resized.left, 2) +
                                    Math.pow(y_d - resized.top, 2);
                            dis[1] = Math.pow(x_d - resized.right, 2) +
                                    Math.pow(y_d - resized.top, 2);
                            dis[2] = Math.pow(x_d - resized.left, 2) +
                                    Math.pow(y_d - resized.bottom, 2);
                            dis[3] = Math.pow(x_d - resized.right, 2) +
                                    Math.pow(y_d - resized.bottom, 2);
                            double dis_min = Math.min(Math.min(dis[0], dis[1]),
                                    Math.min(dis[2], dis[3]));
                            if (dis_min == dis[0]) {
                                edited_corner = 0;
                            } else if (dis_min == dis[1]) {
                                edited_corner = 1;
                            } else if (dis_min == dis[2]) {
                                edited_corner = 2;
                            } else {
                                edited_corner = 3;
                            }
                            int left = resized.left;
                            int top = resized.top;
                            int right = resized.right;
                            int bottom = resized.bottom;
                            switch (edited_corner) {
                                case 0:
                                    left = x_d;
                                    top = y_d;
                                    break;
                                case 1:
                                    right = x_d;
                                    top = y_d;
                                    break;
                                case 2:
                                    left = x_d;
                                    bottom = y_d;
                                    break;
                                case 3:
                                    right = x_d;
                                    bottom = y_d;
                                    break;
                            }
                            mScanResults.get(index_resize).setRect(new Rect(left, top, right, bottom));
                            invalidate();
                        } else {
                            return value;
                        }
                    case MotionEvent.ACTION_MOVE:
                        if (selected) {
                            x_m = (int) event.getX();
                            y_m = (int) event.getY();
                            Rect resized = mScanResults.get(index_resize).getRect();
                            int left = resized.left;
                            int top = resized.top;
                            int right = resized.right;
                            int bottom = resized.bottom;
                            switch (edited_corner) {
                                case 0:
                                    left = x_m;
                                    top = y_m;
                                    break;
                                case 1:
                                    right = x_m;
                                    top = y_m;
                                    break;
                                case 2:
                                    left = x_m;
                                    bottom = y_m;
                                    break;
                                case 3:
                                    right = x_m;
                                    bottom = y_m;
                                    break;
                            }
                            mScanResults.get(index_resize).setRect(new Rect(left, top, right, bottom));
                            invalidate();
                            return true;
                        } else {
                            return value;
                        }
                    case MotionEvent.ACTION_UP:
                        int x_u, y_u;
                        x_u = (int) event.getX();
                        y_u = (int) event.getY();
                        if (selected) {
                            Rect resized = mScanResults.get(index_resize).getRect();
                            int left = resized.left;
                            int top = resized.top;
                            int right = resized.right;
                            int bottom = resized.bottom;
                            switch (edited_corner) {
                                case 0:
                                    left = x_u;
                                    top = y_u;
                                    break;
                                case 1:
                                    right = x_u;
                                    top = y_u;
                                    break;
                                case 2:
                                    left = x_u;
                                    bottom = y_u;
                                    break;
                                case 3:
                                    right = x_u;
                                    bottom = y_u;
                                    break;
                            }
                            mScanResults.get(index_resize).setRect(new Rect(left, top, right, bottom));
                            selected = false;
                            edited_corner = -1;
                            invalidate();
                            index_resize = -1;
                            return true;
                        } else {
                            return value;
                        }
                }
        }
        return value;
    }


    boolean touchedInRect(int i, float x, float y) {
        Rect rect = mScanResults.get(i).getRect();
        return ((x >= rect.left && x <= rect.right) || (x >= rect.right && x <= rect.left)) && ((y >= rect.top && y <= rect.bottom) || (y >= rect.bottom && y <= rect.top));
    }

    boolean touchedNearRect(int i, float x, float y) {
        Rect rect = mScanResults.get(i).getRect();
        float left = Math.max(0, Math.min(rect.left, rect.left) - TOUCH_BIAS);
        float top = Math.max(0, Math.min(rect.top, rect.bottom) - TOUCH_BIAS);
        float right = Math.min(getWidth(), Math.max(rect.left, rect.left) + TOUCH_BIAS);
        float bottom = Math.min(getHeight(), Math.max(rect.top, rect.bottom) + TOUCH_BIAS);
        return (x >= left && x <= right) && (y >= top && y <= bottom);
    }

    public String export() {
        trim();
        String str = "";
        if (mScanResults.size() != 0) {
            for (ScanResult scanResult : mScanResults) {
                String cur = head;
                cur = cur + "<className>" + scanResult.getClassName() + "</className>";
                cur = cur + "<confidence>" + scanResult.getScore().toString() + "</confidence>";
                cur = cur + "<bndbox>";
                Rect rect = scanResult.getRect();
                cur = cur + "<xmin>" + (int) (rect.left * scale_width_result_image) + "</xmin>";
                cur = cur + "<ymin>" + (int) (rect.top * scale_height_result_image) + "</ymin>";
                cur = cur + "<xmax>" + (int) (rect.right * scale_width_result_image) + "</xmax>";
                cur = cur + "<ymax>" + (int) (rect.bottom * scale_height_result_image) + "</ymax>";
                cur = cur + end;
                str = str + cur;
            }
        }
        return str;
    }

    private void trim() {
        if (mScanResults.size() != 0) {
            for (int i = 0; i < mScanResults.size(); i++) {
                ScanResult scanResult = mScanResults.get(i);
                Rect rect = scanResult.getRect();
                int left = rect.left;
                int top = rect.top;
                int right = rect.right;
                int bottom = rect.bottom;
                if (left > right) {
                    int tmp = right;
                    right = left;
                    left = tmp;
                }
                if (top > bottom) {
                    int tmp = bottom;
                    bottom = top;
                    top = tmp;
                }
                if (right < left + PIXEL_ARTIFACT_LIMIT || bottom < top + PIXEL_ARTIFACT_LIMIT) {
                    mScanResults.remove(i);
                    i--;
                } else {
                    scanResult.setRect(new Rect(left, top, right, bottom));
                }
            }
        }
    }
}

//public class ScanResultsView extends View {
//
//    private final float TOUCH_BIAS = 40;
//    private final int PIXEL_ARTIFACT_LIMIT = 10;
//    private final String head = "<object>" +
//            "<name>Sorrel</name>" +
//            "<pose>Unspecified</pose>" +
//            "<truncated>0</truncated>" +
//            "<dificult>0</dificult>" +
//            "<bndbox>";
//    private final String end = "</bndbox>" +
//            "</object>";
//    Paint mRectPaint;
//    Paint mTextPaint;
//    ArrayList<RectF> mRectList;
//    boolean selected = false;
//    int index_resize = -1;
//    int edited_corner = -1;
//    static final int PIC_WIDTH = 1920;
//    static final int PIC_HEIGHT = 1080;
//
//    Edit_state state;
//
//
//    ArrayList<int[]> mBoxList;
//    ArrayList<ScanResult> mScanResults;
//
//    public ScanResultsView(Context context) {
//        super(context);
//        init(null);
//    }
//
//    public ScanResultsView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        init(attrs);
//    }
//
//    public void init(@Nullable AttributeSet set) {
//        mRectPaint = new Paint();
//        mRectPaint.setStrokeWidth(10f);
//        mRectPaint.setStyle(Paint.Style.STROKE);
//        mRectPaint.setColor(Color.RED);
//        mTextPaint = new Paint();
//        state = Edit_state.None;
//
//    }
//
//    public void setBBL(ArrayList<int[]> bBoxList) {
//        mBoxList = bBoxList;
//        mRectList = new ArrayList<>();
//    }
//
//    public void setScanResults(ArrayList<ScanResult> scanResults) {
//        this.mScanResults = scanResults;
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        if (mBoxList != null) {
//            mRectList.clear();
//
//            for (int i = 0; i < mBoxList.size(); i++) {
//                RectF rectF = new RectF();
//                rectF.left = (float) (getWidth() * mBoxList.get(i)[0] / PIC_WIDTH);
//                rectF.top = (float) (getHeight() * mBoxList.get(i)[1] / PIC_HEIGHT);
//                rectF.right = (float) (getWidth() * mBoxList.get(i)[2] / PIC_WIDTH);
//                rectF.bottom = (float) (getHeight() * mBoxList.get(i)[3] / PIC_HEIGHT);
//                mRectList.add(rectF);
//                canvas.drawRect(rectF, mRectPaint);
//
//                invalidate();
//            }
//        }
//
//    }
//
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        boolean value = super.onTouchEvent(event);
//        float x_d, y_d, x_m, y_m;
//        switch (state) {
//            case None:
//                return value;
//            case Add:
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        x_d = event.getX();
//                        y_d = event.getY();
//                        int[] mNewBBox = new int[4];
//                        mNewBBox[0] = (int) (PIC_WIDTH * x_d / getWidth());
//                        mNewBBox[1] = (int) (PIC_HEIGHT * y_d / getHeight());
//                        mNewBBox[2] = (int) (PIC_WIDTH * x_d / getWidth());
//                        mNewBBox[3] = (int) (PIC_HEIGHT * y_d / getHeight());
//                        mBoxList.add(mNewBBox);
//                        invalidate();
//                    case MotionEvent.ACTION_MOVE:
//                        x_m = event.getX();
//                        y_m = event.getY();
//                        int[] added = mBoxList.get(mBoxList.size() - 1);
//                        int x_max, y_max, x_tmp, y_tmp;
//                        x_max = (int) (PIC_WIDTH * x_m / getWidth());
//                        y_max = (int) (PIC_HEIGHT * y_m / getHeight());
//                        added[2] = x_max;
//                        added[3] = y_max;
//                        invalidate();
//                        return true;
//                }
//            case Remove:
//
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    x_d = event.getX();
//                    y_d = event.getY();
//                    int index = -1;
//                    for (int i = 0; i < mRectList.size(); i++) {
//                        if (touchedInRect(i, x_d, y_d)) {
//                            index = i;
//                            break;
//                        }
//                    }
//                    if (index >= 0 && index <= mBoxList.size()) {
//                        mBoxList.remove(index);
//                        invalidate();
////                      this.scanResultsFragment.notify(state, true);
////                    state = Edit_state.None;
//                    }
//                    return true;
//                } else {
//                    return value;
//                }
//            case Resize:
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        x_d = event.getX();
//                        y_d = event.getY();
//                        for (int i = 0; i < mRectList.size(); i++) {
//                            if (touchedNearRect(i, x_d, y_d)) {
//                                index_resize = i;
//                                break;
//                            }
//                        }
//                        if (index_resize >= 0 && index_resize <= mBoxList.size()) {
//                            double[] dis = new double[4];
//                            selected = true;
//                            dis[0] = Math.pow(x_d - mRectList.get(index_resize).left, 2) +
//                                    Math.pow(y_d - mRectList.get(index_resize).top, 2);
//                            dis[1] = Math.pow(x_d - mRectList.get(index_resize).right, 2) +
//                                    Math.pow(y_d - mRectList.get(index_resize).top, 2);
//                            dis[2] = Math.pow(x_d - mRectList.get(index_resize).left, 2) +
//                                    Math.pow(y_d - mRectList.get(index_resize).bottom, 2);
//                            dis[3] = Math.pow(x_d - mRectList.get(index_resize).right, 2) +
//                                    Math.pow(y_d - mRectList.get(index_resize).bottom, 2);
//                            double dis_min = Math.min(Math.min(dis[0], dis[1]),
//                                    Math.min(dis[2], dis[3]));
//                            if (dis_min == dis[0]) {
//                                edited_corner = 0;
//                            } else if (dis_min == dis[1]) {
//                                edited_corner = 1;
//                            } else if (dis_min == dis[2]) {
//                                edited_corner = 2;
//                            } else {
//                                edited_corner = 3;
//                            }
//                            int x_b = (int) (PIC_WIDTH * x_d / getWidth());
//                            int y_b = (int) (PIC_HEIGHT * y_d / getHeight());
//                            int[] resized = mBoxList.get(index_resize);
//                            switch (edited_corner) {
//                                case 0:
//                                    resized[0] = x_b;
//                                    resized[1] = y_b;
//                                    break;
//                                case 1:
//                                    resized[2] = x_b;
//                                    resized[1] = y_b;
//                                    break;
//                                case 2:
//                                    resized[0] = x_b;
//                                    resized[3] = y_b;
//                                    break;
//                                case 3:
//                                    resized[2] = x_b;
//                                    resized[3] = y_b;
//                                    break;
//                            }
//
////                            double[] tmp = mBoxList.get(index_resize);
////                            mBoxList.set(index_resize, mBoxList.get(mBoxList.size() - 1));
////                            mBoxList.set(mBoxList.size() - 1,tmp);
//                        } else {
//
//                            return value;
//                        }
//                    case MotionEvent.ACTION_MOVE:
//                        if (selected) {
//                            x_m = event.getX();
//                            y_m = event.getY();
//                            int x_b = (int) (PIC_WIDTH * x_m / getWidth());
//                            int y_b = (int) (PIC_HEIGHT * y_m / getHeight());
//                            int[] resized = mBoxList.get(index_resize);
//                            switch (edited_corner) {
//                                case 0:
//                                    resized[0] = x_b;
//                                    resized[1] = y_b;
//                                    break;
//                                case 1:
//                                    resized[2] = x_b;
//                                    resized[1] = y_b;
//                                    break;
//                                case 2:
//                                    resized[0] = x_b;
//                                    resized[3] = y_b;
//                                    break;
//                                case 3:
//                                    resized[2] = x_b;
//                                    resized[3] = y_b;
//                                    break;
//                                default:
//                                    break;
//                            }
//                            invalidate();
//                            return true;
//                        } else {
//                            return value;
//                        }
//                    case MotionEvent.ACTION_UP:
//                        int x_u, y_u;
//                        x_u = (int) (PIC_WIDTH * event.getX() / getWidth());
//                        y_u = (int) (PIC_HEIGHT * event.getY() / getHeight());
//                        if (selected) {
//                            int[] resized = mBoxList.get(index_resize);
//                            switch (edited_corner) {
//                                case 0:
//                                    resized[0] = x_u;
//                                    resized[1] = y_u;
//                                    break;
//                                case 1:
//                                    resized[2] = x_u;
//                                    resized[1] = y_u;
//                                    break;
//                                case 2:
//                                    resized[0] = x_u;
//                                    resized[3] = y_u;
//                                    break;
//                                case 3:
//                                    resized[2] = x_u;
//                                    resized[3] = y_u;
//                                    break;
//                                default:
//                                    break;
//                            }
//                            selected = false;
//                            edited_corner = -1;
//                            invalidate();
//                            index_resize = -1;
//                            return true;
//                        } else {
//                            return value;
//                        }
//                }
//        }
//        return value;
//    }
//
//
//    boolean touchedInRect(int i, float x, float y) {
//        RectF rectF = mRectList.get(i);
//        return ((x <= rectF.left && x >= rectF.right) || (x >= rectF.left && x <= rectF.right)) && ((y >= rectF.top && y <= rectF.bottom) || (y <= rectF.top && y >= rectF.bottom));
//    }
//
//    boolean touchedNearRect(int i, float x, float y) {
//        RectF rectF = mRectList.get(i);
//        float left = Math.max(0, Math.min(rectF.left, rectF.right) - TOUCH_BIAS);
//        float top = Math.max(0, Math.min(rectF.top, rectF.bottom) - TOUCH_BIAS);
//        float right = Math.min(getWidth(), Math.max(rectF.left, rectF.right) + TOUCH_BIAS);
//        float bottom = Math.min(getHeight(), Math.max(rectF.top, rectF.bottom) + TOUCH_BIAS);
//        return (x >= left && x <= right) && (y >= top && y <= bottom);
//    }
//
//    public String export() {
//        trim();
//        String str = "";
//        if (mBoxList.size() != 0) {
//            for (int[] mBox : mBoxList) {
//                String cur = head;
//                cur = cur + "<xmin>" + mBox[0] + "</xmin>";
//                cur = cur + "<ymin>" + mBox[1] + "</ymin>";
//                cur = cur + "<xmax>" + mBox[2] + "</xmax>";
//                cur = cur + "<ymax>" + mBox[3] + "</ymax>";
//                cur = cur + end;
//                str = str + cur;
//            }
//        }
//        return str;
//    }
//
//    private void trim() {
//        if (mBoxList.size() != 0) {
//            for (int i = 0; i < mBoxList.size(); i++) {
//                int[] mBox = mBoxList.get(i);
//                int left = mBox[0];
//                int top = mBox[1];
//                int right = mBox[2];
//                int bottom = mBox[3];
//                if (left > right) {
//                    int tmp = right;
//                    right = left;
//                    left = tmp;
//                }
//                if (top > bottom) {
//                    int tmp = bottom;
//                    bottom = top;
//                    top = tmp;
//                }
//                if (right < left + PIXEL_ARTIFACT_LIMIT || bottom < top + PIXEL_ARTIFACT_LIMIT) {
//                    mBoxList.remove(i);
//                    i--;
//                } else {
//                    mBox[0] = left;
//                    mBox[1] = top;
//                    mBox[2] = right;
//                    mBox[3] = bottom;
//                }
//
//            }
//        }
//    }
//
//}

enum Edit_state {
    None, Add, Remove, Resize;
}
