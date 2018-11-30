package org.wordpress.android.fluxc.model

import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.utils.SiteUtils
import org.wordpress.android.fluxc.utils.TimeEnum
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This class is devoided of any Static calls as to be fully 100% testable
 */
class StatsCustomRange {
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.

    private var mStartDate: Date = Date()
    private var mEndDate: Date = Date()
    var granularity: OrderStatsApiUnit = OrderStatsApiUnit.DEFAULT

    constructor(
        startDate: Date, endDate: Date,
        granularity: OrderStatsApiUnit
    ) {
        mStartDate = startDate
        mEndDate = endDate
        this.granularity = granularity
    }

    // Avoid news in Constructors. For debugging only
    @Throws(ParseException::class)
    constructor(debug: Boolean) {
        if (debug) {
            val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)

            mStartDate = formatter.parse("2018-09-18")
            mEndDate = formatter.parse("2018-11-28")
            granularity = OrderStatsApiUnit.YEAR
        }
    }

    constructor() {
        mStartDate = Date()
        mEndDate = Date()
        granularity = OrderStatsApiUnit.DEFAULT
    }

    val getStartDateInDateFormat: Date
        get(){
            return this.mStartDate
        }

    val getEndDateInDateFormat: Date
        get(){
            return this.mEndDate
        }

    fun getStartDate(weekOfTheYear: Int): String {
        val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)
        val fullDate = formatter.format(mStartDate)
        return clipDateBasedOnGranularity(fullDate, weekOfTheYear)
    }

    fun getEndDate(weekOfTheYear: Int): String {
        val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)
        val fullDate = formatter.format(mEndDate)
        return clipDateBasedOnGranularity(fullDate, weekOfTheYear)
    }


    fun checkForWrongValues() {
        checkForSwitchedDates()
        checkForOvershotDates()
    }

    val getTimeEnum: TimeEnum
        get() {
            return when {
                this.granularity === OrderStatsApiUnit.DAY -> TimeEnum.DAYS
                this.granularity === OrderStatsApiUnit.WEEK -> TimeEnum.WEEKS
                this.granularity === OrderStatsApiUnit.MONTH -> TimeEnum.MONTHS
                this.granularity === OrderStatsApiUnit.YEAR -> TimeEnum.YEARS
                else -> TimeEnum.DAYS // Default
            }
        }

    fun setStartDate(startDate: Date) {
        mStartDate = startDate
    }

    fun setEndDate(endDate: Date) {
        mEndDate = endDate
    }

    fun checkForOvershotDates() {
        if (mEndDate.time > System.currentTimeMillis()) {
            this.mEndDate = Date()
        }
    }

    fun checkForSwitchedDates() {
        if (mStartDate.time > mEndDate.time) {
            val tempEndDate = this.mEndDate
            this.mEndDate = this.mStartDate
            this.mStartDate = tempEndDate
        }
    }

    fun clipDateBasedOnGranularity(fullDate: String, week: Int): String {
        return when {
            this.granularity === OrderStatsApiUnit.YEAR -> fullDate.substring(0, fullDate.length - 6)
            this.granularity === OrderStatsApiUnit.MONTH -> fullDate.substring(0, fullDate.length - 3)
            this.granularity === OrderStatsApiUnit.WEEK -> {
                val year = fullDate.substring(0, fullDate.length - 6)
                "$year-W$week"
            }
            else -> fullDate
        }
    }
}
