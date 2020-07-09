package com.example.android.banglakeyboard;

public class Borno {

    public static boolean isConsonant(int primaryCode) {
        return (primaryCode >= 2453 && primaryCode <= 2489) ||
                (primaryCode >= 2524 && primaryCode <= 2527);
    }

    public static boolean isVowel(int primaryCode) {
        return (primaryCode >= 0x985 && primaryCode <= 0x994);
    }

    public static int toKar(int primaryCode) {
        if (primaryCode == 0x985) return 0x9CD;
        else if (primaryCode == 0x9CD) return 0x985;
        else return primaryCode + 56;
    }
}
