package org.wordpress.android.fluxc.wc.order

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderNoteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderNotesApiResponse
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderStatus

object OrderTestUtils {
    fun generateSampleOrder(remoteId: Long): WCOrderModel = generateSampleOrder(remoteId, OrderStatus.PROCESSING)

    fun generateSampleOrder(remoteId: Long, orderStatus: String): WCOrderModel {
        return WCOrderModel().apply {
            remoteOrderId = remoteId
            localSiteId = 6
            status = orderStatus
            dateCreated = "1955-11-05T14:15:00Z"
            currency = "USD"
            total = "10.0"
        }
    }

    fun getOrderNotesFromJsonString(json: String, siteId: Int, orderId: Int): List<WCOrderNoteModel> {
        val responseType = object : TypeToken<List<OrderNotesApiResponse>>() {}.type
        val converted = Gson().fromJson(json, responseType) as? List<OrderNotesApiResponse> ?: emptyList()
        val result = mutableListOf<WCOrderNoteModel>()
        converted.forEach { t ->
            result.add(WCOrderNoteModel().apply {
                remoteNoteId = t.id ?: 0
                dateCreated = "${t.date_created_gmt}Z"
                note = t.note ?: ""
                customerNote = t.customer_note
                localSiteId = siteId
                localOrderId = orderId
            })
        }
        return result
    }

    fun generateSampleNote(remoteId: Long, siteId: Int, orderId: Int): WCOrderNoteModel {
        return WCOrderNoteModel().apply {
            localSiteId = siteId
            localOrderId = orderId
            remoteNoteId = remoteId
            dateCreated = "1955-11-05T14:15:00Z"
            note = "This is a test note"
            customerNote = true
        }
    }
}
