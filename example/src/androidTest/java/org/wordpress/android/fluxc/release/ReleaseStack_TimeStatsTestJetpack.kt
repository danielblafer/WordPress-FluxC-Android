package org.wordpress.android.fluxc.release

import kotlinx.coroutines.experimental.runBlocking
import org.greenrobot.eventbus.Subscribe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wordpress.android.fluxc.TestUtils
import org.wordpress.android.fluxc.action.ActivityLogAction
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.example.BuildConfig
import org.wordpress.android.fluxc.generated.AccountActionBuilder
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.utils.StatsGranularity.DAYS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.MONTHS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.WEEKS
import org.wordpress.android.fluxc.network.utils.StatsGranularity.YEARS
import org.wordpress.android.fluxc.store.AccountStore.AuthenticatePayload
import org.wordpress.android.fluxc.store.AccountStore.OnAccountChanged
import org.wordpress.android.fluxc.store.AccountStore.OnAuthenticationChanged
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnSiteChanged
import org.wordpress.android.fluxc.store.SiteStore.SiteErrorType
import org.wordpress.android.fluxc.store.stats.time.PostAndPageViewsStore
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val PAGE_SIZE = 8

/**
 * Tests with real credentials on real servers using the full release stack (no mock)
 */
class ReleaseStack_TimeStatsTestJetpack : ReleaseStack_Base() {
    private val incomingActions: MutableList<Action<*>> = mutableListOf()
    @Inject lateinit var postAndPageViewsStore: PostAndPageViewsStore
    @Inject internal lateinit var siteStore: SiteStore

    private var nextEvent: TestEvents? = null

    internal enum class TestEvents {
        NONE,
        SITE_CHANGED,
        SITE_REMOVED,
        ERROR_DUPLICATE_SITE
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mReleaseStackAppComponent.inject(this)
        // Register
        init()
        // Reset expected test event
        nextEvent = TestEvents.NONE
        this.incomingActions.clear()
    }

    @Test
    fun testFetchPostAndPageDayViews() {
        val site = authenticate()

        val fetchedInsights = runBlocking { postAndPageViewsStore.fetchPostAndPageViews(site, PAGE_SIZE, DAYS, true) }

        assertNotNull(fetchedInsights)
        assertNotNull(fetchedInsights.model)

        val insightsFromDb = postAndPageViewsStore.getPostAndPageViews(site, DAYS, PAGE_SIZE)

        assertEquals(fetchedInsights.model, insightsFromDb)
    }

    @Test
    fun testFetchPostAndPageWeekViews() {
        val site = authenticate()

        val fetchedInsights = runBlocking { postAndPageViewsStore.fetchPostAndPageViews(site, PAGE_SIZE, WEEKS, true) }

        assertNotNull(fetchedInsights)
        assertNotNull(fetchedInsights.model)

        val insightsFromDb = postAndPageViewsStore.getPostAndPageViews(site, WEEKS, PAGE_SIZE)

        assertEquals(fetchedInsights.model, insightsFromDb)
    }

    @Test
    fun testFetchPostAndPageMonthViews() {
        val site = authenticate()

        val fetchedInsights = runBlocking { postAndPageViewsStore.fetchPostAndPageViews(site, PAGE_SIZE, MONTHS, true) }

        assertNotNull(fetchedInsights)
        assertNotNull(fetchedInsights.model)

        val insightsFromDb = postAndPageViewsStore.getPostAndPageViews(site, MONTHS, PAGE_SIZE)

        assertEquals(fetchedInsights.model, insightsFromDb)
    }

    @Test
    fun testFetchPostAndPageYearViews() {
        val site = authenticate()

        val fetchedInsights = runBlocking { postAndPageViewsStore.fetchPostAndPageViews(site, PAGE_SIZE, YEARS, true) }

        assertNotNull(fetchedInsights)
        assertNotNull(fetchedInsights.model)

        val insightsFromDb = postAndPageViewsStore.getPostAndPageViews(site, YEARS, PAGE_SIZE)

        assertEquals(fetchedInsights.model, insightsFromDb)
    }

    private fun authenticate(): SiteModel {
        authenticateWPComAndFetchSites(BuildConfig.TEST_WPCOM_USERNAME_SINGLE_JETPACK_ONLY,
                BuildConfig.TEST_WPCOM_PASSWORD_SINGLE_JETPACK_ONLY)

        return siteStore.sites[0]
    }

    @Subscribe
    fun onAuthenticationChanged(event: OnAuthenticationChanged) {
        if (event.isError) {
            throw AssertionError("Unexpected error occurred with type: " + event.error.type)
        }
        mCountDownLatch.countDown()
    }

    @Subscribe
    fun onAccountChanged(event: OnAccountChanged) {
        AppLog.d(T.TESTS, "Received OnAccountChanged event")
        if (event.isError) {
            throw AssertionError("Unexpected error occurred with type: " + event.error.type)
        }
        mCountDownLatch.countDown()
    }

    @Subscribe
    fun onSiteChanged(event: OnSiteChanged) {
        AppLog.i(T.TESTS, "site count " + siteStore.sitesCount)
        if (event.isError) {
            if (nextEvent == TestEvents.ERROR_DUPLICATE_SITE) {
                assertEquals(SiteErrorType.DUPLICATE_SITE, event.error.type)
                mCountDownLatch.countDown()
                return
            }
            throw AssertionError("Unexpected error occurred with type: " + event.error.type)
        }
        assertTrue(siteStore.hasSite())
        assertEquals(TestEvents.SITE_CHANGED, nextEvent)
        mCountDownLatch.countDown()
    }

    @Subscribe
    fun onAction(action: Action<*>) {
        if (action.type is ActivityLogAction) {
            incomingActions.add(action)
            mCountDownLatch?.countDown()
        }
    }

    @Throws(InterruptedException::class)
    private fun authenticateWPComAndFetchSites(username: String, password: String) {
        // Authenticate a test user (actual credentials declared in gradle.properties)
        val payload = AuthenticatePayload(username, password)
        mCountDownLatch = CountDownLatch(1)

        // Correct user we should get an OnAuthenticationChanged message
        mDispatcher.dispatch(AuthenticationActionBuilder.newAuthenticateAction(payload))
        // Wait for a network response / onChanged event
        assertTrue(mCountDownLatch.await(TestUtils.DEFAULT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS))

        // Fetch account from REST API, and wait for OnAccountChanged event
        mCountDownLatch = CountDownLatch(1)
        mDispatcher.dispatch(AccountActionBuilder.newFetchAccountAction())
        assertTrue(mCountDownLatch.await(TestUtils.DEFAULT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS))

        // Fetch sites from REST API, and wait for onSiteChanged event
        mCountDownLatch = CountDownLatch(1)
        nextEvent = TestEvents.SITE_CHANGED
        mDispatcher.dispatch(SiteActionBuilder.newFetchSitesAction())

        assertTrue(mCountDownLatch.await(TestUtils.DEFAULT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS))
        assertTrue(siteStore.sitesCount > 0)
    }
}