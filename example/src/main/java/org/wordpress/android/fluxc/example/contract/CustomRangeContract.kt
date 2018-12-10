package org.wordpress.android.fluxc.example.contract

import org.wordpress.android.fluxc.model.StatsCustomRange
import org.wordpress.android.fluxc.store.WCStatsStore.StatsGranularity

interface CustomRangeContract {
    fun userDefinedCustomRange(statsCustomRange: StatsCustomRange, statsGranularity: StatsGranularity)
}
