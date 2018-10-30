package org.wordpress.android.fluxc.persistence

import com.wellsql.generated.PostSummaryModelTable
import com.yarolegovich.wellsql.WellSql
import org.wordpress.android.fluxc.model.list.PostSummaryModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListItemSummarySqlUtils @Inject constructor() {
    /**
     * This function inserts the [postSummaryList] in the [PostSummaryModelTable].
     *
     * Unique constraint in [PostSummaryModel] will ensure that the existing summaries are replaced with the new ones.
     */
    fun insertPostSummaryList(postSummaryList: List<PostSummaryModel>) {
        WellSql.insert(postSummaryList).asSingleTransaction(true).execute()
    }

    /**
     * This function returns a map of remote post ids to [PostSummaryModel]s for the given [localSiteId].
     */
    fun getPostSummariesByRemotePostIds(remotePostIds: List<Long>?, localSiteId: Int): Map<Long, PostSummaryModel> {
        remotePostIds?.let { remoteIds ->
            if (remoteIds.isNotEmpty()) {
                val list = WellSql.select(PostSummaryModel::class.java)
                        .where().isIn(PostSummaryModelTable.REMOTE_POST_ID, remoteIds)
                        .equals(PostSummaryModelTable.LOCAL_SITE_ID, localSiteId).endWhere()
                        .asModel
                return list.associateBy {
                    val remotePostId = requireNotNull(it.remotePostId) {
                        "PostSummaryModel's must have remotePostId!"
                    }
                    remotePostId
                }
            }
        }
        return emptyMap()
    }
}
