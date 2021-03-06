package org.wordpress.android.fluxc.store

import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.stats.CommentsModel
import org.wordpress.android.fluxc.model.stats.FollowersModel
import org.wordpress.android.fluxc.model.stats.InsightsAllTimeModel
import org.wordpress.android.fluxc.model.stats.InsightsLatestPostModel
import org.wordpress.android.fluxc.model.stats.InsightsMapper
import org.wordpress.android.fluxc.model.stats.InsightsMostPopularModel
import org.wordpress.android.fluxc.model.stats.PublicizeModel
import org.wordpress.android.fluxc.model.stats.TagsModel
import org.wordpress.android.fluxc.model.stats.VisitsModel
import org.wordpress.android.fluxc.network.rest.wpcom.stats.InsightsRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.stats.InsightsRestClient.FollowerType
import org.wordpress.android.fluxc.network.rest.wpcom.stats.InsightsRestClient.FollowerType.EMAIL
import org.wordpress.android.fluxc.network.rest.wpcom.stats.InsightsRestClient.FollowerType.WP_COM
import org.wordpress.android.fluxc.network.utils.StatsGranularity.DAYS
import org.wordpress.android.fluxc.persistence.InsightsSqlUtils
import org.wordpress.android.fluxc.store.StatsStore.OnStatsFetched
import org.wordpress.android.fluxc.store.StatsStore.StatsError
import org.wordpress.android.fluxc.store.StatsStore.StatsErrorType.INVALID_RESPONSE
import org.wordpress.android.fluxc.utils.CurrentTimeProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class InsightsStore
@Inject constructor(
    private val restClient: InsightsRestClient,
    private val sqlUtils: InsightsSqlUtils,
    private val insightsMapper: InsightsMapper,
    private val timeProvider: CurrentTimeProvider,
    private val coroutineContext: CoroutineContext
) {
    // All time insights
    suspend fun fetchAllTimeInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val payload = restClient.fetchAllTimeInsights(site, forced)
        return@withContext when {
            payload.isError -> OnStatsFetched(payload.error)
            payload.response != null -> {
                sqlUtils.insert(site, payload.response)
                OnStatsFetched(insightsMapper.map(payload.response, site))
            }
            else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    fun getAllTimeInsights(site: SiteModel): InsightsAllTimeModel? {
        return sqlUtils.selectAllTimeInsights(site)?.let { insightsMapper.map(it, site) }
    }

    // Most popular insights
    suspend fun fetchMostPopularInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val payload = restClient.fetchMostPopularInsights(site, forced)
        return@withContext when {
            payload.isError -> OnStatsFetched(payload.error)
            payload.response != null -> {
                val data = payload.response
                sqlUtils.insert(site, data)
                OnStatsFetched(
                        insightsMapper.map(data, site)
                )
            }
            else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    fun getMostPopularInsights(site: SiteModel): InsightsMostPopularModel? {
        return sqlUtils.selectMostPopularInsights(site)?.let { insightsMapper.map(it, site) }
    }

    // Latest post insights
    suspend fun fetchLatestPostInsights(site: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val latestPost = restClient.fetchLatestPostForInsights(site, forced)
        val postsFound = latestPost.response?.postsFound

        val posts = latestPost.response?.posts
        return@withContext if (postsFound != null && postsFound > 0 && posts != null && posts.isNotEmpty()) {
            val latestPost = posts[0]
            val postStats = restClient.fetchPostStats(site, latestPost.id, forced)
            when {
                postStats.response != null -> {
                    sqlUtils.insert(site, latestPost)
                    sqlUtils.insert(site, postStats.response)
                    OnStatsFetched(insightsMapper.map(latestPost, postStats.response, site))
                }
                postStats.isError -> OnStatsFetched(postStats.error)
                else -> OnStatsFetched()
            }
        } else if (latestPost.isError) {
            OnStatsFetched(latestPost.error)
        } else {
            OnStatsFetched()
        }
    }

    fun getLatestPostInsights(site: SiteModel): InsightsLatestPostModel? {
        val latestPostDetailResponse = sqlUtils.selectLatestPostDetail(site)
        val latestPostViewsResponse = sqlUtils.selectLatestPostStats(site)
        return if (latestPostDetailResponse != null && latestPostViewsResponse != null) {
            insightsMapper.map(latestPostDetailResponse, latestPostViewsResponse, site)
        } else {
            null
        }
    }

    // Time period stats
    suspend fun fetchTodayInsights(siteModel: SiteModel, forced: Boolean = false) = withContext(coroutineContext) {
        val response = restClient.fetchTimePeriodStats(siteModel, DAYS, timeProvider.currentDate, forced)
        return@withContext when {
            response.isError -> {
                OnStatsFetched(response.error)
            }
            response.response != null -> {
                sqlUtils.insert(siteModel, response.response)
                OnStatsFetched(insightsMapper.map(response.response))
            }
            else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    fun getTodayInsights(site: SiteModel): VisitsModel? {
        return sqlUtils.selectTodayInsights(site)?.let { insightsMapper.map(it) }
    }

    // Followers stats
    suspend fun fetchWpComFollowers(
        siteModel: SiteModel,
        pageSize: Int,
        forced: Boolean = false
    ): OnStatsFetched<FollowersModel> {
        return fetchFollowers(siteModel, pageSize, forced, WP_COM)
    }

    suspend fun fetchEmailFollowers(
        siteModel: SiteModel,
        pageSize: Int,
        forced: Boolean = false
    ): OnStatsFetched<FollowersModel> {
        return fetchFollowers(siteModel, pageSize, forced, EMAIL)
    }

    private suspend fun fetchFollowers(
        siteModel: SiteModel,
        pageSize: Int,
        forced: Boolean = false,
        followerType: FollowerType
    ) = withContext(coroutineContext) {
        val response = restClient.fetchFollowers(siteModel, followerType, pageSize = pageSize + 1, forced = forced)
        return@withContext when {
            response.isError -> {
                OnStatsFetched(response.error)
            }
            response.response != null -> {
                sqlUtils.insert(siteModel, response.response, followerType)
                OnStatsFetched(insightsMapper.map(response.response, followerType, pageSize))
            }
            else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
        }
    }

    fun getWpComFollowers(site: SiteModel, pageSize: Int): FollowersModel? {
        return getFollowers(site, WP_COM, pageSize)
    }

    fun getEmailFollowers(site: SiteModel, pageSize: Int): FollowersModel? {
        return getFollowers(site, EMAIL, pageSize)
    }

    private fun getFollowers(
        site: SiteModel,
        followerType: FollowerType,
        pageSize: Int
    ): FollowersModel? {
        val wpComResponse = sqlUtils.selectFollowers(site, followerType)
        return wpComResponse?.let { insightsMapper.map(wpComResponse, followerType, pageSize) }
    }

    // Comments stats
    suspend fun fetchComments(siteModel: SiteModel, pageSize: Int, forced: Boolean = false) =
            withContext(coroutineContext) {
                val response = restClient.fetchTopComments(siteModel, pageSize = pageSize + 1, forced = forced)
                return@withContext when {
                    response.isError -> {
                        OnStatsFetched(response.error)
                    }
                    response.response != null -> {
                        sqlUtils.insert(siteModel, response.response)
                        OnStatsFetched(insightsMapper.map(response.response, pageSize))
                    }
                    else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
                }
            }

    fun getComments(site: SiteModel, pageSize: Int): CommentsModel? {
        return sqlUtils.selectCommentInsights(site)?.let { insightsMapper.map(it, pageSize) }
    }

    // Tags
    suspend fun fetchTags(siteModel: SiteModel, pageSize: Int, forced: Boolean = false) =
            withContext(coroutineContext) {
                val response = restClient.fetchTags(siteModel, pageSize = pageSize + 1, forced = forced)
                return@withContext when {
                    response.isError -> {
                        OnStatsFetched(response.error)
                    }
                    response.response != null -> {
                        sqlUtils.insert(siteModel, response.response)
                        OnStatsFetched(
                                insightsMapper.map(response.response, pageSize)
                        )
                    }
                    else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
                }
            }

    fun getTags(site: SiteModel, pageSize: Int): TagsModel? {
        return sqlUtils.selectTags(site)?.let { insightsMapper.map(it, pageSize) }
    }

    // Publicize stats
    suspend fun fetchPublicizeData(siteModel: SiteModel, pageSize: Int, forced: Boolean = false) =
            withContext(coroutineContext) {
                val response = restClient.fetchPublicizeData(siteModel, pageSize = pageSize + 1, forced = forced)
                return@withContext when {
                    response.isError -> {
                        OnStatsFetched(response.error)
                    }
                    response.response != null -> {
                        sqlUtils.insert(siteModel, response.response)
                        OnStatsFetched(insightsMapper.map(response.response, pageSize))
                    }
                    else -> OnStatsFetched(StatsError(INVALID_RESPONSE))
                }
            }

    fun getPublicizeData(site: SiteModel, pageSize: Int): PublicizeModel? {
        return sqlUtils.selectPublicizeInsights(site)?.let { insightsMapper.map(it, pageSize) }
    }
}
