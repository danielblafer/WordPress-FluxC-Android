package org.wordpress.android.fluxc.list

import com.yarolegovich.wellsql.WellSql
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.list.PostSummaryModel
import org.wordpress.android.fluxc.persistence.ListItemSummarySqlUtils
import org.wordpress.android.fluxc.persistence.SiteSqlUtils
import org.wordpress.android.fluxc.persistence.WellSqlConfig
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ListItemSummarySqlUtilsTest {
    private lateinit var listItemSummarySqlUtils: ListItemSummarySqlUtils

    @Before
    fun setUp() {
        val appContext = RuntimeEnvironment.application.applicationContext
        val config = WellSqlConfig(appContext)
        WellSql.init(config)
        config.reset()

        listItemSummarySqlUtils = ListItemSummarySqlUtils()
    }

    @Test
    fun testInsertPostListSummary() {
        val site = insertTestSite()
        val remotePostIds = (1..100L).toList()
        val postSummaryList = remotePostIds.map { PostSummaryModel(site.id, it, "") }
        /**
         * 1. Insert 100 post summaries in DB
         * 2. Verify that they were inserted correctly
         */
        listItemSummarySqlUtils.insertPostSummaryList(postSummaryList)
        val map = listItemSummarySqlUtils.getPostSummariesByRemotePostIds(remotePostIds, site.id)
        assertEquals(remotePostIds.size, map.size, "Not all summaries were inserted correctly")
        postSummaryList.forEach {
            val post = requireNotNull(map[it.remotePostId]) {
                "Post wasn't inserted correctly"
            }
            assertEquals(it.remotePostId, post.remotePostId, "Inserted summary doesn't have the correct remote post id")
            assertEquals(it.localSiteId, post.localSiteId, "Inserted summary doesn't have the correct local site id")
            assertEquals(it.title, post.title, "Inserted summary doesn't have the correct title")
        }
    }

    private fun insertTestSite(localSiteId: Int = 111): SiteModel {
        val site = SiteModel()
        site.id = localSiteId
        SiteSqlUtils.insertOrUpdateSite(site)
        return site
    }
}
