package org.wordpress.android.fluxc.wc.stats

import org.wordpress.android.fluxc.UnitTestUtils
import org.wordpress.android.fluxc.model.WCOrderStatsModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit
import org.wordpress.android.fluxc.store.WCStatsStore

object WCStatsTestUtils {
    fun generateSampleStatsModel(
        localSiteId: Int = 6,
        unit: String = OrderStatsApiUnit.DAY.toString(),
        custom: Int = WCStatsStore.IS_NOT_CUSTOM,
        fields: String = UnitTestUtils.getStringFromResourceFile(this.javaClass, "wc/order-stats-fields.json"),
        data: String = UnitTestUtils.getStringFromResourceFile(this.javaClass, "wc/order-stats-data.json")
    ): WCOrderStatsModel {
        return WCOrderStatsModel().apply {
            this.localSiteId = localSiteId
            this.unit = unit
            this.fields = fields
            this.data = data
        }
    }
}
