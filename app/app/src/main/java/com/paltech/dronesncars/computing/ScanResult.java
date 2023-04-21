package com.paltech.dronesncars.computing;

import android.graphics.Rect;

public class ScanResult {
    String className;
    Float score;
    Rect rect;

    public ScanResult(String className, Float output, Rect rect) {
        this.className = className;
        this.score = output;
        this.rect = rect;
    }

    public String getClassName() {
        return className;
    }

    public Float getScore() {
        return score;
    }

    public Rect getRect() {
        return rect;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}