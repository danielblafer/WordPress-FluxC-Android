package org.wordpress.android.fluxc.store.stats.time

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers.Unconfined
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.stats.time.PostAndPageViewsModel
import org.wordpress.android.fluxc.model.stats.time.TimeStatsMapper
import org.wordpress.android.fluxc.network.rest.wpcom.stats.time.PostAndPageViewsRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.stats.time.PostAndPageViewsRestClient.PostAndPageViewsResponse
import org.wordpress.android.fluxc.network.utils.StatsGranularity.DAYS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.MONTHS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.WEEKS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.YEARS
import org.wordpress.android.fluxc.persistence.TimeStatsSqlUtils
import org.wordpress.android.fluxc.store.StatsStore.FetchStatsPayload
import org.wordpress.android.fluxc.store.StatsStore.StatsError
import org.wordpress.android.fluxc.store.StatsStore.StatsErrorType.API_ERROR
import org.wordpress.android.fluxc.test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private const val PAGE_SIZE = 8
private val DATE = Date(0)

@RunWith(MockitoJUnitRunner::class)
class PostAndPageViewsStoreTest {
    @Mock lateinit var site: SiteModel
    @Mock lateinit var restClient: PostAndPageViewsRestClient
    @Mock lateinit var sqlUtils: TimeStatsSqlUtils
    @Mock lateinit var mapper: TimeStatsMapper
    private lateinit var store: PostAndPageViewsStore
    @Before
    fun setUp() {
        store = PostAndPageViewsStore(
                restClient,
                sqlUtils,
                mapper,
                Unconfined
        )
    }

    @Test
    fun `returns post and page day views per site`() = test {
        val fetchInsightsPayload = FetchStatsPayload(
                DAY_POST_AND_PAGE_VIEWS_RESPONSE
        )
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, DAYS, DATE, PAGE_SIZE + 1, forced)).thenReturn(
                fetchInsightsPayload
        )
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(DAY_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, DAYS, DATE, forced)

        assertThat(responseModel.model).isEqualTo(model)
        verify(sqlUtils).insert(site, DAY_POST_AND_PAGE_VIEWS_RESPONSE, DAYS, DATE)
    }

    @Test
    fun `returns error when post and page day views call fail`() = test {
        val type = API_ERROR
        val message = "message"
        val errorPayload = FetchStatsPayload<PostAndPageViewsResponse>(StatsError(type, message))
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, DAYS, DATE, PAGE_SIZE + 1, forced)).thenReturn(errorPayload)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, DAYS, DATE, forced)

        assertNotNull(responseModel.error)
        val error = responseModel.error!!
        assertEquals(type, error.type)
        assertEquals(message, error.message)
    }

    @Test
    fun `returns post and page day views from db`() {
        whenever(sqlUtils.selectPostAndPageViews(site, DAYS, DATE)).thenReturn(DAY_POST_AND_PAGE_VIEWS_RESPONSE)
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(DAY_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val result = store.getPostAndPageViews(site, DAYS, DATE, PAGE_SIZE)

        assertThat(result).isEqualTo(model)
    }

    @Test
    fun `returns post and page week views per site`() = test {
        val fetchInsightsPayload = FetchStatsPayload(
                WEEK_POST_AND_PAGE_VIEWS_RESPONSE
        )
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, WEEKS, DATE, PAGE_SIZE + 1, forced)).thenReturn(
                fetchInsightsPayload
        )
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(WEEK_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, WEEKS, DATE, forced)

        assertThat(responseModel.model).isEqualTo(model)
        verify(sqlUtils).insert(site, WEEK_POST_AND_PAGE_VIEWS_RESPONSE, WEEKS, DATE)
    }

    @Test
    fun `returns error when post and page week views call fail`() = test {
        val type = API_ERROR
        val message = "message"
        val errorPayload = FetchStatsPayload<PostAndPageViewsResponse>(StatsError(type, message))
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, WEEKS, DATE, PAGE_SIZE + 1, forced)).thenReturn(errorPayload)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, WEEKS, DATE, forced)

        assertNotNull(responseModel.error)
        val error = responseModel.error!!
        assertEquals(type, error.type)
        assertEquals(message, error.message)
    }

    @Test
    fun `returns post and page week views from db`() {
        whenever(sqlUtils.selectPostAndPageViews(site, WEEKS, DATE)).thenReturn(WEEK_POST_AND_PAGE_VIEWS_RESPONSE)
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(WEEK_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val result = store.getPostAndPageViews(site, WEEKS, DATE, PAGE_SIZE)

        assertThat(result).isEqualTo(model)
    }

    @Test
    fun `returns post and page month views per site`() = test {
        val fetchInsightsPayload = FetchStatsPayload(
                MONTH_POST_AND_PAGE_VIEWS_RESPONSE
        )
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, MONTHS, DATE, PAGE_SIZE + 1, forced)).thenReturn(
                fetchInsightsPayload
        )
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(MONTH_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, MONTHS, DATE, forced)

        assertThat(responseModel.model).isEqualTo(model)
        verify(sqlUtils).insert(site, MONTH_POST_AND_PAGE_VIEWS_RESPONSE, MONTHS, DATE)
    }

    @Test
    fun `returns error when post and page month views call fail`() = test {
        val type = API_ERROR
        val message = "message"
        val errorPayload = FetchStatsPayload<PostAndPageViewsResponse>(StatsError(type, message))
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, MONTHS, DATE, PAGE_SIZE + 1, forced)).thenReturn(errorPayload)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, MONTHS, DATE, forced)

        assertNotNull(responseModel.error)
        val error = responseModel.error!!
        assertEquals(type, error.type)
        assertEquals(message, error.message)
    }

    @Test
    fun `returns post and page month views from db`() {
        whenever(sqlUtils.selectPostAndPageViews(site, MONTHS, DATE)).thenReturn(MONTH_POST_AND_PAGE_VIEWS_RESPONSE)
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(MONTH_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val result = store.getPostAndPageViews(site, MONTHS, DATE, PAGE_SIZE)

        assertThat(result).isEqualTo(model)
    }

    @Test
    fun `returns post and page year views per site`() = test {
        val fetchInsightsPayload = FetchStatsPayload(
                YEAR_POST_AND_PAGE_VIEWS_RESPONSE
        )
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, YEARS, DATE, PAGE_SIZE + 1, forced)).thenReturn(
                fetchInsightsPayload
        )
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(YEAR_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, YEARS, DATE, forced)

        assertThat(responseModel.model).isEqualTo(model)
        verify(sqlUtils).insert(site, YEAR_POST_AND_PAGE_VIEWS_RESPONSE, YEARS, DATE)
    }

    @Test
    fun `returns error when post and page year views call fail`() = test {
        val type = API_ERROR
        val message = "message"
        val errorPayload = FetchStatsPayload<PostAndPageViewsResponse>(StatsError(type, message))
        val forced = true
        whenever(restClient.fetchPostAndPageViews(site, YEARS, DATE, PAGE_SIZE + 1, forced)).thenReturn(errorPayload)

        val responseModel = store.fetchPostAndPageViews(site, PAGE_SIZE, YEARS, DATE, forced)

        assertNotNull(responseModel.error)
        val error = responseModel.error!!
        assertEquals(type, error.type)
        assertEquals(message, error.message)
    }

    @Test
    fun `returns post and page year views from db`() {
        whenever(sqlUtils.selectPostAndPageViews(site, YEARS, DATE)).thenReturn(YEAR_POST_AND_PAGE_VIEWS_RESPONSE)
        val model = mock<PostAndPageViewsModel>()
        whenever(mapper.map(YEAR_POST_AND_PAGE_VIEWS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val result = store.getPostAndPageViews(site, YEARS, DATE, PAGE_SIZE)

        assertThat(result).isEqualTo(model)
    }
}
