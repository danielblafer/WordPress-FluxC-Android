package org.wordpress.android.fluxc.store

import kotlinx.coroutines.experimental.withContext
import org.wordpress.android.fluxc.Payload
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.stats.InsightsAllTimeModel
import org.wordpress.android.fluxc.model.stats.InsightsLatestPostModel
import org.wordpress.android.fluxc.model.stats.InsightsMostPopularModel
import org.wordpress.android.fluxc.network.rest.wpcom.stats.InsightsRestClient
import org.wordpress.android.fluxc.store.InsightsStore.StatsErrorType.INVALID_RESPONSE
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Singleton
class InsightsStore
@Inject constructor(
    private val insightsRestClient: InsightsRestClient,
    private val coroutineContext: CoroutineContext
) {
    suspend fun fetchAllTimeInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val payload = insightsRestClient.fetchAllTimeInsights(site, forced)
        return@withContext when {
            payload.isError -> OnInsightsFetched(payload.error)
            payload.response != null -> {
                val data = payload.response
                val stats = data.stats
                OnInsightsFetched(
                        InsightsAllTimeModel(
                                site.siteId,
                                data.date,
                                stats.visitors,
                                stats.views,
                                stats.posts,
                                stats.viewsBestDay,
                                stats.viewsBestDayTotal
                        )
                )
            }
            else -> OnInsightsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    suspend fun fetchMostPopularInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val payload = insightsRestClient.fetchMostPopularInsights(site, forced)
        return@withContext when {
            payload.isError -> OnInsightsFetched(payload.error)
            payload.response != null -> {
                val data = payload.response
                OnInsightsFetched(
                        InsightsMostPopularModel(
                                data.highestDayOfWeek,
                                data.highestHour,
                                data.highestDayPercent,
                                data.highestHourPercent
                        )
                )
            }
            else -> OnInsightsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    suspend fun fetchLatestPostInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val responsePost = insightsRestClient.fetchLatestPostForInsights(site, forced)
        val postsFound = responsePost.response?.postsFound

        val posts = responsePost.response?.posts
        return@withContext if (postsFound != null && postsFound > 0 && posts != null && posts.isNotEmpty()) {
            val latestPost = posts[0]
            val postViews = insightsRestClient.fetchPostViewsForInsights(site, latestPost.id, forced)
            val commentCount = latestPost.discussion?.commentCount ?: 0
            val viewsCount = postViews.response?.views ?: 0
            OnInsightsFetched(
                    InsightsLatestPostModel(
                            site.siteId,
                            latestPost.title,
                            latestPost.url,
                            latestPost.date,
                            latestPost.id,
                            viewsCount,
                            commentCount,
                            latestPost.likeCount
                    )
            )
        } else if (responsePost.isError) {
            OnInsightsFetched(responsePost.error)
        } else {
            OnInsightsFetched()
        }
    }

    data class OnInsightsFetched<T>(val model: T? = null) : Store.OnChanged<StatsError>() {
        constructor(error: StatsError) : this() {
            this.error = error
        }
    }

    data class FetchInsightsPayload<T>(
        val response: T? = null
    ) : Payload<StatsError>() {
        constructor(error: StatsError) : this() {
            this.error = error
        }
    }

    enum class StatsErrorType {
        GENERIC_ERROR,
        TIMEOUT,
        API_ERROR,
        AUTHORIZATION_REQUIRED,
        INVALID_RESPONSE
    }

    class StatsError(var type: StatsErrorType, var message: String? = null) : Store.OnChangedError
}