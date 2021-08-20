package com.yocn.meida.util;

public class TimeUtil {
    public static String getTimeString(long ts) {
        int SS = 60;
        int MM = 60 * 60;
        int HH = 60 * 60 * 60;
        int second = (int) (ts % SS);
        int minute = (int) (ts / SS) % SS;
        int hour = (int) (ts / MM);
        String secondString = second < 10 ? "0" + second : String.valueOf(second);
        String minuteString = minute < 10 ? "0" + minute : String.valueOf(minute);
        String msString = minuteString + ":" + secondString;
        return hour == 0 ? msString : hour + ":" + msString;
    }
}
