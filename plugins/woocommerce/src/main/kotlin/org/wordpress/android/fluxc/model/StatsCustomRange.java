package org.wordpress.android.fluxc.model;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit;
import org.wordpress.android.fluxc.store.WCStatsStore;

//TODO: Unit Test
public class StatsCustomRange {
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.
    private DateTime mStartDate;
    private DateTime mEndDate;
    private OrderStatsApiUnit mGranularity;

    public StatsCustomRange(DateTime startDate, DateTime endDate,
                            OrderStatsApiUnit granularity) {
        mStartDate = startDate;
        mEndDate = endDate;
        mGranularity = granularity;
    }

    // Avoid news in Constructors. For debugging only
    public StatsCustomRange(boolean debug) {
        if (!debug) {
            mStartDate = null;
            mEndDate = null;
            mGranularity = null;
        } else {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(WCStatsStore.DATE_FORMAT_DAY);

            mStartDate = formatter.parseDateTime("2018-09-18");
            mEndDate = formatter.parseDateTime("2018-11-28");
            mGranularity = OrderStatsApiUnit.YEAR;
        }
    }

    public StatsCustomRange() {
        mStartDate = null;
        mEndDate = null;
        mGranularity = null;
    }

    public String getStartDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(WCStatsStore.DATE_FORMAT_DAY);
        String fullDate = formatter.print(mStartDate);
        return clipDateBasedOnGranularity(fullDate);
    }

    public String getEndDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(WCStatsStore.DATE_FORMAT_DAY);
        String fullDate = formatter.print(mEndDate);
        return clipDateBasedOnGranularity(fullDate);
    }

    public OrderStatsApiUnit getGranularity() {
        return mGranularity;
    }

    public void setStartDate(DateTime startDate) {
        mStartDate = startDate;
    }

    public void setEndDate(DateTime endDate) {
        mEndDate = endDate;
    }

    public void setGranularity(OrderStatsApiUnit granularity) {
        mGranularity = granularity;
    }

    public int getQuantityBetweenTwoDates() {
        checkForSwitchedDates();
        checkForOvershotDates();

        if (this.mGranularity == OrderStatsApiUnit.DAY) {
            return Days.daysBetween(this.mStartDate, this.mEndDate).getDays();
        } else if (this.mGranularity == OrderStatsApiUnit.MONTH) {
            return Months.monthsBetween(this.mStartDate, this.mEndDate).getMonths();
        } else if (this.mGranularity == OrderStatsApiUnit.YEAR) {
            return Years.yearsBetween(this.mStartDate, this.mEndDate).getYears();
        } else return 0;
    }

    private void checkForOvershotDates() {
        DateTime rightNow = new DateTime();
        if (mEndDate.isAfter(rightNow)) {
            this.mEndDate = rightNow;
        }
    }

    private void checkForSwitchedDates() {
        if (mStartDate.isAfter(mEndDate)) {
            DateTime tempEndDate = this.mEndDate;
            this.mEndDate = this.mStartDate;
            this.mStartDate = tempEndDate;
        }
    }

    private String clipDateBasedOnGranularity(String fullDate) {
        if (this.mGranularity == OrderStatsApiUnit.YEAR) {
            return fullDate.substring(0, fullDate.length() - 6);
        } else if (this.mGranularity == OrderStatsApiUnit.MONTH) {
            return fullDate.substring(0, fullDate.length() - 3);
        } else return fullDate;
    }
}
