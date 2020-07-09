package com.example.android.banglakeyboard;

import android.util.Log;
import android.view.MotionEvent;

import static java.lang.Math.abs;

public class MyGestureDetector {

    private MyGestureListener myGestureListener;

    private MotionEvent downEvent;

    public MyGestureDetector(MyGestureListener myGestureListener) {
        this.myGestureListener = myGestureListener;
    }

    public boolean onTouchEvent(MotionEvent me) {
        int action = me.getActionMasked();
        myGestureListener.onAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("MotionEvent","ACTION_DOWN");
                reportAction(me, "down");
                return handleDown(me);
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("MotionEvent","ACTION_POINTER_DOWN");
                reportAction(me,"down");
                return handlePointerDown(me);
            case MotionEvent.ACTION_MOVE:
                Log.d("MotionEvent","ACTION_MOVE");
                return handleMove(me);
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("MotionEvent","ACTION_POINTER_UP");
                reportAction(me,"up");
                return handleUp(me);
            case MotionEvent.ACTION_UP:
                Log.d("MotionEvent","ACTION_UP");
                reportAction(me,"up");
                myGestureListener.onFinishGesture();
                return handleUp(me);
            case MotionEvent.ACTION_CANCEL:
                Log.d("MotionEvent","ACTION_CANCEL");
                return false;
            case MotionEvent.ACTION_OUTSIDE:
                Log.d("MotionEvent","ACTION_OUTSIDE");
                return false;
            default:
                return false;
        }
    }

    private void reportAction(MotionEvent me, String action) {
        int pId = getActionId(me);
        String pointers = "";
        for (int i = 0; i < me.getPointerCount(); i++) {
            pointers = pointers + ", " + i + ":" + me.getPointerId(i);
        }
        Log.d("Gestures","Pointer " + pId + " " + action + pointers);
    }

    private boolean handleMove(MotionEvent me) {
        return downEvent == null || handleFling(downEvent, me, false);
    }

    private boolean handleDown(MotionEvent me) {
        downEvent = MotionEvent.obtain(me); // very important to obtain a copy of it
        int pointerIndex = me.getActionIndex();
        float x = me.getX(pointerIndex);
        float y = me.getY(pointerIndex);
        return myGestureListener.onDown(x,y);
    }

    private boolean handlePointerDown(MotionEvent me) {
        if (downEvent != null) {
            handleFling(downEvent, me,true);
        }
        return handleDown(me);
    }

    private boolean handleUp(MotionEvent me) {
        int upId = getActionId(me);
        if (downEvent == null) {
            Log.d("Gestures","Ignored: " + upId);
            return myGestureListener.onIgnore();
        }
        int downId = getActionId(downEvent);
        if (downId == upId) {
            Log.d("Gestures","Matched: " + downId);
            handleFling(downEvent, me,true);
            downEvent = null;
            return true;
        } else {
            Log.d("Gestures","Ignored: (" + downId + ", " + upId + ")");
            return myGestureListener.onIgnore();
        }
    }

    private int getActionId(MotionEvent me) {
        int pointerIndex = me.getActionIndex();
        return me.getPointerId(pointerIndex);
    }

    private boolean handleFling(MotionEvent me1, MotionEvent me2, boolean pressKey) {
        int pId = getActionId(me1);
        int pIdx1 = me1.findPointerIndex(pId);
        int pIdx2 = me2.findPointerIndex(pId);
        float xDown = me1.getX(pIdx1);
        float yDown = me1.getY(pIdx1);
        float xUp = me2.getX(pIdx2);
        float yUp = me2.getY(pIdx2);
        float xDisp = xUp - xDown, yDisp = yUp - yDown;
        long time = me2.getEventTime() - me1.getEventTime();
        Log.d("Gestures","Fling: (" + xDisp + ", " + yDisp + "). Time: " + time + ". ");
        if (!pressKey) {
            return myGestureListener.onMove(xDown, yDown, xDisp, yDisp);
        }
        myGestureListener.lastKeyPressTime = myGestureListener.thisKeyPressTime;
        myGestureListener.thisKeyPressTime = me2.getEventTime();
        if (abs(xDisp) > 25 || abs(yDisp) > 25) {
            return myGestureListener.onFling(xDown, yDown, xDisp, yDisp);
        } else if (time < 500) {
            return myGestureListener.onTap(xDown,yDown);
        } else {
            return myGestureListener.onLongTap(xDown,yDown);
        }
    }
}
