package org.wordpress.android.fluxc.network.rest.wpcom.wc

import android.content.Context
import com.android.volley.RequestQueue
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.WCCoreAction
import org.wordpress.android.fluxc.generated.WCCoreActionBuilder
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.discovery.RootWPAPIRestResponse
import org.wordpress.android.fluxc.network.rest.wpcom.BaseWPComRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequest
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequest.WPComGsonNetworkError
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AccessToken
import org.wordpress.android.fluxc.network.rest.wpcom.jetpacktunnel.JetpackTunnelGsonRequest
import org.wordpress.android.fluxc.store.WooCommerceStore
import org.wordpress.android.fluxc.store.WooCommerceStore.ApiVersionError
import org.wordpress.android.fluxc.store.WooCommerceStore.ApiVersionErrorType
import org.wordpress.android.fluxc.store.WooCommerceStore.FetchApiVersionResponsePayload
import javax.inject.Singleton

@Singleton
class WooCommerceRestClient(
    appContext: Context,
    private val dispatcher: Dispatcher,
    requestQueue: RequestQueue,
    accessToken: AccessToken,
    userAgent: UserAgent
) : BaseWPComRestClient(appContext, dispatcher, requestQueue, accessToken, userAgent) {
    /**
     * Makes a GET call to the root wp-json endpoint (`/`) via the Jetpack tunnel (see [JetpackTunnelGsonRequest])
     * for the given [SiteModel], and parses through the `namespaces` field in the result for supported versions
     * of the Woo API.
     *
     * Dispatches a [WCCoreAction.FETCHED_SITE_API_VERSION] action with the highest version of the Woo API supported
     * by the site (but no newer than the latest supported by FluxC).
     */
    fun getSupportedWooApiVersion(site: SiteModel) {
        val url = "/"
        val request = JetpackTunnelGsonRequest.buildGetRequest(url, site.siteId, emptyMap(),
                RootWPAPIRestResponse::class.java,
                { response: RootWPAPIRestResponse? ->
                    val namespaces = response?.namespaces

                    val maxWooApiVersion = namespaces?.run {
                        find { it == WooCommerceStore.WOO_API_NAMESPACE_V3 }
                                ?: find { it == WooCommerceStore.WOO_API_NAMESPACE_V2 }
                                ?: find { it == WooCommerceStore.WOO_API_NAMESPACE_V1 }
                    }

                    maxWooApiVersion?.let { maxApiVersion ->
                        val payload = FetchApiVersionResponsePayload(site, maxApiVersion)
                        dispatcher.dispatch(WCCoreActionBuilder.newFetchedSiteApiVersionAction(payload))
                    } ?: run {
                        val apiVersionError = ApiVersionError(ApiVersionErrorType.NO_WOO_API)
                        val payload = FetchApiVersionResponsePayload(apiVersionError, site)
                        dispatcher.dispatch(WCCoreActionBuilder.newFetchedSiteApiVersionAction(payload))
                    }
                },
                WPComGsonRequest.WPComErrorListener { networkError ->
                    val payload = FetchApiVersionResponsePayload(networkErrorToApiVersionError(networkError), site)
                    dispatcher.dispatch(WCCoreActionBuilder.newFetchedSiteApiVersionAction(payload))
                },
                { request: WPComGsonRequest<*> -> add(request) })
        add(request)
    }

    private fun networkErrorToApiVersionError(wpComError: WPComGsonNetworkError): ApiVersionError {
        val apiVersionErrorType = ApiVersionErrorType.fromString(wpComError.apiError)
        return ApiVersionError(apiVersionErrorType, wpComError.message)
    }
}
