package com.afollestad.silk.utilities;

import java.util.Calendar;

/**
 * Utilities for getting human readable time strings.
 *
 * @author Aidan Follestad (afollestad)
 */
public class TimeUtils {

    /**
     * Gets a human-readable long time string (includes both the time and date, excluded certain parts if possible).
     *
     * @param shortMonth Whether or display a long or short month string (e.g. 'January' or 'Jan').
     */
    public static String toString(Calendar date, boolean includeTime, boolean shortMonth) {
        Calendar now = Calendar.getInstance();
        int hourInt = date.get(Calendar.HOUR);
        int minuteInt = date.get(Calendar.MINUTE);
        String dayStr = getNumberWithSuffix(date.get(Calendar.DAY_OF_MONTH));

        String timeStr = "";
        if (hourInt == 0) timeStr += "12";
        else timeStr += "" + hourInt;
        if (minuteInt < 10) timeStr += ":0" + minuteInt;
        else timeStr += ":" + minuteInt;
        if (date.get(Calendar.AM_PM) == Calendar.AM) timeStr += "AM";
        else timeStr += "PM";

        if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == date.get(Calendar.MONTH)) {
                // Same year, same month
                if (now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
                    // Same year, same month, same day
                    return timeStr;
                } else {
                    // Same year, same month, different day
                    String toReturn = "";
                    if (includeTime) toReturn = timeStr + " ";
                    toReturn += convertMonth(date.get(Calendar.MONTH), shortMonth) + " " + dayStr;
                    return toReturn;
                }
            } else {
                // Different month, same year
                String toReturn = "";
                if (includeTime) toReturn = timeStr + " ";
                toReturn += convertMonth(date.get(Calendar.MONTH), shortMonth) + " " + dayStr;
                return toReturn;
            }
        } else {
            // Different year
            String year = Integer.toString(date.get(Calendar.YEAR));
            String toReturn = "";
            if (includeTime) toReturn = timeStr + " ";
            toReturn += convertMonth(date.get(Calendar.MONTH), shortMonth) + " " + dayStr + ", " + year;
            return toReturn;
        }
    }

    /**
     * Gets a human-readable date string (month, day, and year).
     *
     * @param shortMonth Whether or display a long or short month string (e.g. 'January' or 'Jan').
     */
    public static String toStringDate(Calendar time, boolean shortMonth) {
        Calendar now = Calendar.getInstance();
        String day = getNumberWithSuffix(time.get(Calendar.DAY_OF_MONTH));
        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            // Same year
            if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH)) {
                // Same year, same month
                return convertMonth(time.get(Calendar.MONTH), shortMonth) + " " + day;
            } else {
                // Different month, same year
                return convertMonth(time.get(Calendar.MONTH), shortMonth) + " " + day;
            }
        } else {
            // Different year
            String year = Integer.toString(time.get(Calendar.YEAR));
            return convertMonth(time.get(Calendar.MONTH), shortMonth) + " " + day + ", " + year;
        }
    }

    public static String toStringShort(Calendar time) {
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

    private static String convertMonth(int month, boolean useShort) {
        String monthStr;
        switch (month) {
            default:
                monthStr = "January";
                break;
            case Calendar.FEBRUARY:
                monthStr = "February";
                break;
            case Calendar.MARCH:
                monthStr = "March";
                break;
            case Calendar.APRIL:
                monthStr = "April";
                break;
            case Calendar.MAY:
                monthStr = "May";
                break;
            case Calendar.JUNE:
                monthStr = "June";
                break;
            case Calendar.JULY:
                monthStr = "July";
                break;
            case Calendar.AUGUST:
                monthStr = "August";
                break;
            case Calendar.SEPTEMBER:
                monthStr = "September";
                break;
            case Calendar.OCTOBER:
                monthStr = "October";
                break;
            case Calendar.NOVEMBER:
                monthStr = "November";
                break;
            case Calendar.DECEMBER:
                monthStr = "December";
                break;
        }
        if (useShort) monthStr = monthStr.substring(0, 3);
        return monthStr;
    }

    private static String getNumberWithSuffix(int number) {
        int j = number % 10;
        if (j == 1 && number != 11) {
            return number + "st";
        }
        if (j == 2 && number != 12) {
            return number + "nd";
        }
        if (j == 3 && number != 13) {
            return number + "rd";
        }
        return number + "th";
    }
}