package com.example.android.banglakeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;

public class MyKeyboard extends Keyboard {

    public static final int KEYCODE_MODE_CHANGE_KAR = -5001;
    public static final int KEYCODE_MODE_CHANGE_NUM = -5002;
    public static final int KEYCODE_JO_FOLA = -5003;
    public static final int KEYCODE_RO_FOLA = -5004;
    public static final int KEYCODE_REF     = -5005;
    public static final int KEYCODE_LANGUAGE_SWITCH = -101;

    public MyKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }
}
