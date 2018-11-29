package org.wordpress.android.fluxc.store;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.wordpress.android.fluxc.network.utils.StatsGranularity;

//TODO: Unit Test
public class StatsCustomRange {
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.
    private DateTime mStartDate;
    private DateTime mEndDate;
    private Enum<StatsGranularity> mGranularity;

    public StatsCustomRange(DateTime startDate, DateTime endDate,
                            Enum<StatsGranularity> granularity) {
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
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

            mStartDate = formatter.parseDateTime("2018-09-18");
            mEndDate = formatter.parseDateTime("2018-11-28");
            mGranularity = StatsGranularity.DAYS;
        }
    }

    public StatsCustomRange() {
        mStartDate = null;
        mEndDate = null;
        mGranularity = null;
    }

    public DateTime getStartDate() {
        return mStartDate;
    }

    public DateTime getEndDate() {
        return mEndDate;
    }

    public Enum<StatsGranularity> getGranularity() {
        return mGranularity;
    }

    public void setStartDate(DateTime startDate) {
        mStartDate = startDate;
    }

    public void setEndDate(DateTime endDate) {
        mEndDate = endDate;
    }

    public void setGranularity(Enum<StatsGranularity> granularity) {
        mGranularity = granularity;
    }

    public int getQuantityBetweenTwoDates() {
        checkForSwitchedDates();
        checkForOvershotDates();

        if (this.mGranularity == StatsGranularity.DAYS) {
            return Days.daysBetween(this.mEndDate, this.mStartDate).getDays();
        } else if (this.mGranularity == StatsGranularity.MONTHS) {
            return Months.monthsBetween(this.mEndDate, this.mStartDate).getMonths();
        } else if (this.mGranularity == StatsGranularity.YEARS) {
            return Years.yearsBetween(this.mEndDate, this.mStartDate).getYears();
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
}
