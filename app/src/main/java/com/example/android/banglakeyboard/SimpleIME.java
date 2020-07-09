package com.example.android.banglakeyboard;

import android.app.Dialog;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class SimpleIME extends InputMethodService implements
        KeyboardView.OnKeyboardActionListener {

    private InputMethodManager mInputMethodManager;

    private MyKeyboardView kv;
    private MyKeyboard kbd_ben_primary;
    private MyKeyboard kbd_eng_primary;
    private MyKeyboard kbd_ben_kar;
    private MyKeyboard kbd_ben_sym_1;
    private MyKeyboard kbd_ben_sym_2;

    private MyKeyboard curKeyboard;

    public boolean handled;

    public boolean preventSwitch;

    public boolean caps = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onInitializeInterface() {
        kbd_ben_primary = new MyKeyboard(this,R.xml.ben_primary);
        kbd_eng_primary = new MyKeyboard(this,R.xml.eng_primary);
        kbd_ben_kar = new MyKeyboard(this,R.xml.ben_kar);
        kbd_ben_sym_1 = new MyKeyboard(this,R.xml.ben_sym_1);
        kbd_ben_sym_2 = new MyKeyboard(this,R.xml.ben_sym_2);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_TEXT:
                curKeyboard = kbd_ben_primary;
                break;
            default:
                curKeyboard = kbd_eng_primary;
        }
    }

    @Override
    public View onCreateInputView() {
        kv = (MyKeyboardView) getLayoutInflater().inflate(R.layout.keyboard,null);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        kv.setIME(this);
        return kv;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setKeyboard(curKeyboard);
        kv.initializePaint();
    }

    @Override
    public void onUpdateSelection(int oldSelStart,
                                  int oldSelEnd,
                                  int newSelStart,
                                  int newSelEnd,
                                  int candidatesStart,
                                  int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart,
                newSelEnd, candidatesStart, candidatesEnd);
        if (!preventSwitch) {
            if (newSelEnd == newSelStart) switchKeyboard();
            else if (kv != null && kv.getKeyboard() == kbd_ben_kar) setKeyboard(kbd_ben_primary);
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (handled) return;
        InputConnection ic = getCurrentInputConnection();
        char code;
        switch(primaryCode) {
            case 0:
                return;
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    ic.deleteSurroundingText(1, 0);
                } else {
                    ic.commitText("",1);
                }
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                if (kv != null) {
                    Keyboard current = kv.getKeyboard();
                    if (current == kbd_ben_primary || current == kbd_ben_kar) {
                        setKeyboard(kbd_eng_primary);
                        caps = false;
                    } else {
                        setKeyboard(kbd_ben_primary);
                    }
                }
                break;
            case MyKeyboard.KEYCODE_MODE_CHANGE_KAR:
                if (kv != null) {
                    Keyboard current = kv.getKeyboard();
                    if (current == kbd_ben_primary) {
                        setKeyboard(kbd_ben_kar);
                    } else {
                        setKeyboard(kbd_ben_primary);
                    }
                }
                break;
            case MyKeyboard.KEYCODE_MODE_CHANGE_NUM:
                if (kv != null) {
                    Keyboard current = kv.getKeyboard();
                    if (current == kbd_ben_primary || current == kbd_ben_kar) {
                        setKeyboard(kbd_ben_sym_1);
                    } else if (current == kbd_ben_sym_1) {
                        setKeyboard(kbd_ben_sym_2);
                    } else if (current == kbd_ben_sym_2) {
                        setKeyboard(kbd_ben_sym_1);
                    }
                }
                break;
            case MyKeyboard.KEYCODE_LANGUAGE_SWITCH:
                mInputMethodManager.switchToNextInputMethod(getToken(),false);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                kv.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER));
                break;
            case MyKeyboard.KEYCODE_JO_FOLA:
                code = (char) 2509;
                ic.commitText(String.valueOf(code),1);
                code = (char) 2479;
                ic.commitText(String.valueOf(code),1);
                break;
            case MyKeyboard.KEYCODE_RO_FOLA:
                code = (char) 2509;
                ic.commitText(String.valueOf(code),1);
                code = (char) 2480;
                ic.commitText(String.valueOf(code),1);
                break;
            case MyKeyboard.KEYCODE_REF:
                code = (char) 2480;
                ic.commitText(String.valueOf(code),1);
                code = (char) 2509;
                ic.commitText(String.valueOf(code),1);
                break;
            default:
                if (Character.isLowerCase(primaryCode)) {
                    if (caps) {
                        primaryCode = primaryCode - 32;
                    }
                }
                code = (char) primaryCode;
                ic.commitText(String.valueOf(code),1);
        }
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void setKeyboard(Keyboard keyboard) {
        kv.setKeyboard(keyboard);
    }

    public void switchKeyboard() {
        InputConnection ic = getCurrentInputConnection();
        if (kv != null) {
            Keyboard current = kv.getKeyboard();
            CharSequence previousChar = ic.getTextBeforeCursor(1,0);
            if (!TextUtils.isEmpty(previousChar) && Borno.isConsonant((int) previousChar.charAt(0))) {
                if (current == kbd_ben_primary) {
                    setKeyboard(kbd_ben_kar);
                }
            } else if (current == kbd_ben_kar) {
                setKeyboard(kbd_ben_primary);
            }
        }
    }

    protected void deletePreviousCharacter() {
        InputConnection ic = getCurrentInputConnection();
        ic.deleteSurroundingText(1,0);
    }

    @Override public void onPress(int primaryCode) {}
    @Override public void onRelease(int primaryCode) {}
    @Override public void onText(CharSequence text) {}
    @Override public void swipeUp() {}
    @Override public void swipeDown() {}
    @Override public void swipeLeft() {}
    @Override public void swipeRight() {}
}
