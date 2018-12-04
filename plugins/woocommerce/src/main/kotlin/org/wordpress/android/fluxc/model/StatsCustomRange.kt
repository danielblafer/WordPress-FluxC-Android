package org.wordpress.android.fluxc.model

import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.utils.TimeEnum
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This class is devoided of any Static calls as to be fully 100% testable
 */
class StatsCustomRange(
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.
    var startDate: Date = Date(),
    var endDate: Date = Date(),
    var granularity: OrderStatsApiUnit = OrderStatsApiUnit.CUSTOM
) {
    fun getStartDateAsStringForGranularity(weekOfTheYear: Int): String {
        val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)
        val fullDate = formatter.format(startDate)
        return clipDateBasedOnGranularity(fullDate, weekOfTheYear)
    }

    fun getEndDateAsStringForGranularity(weekOfTheYear: Int): String {
        val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)
        val fullDate = formatter.format(endDate)
        return clipDateBasedOnGranularity(fullDate, weekOfTheYear)
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

    fun checkForSwitchedDates() {
        if (startDate.time > endDate.time) {
            val tempEndDate = this.endDate
            this.endDate = this.startDate
            this.startDate = tempEndDate
        }
    }

    fun clipDateBasedOnGranularity(fullDate: String, week: Int): String {
        return when {
            this.granularity === OrderStatsApiUnit.YEAR -> fullDate.substring(0, fullDate.length - 6)
            this.granularity === OrderStatsApiUnit.MONTH -> fullDate.substring(0, fullDate.length - 3)
            this.granularity === OrderStatsApiUnit.WEEK -> {

                var weekString = week.toString()

                if(week < 10) {
                    weekString = "0$week"
                }

                val year = fullDate.substring(0, fullDate.length - 6)
                "$year-W$weekString"
            }
            else -> fullDate
        }
    }
}
