package org.wordpress.android.fluxc.utils;

import java.util.Date;

public class DateUtils {
    private static final long MILLISECONDS = 1000;
    private static final long ONE_DAY_IN_SECONDS = 86400;
    private static final long DAYS_IN_A_WEEK = 7;
    // According to google, the average number of days in a month is 30.42
    private static final double DAYS_IN_A_MONTH = 30.42;
    private static final double DAYS_IN_A_YEAR = 365;

    private static final String DAYS = "DAYS";
    private static final String WEEKS = "WEEKS";
    private static final String MONTHS = "MONTHS";
    private static final String YEARS = "YEARS";

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
     * @param time The actual value of type we want (Days, Weeks, Months, Years)
     * @return an integer value of the type we want
     */
    public static int calculateTimeDifferencesBetweenDates(Date startDate, Date endDate, String time) {
        long differenceInTimeMS = endDate.getTime() - startDate.getTime();

        switch (time) {
            case DAYS:
                // The +1 accounts for inclusive days
                return ((int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS))) + 1;
            case WEEKS:
                // The +1 accounts for inclusive weeks
                return ((int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_WEEK))) + 1;
            case MONTHS:
                // Ceil (rounding up) accounts for inclusive months
                return ((int) Math.ceil((differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_MONTH))));
            case YEARS:
                // The +1 accounts for inclusive years
                return ((int) (differenceInTimeMS / (MILLISECONDS * ONE_DAY_IN_SECONDS * DAYS_IN_A_YEAR))) + 1;
            default:
                return 0;
        }
    }
}
