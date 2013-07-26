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
    public static String getFriendlyTimeLong(Calendar date, boolean includeTime) {
        Calendar now = Calendar.getInstance();
        int hourInt = date.get(Calendar.HOUR);
        int minuteInt = date.get(Calendar.MINUTE);
        int dayInt = date.get(Calendar.DAY_OF_MONTH);

        String timeStr = "";
        String dayStr;
        if (hourInt == 0) timeStr += "12";
        else if (hourInt < 10) timeStr += ":0" + hourInt;
        else timeStr += ":" + hourInt;
        if (minuteInt < 10) timeStr += ":0" + minuteInt;
        else timeStr += ":" + minuteInt;
        if (date.get(Calendar.AM_PM) == Calendar.AM) timeStr += "AM";
        else timeStr += "PM";
        if (dayInt < 10) dayStr = "0" + dayInt;
        else dayStr = "" + dayInt;

        if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == date.get(Calendar.MONTH)) {
                // Same year, same month
                if (now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
                    // Same year, same month, same day
                    return timeStr;
                } else {
                    // Same year, same month, different day
                    String toReturn = convertMonth(date.get(Calendar.MONTH)) + " " + dayStr;
                    if (includeTime) toReturn += " " + timeStr;
                    return toReturn;
                }
            } else {
                // Different month, same year
                String toReturn = convertMonth(date.get(Calendar.MONTH)) + " " + dayStr + " " + timeStr;
                if (includeTime) toReturn += " " + timeStr;
                return toReturn;
            }
        } else {
            // Different year
            String year = Integer.toString(date.get(Calendar.YEAR));
            String toReturn = convertMonth(date.get(Calendar.MONTH)) + " " + dayStr + ", " + year + " " + timeStr;
            if (includeTime) toReturn += " " + timeStr;
            return toReturn;
        }
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