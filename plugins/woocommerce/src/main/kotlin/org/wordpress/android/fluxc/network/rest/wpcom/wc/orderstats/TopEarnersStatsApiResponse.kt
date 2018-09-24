package org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats

import org.wordpress.android.fluxc.network.Response

class TopEarnersStatsApiResponse : Response {
    val data: List<TopEarner>? = null

    class TopEarner {
        val product_id: Int? = 0
        val items_sold: Int? = 0
        val gross_revenue: Double? = 0.0
        val orders_count: Int? = 0
        val price: Double? = 0.0
        val name: String? = ""
        val image: String? = ""
        val permalink: String? = ""
    }
}
