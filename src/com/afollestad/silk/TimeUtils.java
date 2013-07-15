package com.afollestad.silk;

import java.util.Calendar;

/**
 * Utilities for getting human readable time strings.
 *
 * @author Aidan Follestad (afollestad)
 */
public class TimeUtils {

    /**
     * Gets a human-readable long time string (includes both the time and date, excluded certain parts if possible).
     */
    public static String getFriendlyTimeLong(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return getFriendlyDate(cal);
    }

    /**
     * Gets a human-readable long time string (includes both the time and date, excluded certain parts if possible).
     */
    public static String getFriendlyTimeLong(Calendar time) {
        Calendar now = Calendar.getInstance();
        String am_pm = "AM";
        if (time.get(Calendar.AM_PM) == Calendar.PM) am_pm = "PM";
        String day = "";
        if (time.get(Calendar.DAY_OF_MONTH) < 10) day = "0";
        day += time.get(Calendar.DAY_OF_MONTH);
        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH)) {
                // Same year, same month
                if (now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)) {
                    // Same year, same month, same day
                    String minute = Integer.toString(time.get(Calendar.MINUTE));
                    int hour = time.get(Calendar.HOUR);
                    if (hour == 0) {
                        // 12AM will be 0 since it's the first hour in the day
                        hour = 12;
                    }
                    if (minute.length() == 1) {
                        // Add a zero before the minute if it's below 10 (1 character in length), since 12:1 looks stupid compared to 12:01.
                        minute = ("0" + minute);
                    }
                    return hour + ":" + minute + am_pm;
                } else {
                    // Same year, same month, different day
                    return convertMonth(time.get(Calendar.MONTH)) + " " + day;
                }
            } else {
                // Different month, same year
                return convertMonth(time.get(Calendar.MONTH)) + " " + day;
            }
        } else {
            // Different year
            String year = Integer.toString(time.get(Calendar.YEAR));
            return convertMonth(time.get(Calendar.MONTH)) + " " + day + ", " + year;
        }
    }

    /**
     * Gets a human-readable date string (month, day, and year).
     *
     * @param time
     * @return
     */
    public static String getFriendlyDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return getFriendlyDate(cal);
    }

    /**
     * Gets a human-readable date string (month, day, and year).
     */
    public static String getFriendlyDate(Calendar time) {
        Calendar now = Calendar.getInstance();
        String day = "";
        if (time.get(Calendar.DAY_OF_MONTH) < 10) day = "0";
        day += time.get(Calendar.DAY_OF_MONTH);
        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH)) {
                // Same year, same month
                return convertMonth(time.get(Calendar.MONTH)) + " " + day;
            } else {
                // Different month, same year
                return convertMonth(time.get(Calendar.MONTH)) + " " + day;
            }
        } else {
            // Different year
            String year = Integer.toString(time.get(Calendar.YEAR));
            return convertMonth(time.get(Calendar.MONTH)) + " " + day + ", " + year;
        }
    }

    /**
     * Gets a human readable short time string, that indicates how long it's been since a specified time.
     * The format is similar to what most Twitter clients do (e.g. 1s, 1m, 1h, 1d, 1w, 1m, 1y).
     */
    public static String getFriendlyTimeShort(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return getFriendlyTimeShort(cal);
    }

    /**
     * Gets a human readable short time string, that indicates how long it's been since a specified time.
     * The format is similar to what most Twitter clients do (e.g. 1s, 1m, 1h, 1d, 1w, 1m, 1y).
     */
    public static String getFriendlyTimeShort(Calendar time) {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH)) {
                // Same year, same month
                if (now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)) {
                    // Same month, same day
                    if (now.get(Calendar.HOUR) == time.get(Calendar.HOUR)) {
                        // Same day, same hour
                        if (now.get(Calendar.MINUTE) == time.get(Calendar.MINUTE)) {
                            // Same hour, same minute
                            return (now.get(Calendar.SECOND) - time.get(Calendar.SECOND)) + "s";
                        } else {
                            // Different minute
                            return (now.get(Calendar.MINUTE) - time.get(Calendar.MINUTE)) + "m";
                        }
                    } else {
                        // Same month, different hour
                        return (now.get(Calendar.HOUR_OF_DAY) - time.get(Calendar.HOUR_OF_DAY)) + "h";
                    }
                } else {
                    // Same year, same month, different day
                    int totalDays = now.get(Calendar.DAY_OF_YEAR) - time.get(Calendar.DAY_OF_YEAR);
                    if (totalDays < 7) {
                        // Less than a week ago, return days
                        return totalDays + "d";
                    } else if ((totalDays % 7) == 0) {
                        return (totalDays / 7) + "w";
                    }
                    // Return both weeks and days
                    int weeks = totalDays / 7;
                    int days = totalDays % 7;
                    return weeks + "w" + days + "d";
                }
            } else {
                // Different month, same year
                return (now.get(Calendar.MONTH) - time.get(Calendar.MONTH)) + "m";
            }
        } else {
            // Different year
            return (now.get(Calendar.YEAR) - time.get(Calendar.YEAR)) + "y";
        }
    }

    private static String convertMonth(int month) {
        switch (month) {
            default:
                return "Jan";
            case Calendar.FEBRUARY:
                return "Feb";
            case Calendar.MARCH:
                return "Mar";
            case Calendar.APRIL:
                return "Apr";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "Jun";
            case Calendar.JULY:
                return "Jul";
            case Calendar.AUGUST:
                return "Aug";
            case Calendar.SEPTEMBER:
                return "Sep";
            case Calendar.OCTOBER:
                return "Oct";
            case Calendar.NOVEMBER:
                return "Nov";
            case Calendar.DECEMBER:
                return "Dec";
        }
    }
}