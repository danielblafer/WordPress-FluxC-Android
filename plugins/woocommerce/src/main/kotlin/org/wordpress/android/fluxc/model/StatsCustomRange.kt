package org.wordpress.android.fluxc.model

import java.util.Date

/**
 * This class is devoided of any Static calls as to be fully 100% testable
 */
class StatsCustomRange(
    // In the future we need to use LocalDate. The MinApi Call is #26 though which may prove a bit steep. Still, it
    //  reduces boiler plate code so much.
    var startDate: Date = Date(),
    var endDate: Date = Date(),
    var customObject: Boolean = false
) {
    init {
        if (startDate.time > endDate.time) {
            val tempEndDate = this.endDate
            this.endDate = this.startDate
            this.startDate = tempEndDate
        }
    }
}
