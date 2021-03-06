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
import org.wordpress.android.fluxc.model.stats.time.AuthorsModel
import org.wordpress.android.fluxc.model.stats.time.TimeStatsMapper
import org.wordpress.android.fluxc.network.rest.wpcom.stats.time.AuthorsRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.stats.time.AuthorsRestClient.AuthorsResponse
import org.wordpress.android.fluxc.network.utils.StatsGranularity.DAYS
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
class AuthorsStoreTest {
    @Mock lateinit var site: SiteModel
    @Mock lateinit var restClient: AuthorsRestClient
    @Mock lateinit var sqlUtils: TimeStatsSqlUtils
    @Mock lateinit var mapper: TimeStatsMapper
    private lateinit var store: AuthorsStore
    @Before
    fun setUp() {
        store = AuthorsStore(
                restClient,
                sqlUtils,
                mapper,
                Unconfined
        )
    }

    @Test
    fun `returns data per site`() = test {
        val fetchInsightsPayload = FetchStatsPayload(
                AUTHORS_RESPONSE
        )
        val forced = true
        whenever(restClient.fetchAuthors(site, DAYS, DATE, PAGE_SIZE + 1, forced)).thenReturn(
                fetchInsightsPayload
        )
        val model = mock<AuthorsModel>()
        whenever(mapper.map(AUTHORS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val responseModel = store.fetchAuthors(site, PAGE_SIZE, DAYS, DATE, forced)

        assertThat(responseModel.model).isEqualTo(model)
        verify(sqlUtils).insert(site, AUTHORS_RESPONSE, DAYS, DATE)
    }

    @Test
    fun `returns error when data call fail`() = test {
        val type = API_ERROR
        val message = "message"
        val errorPayload = FetchStatsPayload<AuthorsResponse>(StatsError(type, message))
        val forced = true
        whenever(restClient.fetchAuthors(site, DAYS, DATE, PAGE_SIZE + 1, forced)).thenReturn(errorPayload)

        val responseModel = store.fetchAuthors(site, PAGE_SIZE, DAYS, DATE, forced)

        assertNotNull(responseModel.error)
        val error = responseModel.error!!
        assertEquals(type, error.type)
        assertEquals(message, error.message)
    }

    @Test
    fun `returns data from db`() {
        whenever(sqlUtils.selectAuthors(site, DAYS, DATE)).thenReturn(AUTHORS_RESPONSE)
        val model = mock<AuthorsModel>()
        whenever(mapper.map(AUTHORS_RESPONSE, PAGE_SIZE)).thenReturn(model)

        val result = store.getAuthors(site, DAYS, PAGE_SIZE, DATE)

        assertThat(result).isEqualTo(model)
    }
}
