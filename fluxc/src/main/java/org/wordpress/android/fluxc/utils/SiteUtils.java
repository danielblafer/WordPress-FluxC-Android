package org.wordpress.android.fluxc.utils;

import android.support.annotation.NonNull;

import org.wordpress.android.fluxc.model.SiteModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.apache.commons.lang3.StringUtils.split;

public class SiteUtils {

    private static final long MILLISECONDS = 1000;
    private static final long ONE_DAY_IN_SECONDS = 86400;
    private static final long DAYS_IN_A_WEEK = 7;
    // According to google, the average number of days in a month is 30.42
    private static final double DAYS_IN_A_MONTH = 30.42;
    private static final double DAYS_IN_A_YEAR = 365;

    /**
     * Given a {@link SiteModel} and a {@link String} compatible with {@link SimpleDateFormat},
     * returns a formatted date that accounts for the site's timezone setting.
     * <p>
     * Imported from WordPress-Android with some modifications.
     */
    public static @NonNull String getCurrentDateTimeForSite(@NonNull SiteModel site, @NonNull String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ROOT);
        return getCurrentDateTimeForSite(site, dateFormat);
    }

    /**
     * Given a {@link SiteModel}, {@link String} and a {@link Date} compatible with {@link SimpleDateFormat},
     * returns a formatted date that accounts for the site's timezone setting.
     */
    public static @NonNull String getDateTimeForSite(@NonNull SiteModel site,
                                                     @NonNull String pattern,
                                                     @NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ROOT);
        return getDateTimeForSite(site, dateFormat, date);
    }

    /**
     * Given a {@link SiteModel} and a {@link SimpleDateFormat},
     * returns a formatted date that accounts for the site's timezone setting.
     * <p>
     * Imported from WordPress-Android with some modifications.
     */
    public static @NonNull String getCurrentDateTimeForSite(@NonNull SiteModel site,
                                                            @NonNull SimpleDateFormat dateFormat) {
        Date date = new Date();
        return getDateTimeForSite(site, dateFormat, date);
    }

    /**
     * While not ideal, this function calculates the number of "enum type" based on the difference in milliseconds
     *  between each date.
     * The idea is to count how many days are in each "enum type" and multiply to the number of seconds in a day. Since
     *  we actually want the number of milliseconds in each "enum type" we multiply by an extra 1000.
     * After that we simply divide by the the original millisecond difference to the calculated milliseconds and we
     *  have the correct value
     *
     * This solution does not account for leap years or daylight savings.
     * @param startDate The starting date
     * @param endDate The ending date
     * @param timeEnum The actual value of type we want (Days, Weeks, Months, Years)
     * @return an integer value of the type we want
     */
    public static int calculateTimeDifferencesBetweenDates(Date startDate, Date endDate, TimeEnum timeEnum) {
        long differenceInTimeMS = endDate.getTime() - startDate.getTime();

        if (timeEnum == TimeEnum.DAYS) {
            return (int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS));
        } else if (timeEnum == TimeEnum.WEEKS) {
            return (int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_WEEK));
        } else if (timeEnum == TimeEnum.MONTHS) {
            return (int) ((long) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_MONTH)));
        } else if (timeEnum == TimeEnum.YEARS) {
            return (int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_YEAR));
        } else return 0;
    }

    /**
     * Creates a calendar based on the passed date and checks which week that calendar is on
     * @param date the date in question
     * @return the week the calendar is set in
     */
    public static int getWeekNumberInCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }


    /**
     * Given a {@link SiteModel}, {@link SimpleDateFormat} and a {@link Date},
     * returns a formatted date that accounts for the site's timezone setting.
     */
    private static @NonNull String getDateTimeForSite(@NonNull SiteModel site,
                                                      @NonNull SimpleDateFormat dateFormat,
                                                      @NonNull Date date) {
        String wpTimeZone = site.getTimezone();

        /*
        Convert the timezone to a form that is compatible with Java TimeZone class
        WordPress returns something like the following:
           UTC+0:30 ----> 0.5
           UTC+1 ----> 1.0
           UTC-0:30 ----> -1.0
        */

        String timezoneNormalized;
        if (wpTimeZone == null || wpTimeZone.isEmpty() || wpTimeZone.equals("0") || wpTimeZone.equals("0.0")) {
            timezoneNormalized = "GMT";
        } else {
            String[] timezoneSplit = split(wpTimeZone, ".");
            timezoneNormalized = timezoneSplit[0];
            if (timezoneSplit.length > 1) {
                switch (timezoneSplit[1]) {
                    case "5":
                        timezoneNormalized += ":30";
                        break;
                    case "75":
                        timezoneNormalized += ":45";
                        break;
                    case "25":
                        // Not used by any timezones as of current writing, but you never know
                        timezoneNormalized += ":15";
                        break;
                }
            }
            if (timezoneNormalized.startsWith("-")) {
                timezoneNormalized = "GMT" + timezoneNormalized;
            } else {
                if (timezoneNormalized.startsWith("+")) {
                    timezoneNormalized = "GMT" + timezoneNormalized;
                } else {
                    timezoneNormalized = "GMT+" + timezoneNormalized;
                }
            }
        }

        dateFormat.setTimeZone(TimeZone.getTimeZone(timezoneNormalized));
        return dateFormat.format(date);
    }
}
