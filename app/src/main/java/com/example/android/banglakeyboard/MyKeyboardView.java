package com.example.android.banglakeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.List;

public class MyKeyboardView extends KeyboardView {

    private MyGestureDetector myGestureDetector;
    SimpleIME simpleIME;

    private Paint paintKey, paintHighlight, paintSmall, paintLarge;
    private Rect textBounds;

    boolean keyPressed;
    Keyboard.Key pressedKey;
    int keyOffset;
    boolean callSuperOnTouch = true;

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MyGestureListener myGestureListener = new MyGestureListener(this);
        myGestureDetector = new MyGestureDetector(myGestureListener);
    }

    void setIME(SimpleIME simpleIME) {
        this.simpleIME = simpleIME;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        simpleIME.handled = false;
        this.myGestureDetector.onTouchEvent(me);
        return !callSuperOnTouch || super.onTouchEvent(me);
    }

    void ignore() {
        simpleIME.handled = true;
    }

    void preventSwitch() {
        simpleIME.preventSwitch = true;
    }

    void handleSwitch() {
        if (simpleIME.preventSwitch) {
            simpleIME.switchKeyboard();
            simpleIME.preventSwitch = false;
        }
    }

    void pressKey(Keyboard.Key pressedKey, int keyOffset) {
        if (!pressedKey.repeatable) {
            simpleIME.onKey(pressedKey.codes[keyOffset], pressedKey.codes);
            simpleIME.handled = true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        List<Keyboard.Key>  keys = getKeyboard().getKeys();

        for (Keyboard.Key key : keys) {
            if (keyPressed && pressedKey.codes[0] > 32 && key.codes[0] == pressedKey.codes[0]) {
                float x = key.x + key.width/2;
                float y = key.y + key.height*0.54f;
                float x1 = x-key.width/2;
                float x2 = x+key.width/2;
                float y1 = y-key.height/2;
                float y2 = y+key.height/2;
                canvas.drawRect(x1,y1,x2,y2,paintKey);
                Path path = getPath(x,y,x1,y1,x2,y2);
                canvas.drawPath(path,paintHighlight);
                if (keyOffset == 0) {
                    canvas.drawRect(x1,y1,x2,y2,paintHighlight);
                    drawTextCentered(canvas, paintLarge,key.label + "", x, key.y + key.height*0.54f);
                } else {
                    drawKey(canvas, key, false);
                }
            } else {
                drawKey(canvas, key, true);
            }
        }
    }

    public void initializePaint() {
        textBounds = new Rect();
        paintKey = new Paint();
        paintKey.setColor(Color.LTGRAY);
        paintHighlight = new Paint();
        paintHighlight.setColor(Color.BLUE);
        paintSmall = new Paint();
        paintSmall.setAntiAlias(true);
        paintSmall.setColor(Color.WHITE);
        paintSmall.setTextAlign(Paint.Align.CENTER);
        paintSmall.setTypeface(Typeface.MONOSPACE);
        paintSmall.getTextBounds("ক",0,1,textBounds);
        float fontHeight = textBounds.height();
        float keyHeight = getKeyboard().getKeys().get(0).height;
        float fontHeightFrac = fontHeight / keyHeight;
        float textSizeSmall = paintSmall.getTextSize() * 0.15f / fontHeightFrac;
        float textSizeLarge = paintSmall.getTextSize() * 0.32f / fontHeightFrac;
        paintSmall.setTextSize(textSizeSmall);
        paintLarge = new Paint(paintSmall);
        paintLarge.setTextSize(textSizeLarge);
    }

    private Path getPath(float x, float y, float x1, float y1, float x2, float y2) {
        Path path = new Path();
        path.moveTo(x,y);
        switch (keyOffset) {
            case 0:
                break;
            case 1:
                path.lineTo(x1,y2);
                path.lineTo(x1,y1);
                break;
            case 2:
                path.lineTo(x1,y1);
                path.lineTo(x2,y1);
                break;
            case 3:
                path.lineTo(x2,y1);
                path.lineTo(x2,y2);
                break;
            case 4:
                path.lineTo(x2,y2);
                path.lineTo(x1,y2);
                break;
            default:
        }
        path.close();
        return path;
    }

    private void drawKey(Canvas canvas, Keyboard.Key key, boolean drawAll) {
        for (int i = 1; i < key.codes.length; i++) {
            if (!drawAll) {
                if (i != keyOffset) continue;
            }
            if (key.codes[i] > 0) {
                int shiftX, shiftY;
                if (i%2 == 1) {
                    shiftY = 0; shiftX = i-2;
                } else {
                    shiftX = 0; shiftY = i-3;
                }
                float x = key.x + key.width/2 + shiftX*((float)0.3*key.width);
                float y = key.y + (key.height*0.54f) + shiftY*((float)0.3*key.height);
                int text = key.codes[i];
                if (Character.isLowerCase(text) && simpleIME.caps)
                    text = Character.toUpperCase(text);
                if (drawAll) {
                    drawTextCentered(canvas, paintSmall,(char) text + "", x, y);
                } else {
                    drawTextCentered(canvas, paintLarge,(char) text + "", x, y);
                }
            }
        }
    }

    private void drawTextCentered(Canvas canvas, Paint paint, String text, float x, float y) {
        paint.getTextBounds(text,0,text.length(),textBounds);
        canvas.drawText(text, x,y-textBounds.exactCenterY(), paint);
    }

}
