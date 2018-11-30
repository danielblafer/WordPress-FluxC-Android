package org.wordpress.android.fluxc.model;

import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit;
import org.wordpress.android.fluxc.store.WCStatsStore;
import org.wordpress.android.fluxc.utils.SiteUtils;
import org.wordpress.android.fluxc.utils.TimeEnum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//TODO: Unit Test
public class StatsCustomRange {
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.
    private Date mStartDate;
    private Date mEndDate;
    private OrderStatsApiUnit mGranularity;

    public StatsCustomRange(Date startDate, Date endDate,
                            OrderStatsApiUnit granularity) {
        mStartDate = startDate;
        mEndDate = endDate;
        mGranularity = granularity;
    }

    // Avoid news in Constructors. For debugging only
    public StatsCustomRange(boolean debug) throws ParseException {
        if (!debug) {
            mStartDate = null;
            mEndDate = null;
            mGranularity = null;
        } else {
            DateFormat formatter = new SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT);

            mStartDate = formatter.parse("2018-09-18");
            mEndDate = formatter.parse("2018-11-28");
            mGranularity = OrderStatsApiUnit.YEAR;
        }
    }

    public StatsCustomRange() {
        mStartDate = null;
        mEndDate = null;
        mGranularity = null;
    }

    public String getStartDate() {
        DateFormat formatter = new SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT);
        String fullDate = formatter.format(mStartDate);
        return clipDateBasedOnGranularity(fullDate);
    }

    public String getEndDate() {
        DateFormat formatter = new SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT);
        String fullDate = formatter.format(mEndDate);
        return clipDateBasedOnGranularity(fullDate);
    }

    public OrderStatsApiUnit getGranularity() {
        return mGranularity;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public void setGranularity(OrderStatsApiUnit granularity) {
        mGranularity = granularity;
    }

    public int getQuantityBetweenTwoDates() {
        checkForSwitchedDates();
        checkForOvershotDates();

        if (this.mGranularity == OrderStatsApiUnit.DAY) {
            return SiteUtils.calculateTimeDifferencesBetweenDates(this.mStartDate, this.mEndDate, TimeEnum.DAYS);
        } else if (this.mGranularity == OrderStatsApiUnit.WEEK) {
            return SiteUtils.calculateTimeDifferencesBetweenDates(this.mStartDate, this.mEndDate, TimeEnum.WEEKS);
        } else if (this.mGranularity == OrderStatsApiUnit.MONTH) {
            return SiteUtils.calculateTimeDifferencesBetweenDates(this.mStartDate, this.mEndDate, TimeEnum.MONTHS);
        } else if (this.mGranularity == OrderStatsApiUnit.YEAR) {
            return SiteUtils.calculateTimeDifferencesBetweenDates(this.mStartDate, this.mEndDate, TimeEnum.YEARS);
        } else return 0;
    }

    private void checkForOvershotDates() {
        if (mEndDate.getTime() > System.currentTimeMillis()) {
            this.mEndDate = new Date();
        }
    }

    private void checkForSwitchedDates() {
        if (mStartDate.getTime() > mEndDate.getTime()) {
            Date tempEndDate = this.mEndDate;
            this.mEndDate = this.mStartDate;
            this.mStartDate = tempEndDate;
        }
    }

    private String clipDateBasedOnGranularity(String fullDate) {
        if (this.mGranularity == OrderStatsApiUnit.YEAR) {
            return fullDate.substring(0, fullDate.length() - 6);
        } else if (this.mGranularity == OrderStatsApiUnit.MONTH) {
            return fullDate.substring(0, fullDate.length() - 3);
        } else if (this.mGranularity == OrderStatsApiUnit.WEEK) {
            String year = fullDate.substring(0, fullDate.length() - 6);
            int week = SiteUtils.getWeekNumberInCalendar(mStartDate);
            return year + "-W" + week;
        } else return fullDate;
    }
}
