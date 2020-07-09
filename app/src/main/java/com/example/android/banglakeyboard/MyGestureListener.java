package com.example.android.banglakeyboard;

import android.inputmethodservice.Keyboard;

import java.util.List;

import static java.lang.Math.abs;

public class MyGestureListener {

    private MyKeyboardView myKeyboardView;

    long lastKeyPressTime;
    long thisKeyPressTime;
    private Keyboard.Key lastPressedKey;
    private int keyOffset;

    MyGestureListener(MyKeyboardView myKeyboardView) {
        this.myKeyboardView = myKeyboardView;
    }

    public boolean onDown(float x, float y) {
        myKeyboardView.keyPressed = true;
        myKeyboardView.pressedKey = getPressedKey(x,y);
        myKeyboardView.keyOffset = 0;
        myKeyboardView.callSuperOnTouch = true;
        if (myKeyboardView.pressedKey.repeatable) {
            myKeyboardView.preventSwitch();
        }
        return true;
    }

    public boolean onFling(float x, float y, float xDisp, float yDisp) {
        Keyboard.Key pressedKey = getPressedKey(x,y);
        int flingDirection = getFlingDirection(xDisp, yDisp);
        int keyOffset = getKeyOffset(pressedKey, flingDirection);
        return handleFling(pressedKey, keyOffset);
    }

    public boolean onTap(float x, float y) {
        return handleTap(x, y);
    }

    public boolean onLongTap(float x, float y) {
        return onTap(x,y);
    }

    public boolean onMove(float x, float y, float xDisp, float yDisp) {
        Keyboard.Key pressedKey = getPressedKey(x,y);
        int flingDirection = getFlingDirection(xDisp, yDisp);
        myKeyboardView.keyOffset = getKeyOffset(pressedKey, flingDirection);
        myKeyboardView.callSuperOnTouch = false;
        return true;
    }

    public boolean onIgnore() {
        myKeyboardView.ignore();
        return true;
    }

    private boolean handleFling(Keyboard.Key pressedKey, int keyOffset) {
        myKeyboardView.handleSwitch();
        myKeyboardView.pressKey(pressedKey, keyOffset);
        lastPressedKey = null;
        myKeyboardView.keyPressed = false;
        return true;
    }

    private boolean handleTap(float x, float y) {
        myKeyboardView.handleSwitch();
        Keyboard.Key pressedKey = getPressedKey(x, y);
        if (    (lastPressedKey != null) &&
                (pressedKey.codes[0] == lastPressedKey.codes[0]) &&
                (thisKeyPressTime - lastKeyPressTime < 1000) &&
                (pressedKey.codes.length > 1) &&
                (pressedKey.codes[0] > 0)         ) {
            keyOffset = (keyOffset + 1) % pressedKey.codes.length;
            myKeyboardView.simpleIME.deletePreviousCharacter();
        } else {
            keyOffset = 0;
        }
        myKeyboardView.pressKey(pressedKey, keyOffset);
        lastPressedKey = pressedKey;
        myKeyboardView.keyPressed = false;
        return true;
    }

    public void onFinishGesture() {
        myKeyboardView.callSuperOnTouch = true;
    }

    public void onAction() {
        myKeyboardView.invalidate();
    }

    private Keyboard.Key getPressedKey(float x, float y) {
        List<Keyboard.Key> keys = myKeyboardView.getKeyboard().getKeys();
        Keyboard.Key pressedKey = keys.get(0);
        for (Keyboard.Key key : keys) {
            if (key.isInside((int) x, (int) y)) {
                pressedKey = key;
                break;
            }
        }
        return pressedKey;
    }

    private int getFlingDirection(float xDisp, float yDisp) {
        final int center = 0;
        final int left = 1;
        final int up = 2;
        final int right = 3;
        final int down = 4;
        int flingDirection;
        // determine fling direction
        if (abs(xDisp) < 25 && abs(yDisp) < 25) {
            flingDirection = center;
        } else if (abs(xDisp) > abs(yDisp)) {
            if (xDisp > 0) flingDirection = right;
            else flingDirection = left;
        } else {
            if (yDisp > 0) flingDirection = down;
            else flingDirection = up;
        }
        return flingDirection;
    }

    private int getKeyOffset(Keyboard.Key pressedKey, int flingDirection) {
        return flingDirection < pressedKey.codes.length ? flingDirection : 0;
    }
}
