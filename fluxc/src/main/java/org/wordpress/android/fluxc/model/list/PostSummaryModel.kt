package org.wordpress.android.fluxc.model.list

import com.yarolegovich.wellsql.core.Identifiable
import com.yarolegovich.wellsql.core.annotation.Column
import com.yarolegovich.wellsql.core.annotation.PrimaryKey
import com.yarolegovich.wellsql.core.annotation.RawConstraints
import com.yarolegovich.wellsql.core.annotation.Table

@Table
@RawConstraints(
        "FOREIGN KEY(LOCAL_SITE_ID) REFERENCES SiteModel(_id) ON DELETE CASCADE",
        "UNIQUE (REMOTE_POST_ID) ON CONFLICT REPLACE"
)
class PostSummaryModel(@PrimaryKey @Column private var id: Int = 0) : Identifiable {
    constructor(localSiteId: Int, remotePostId: Long, title: String): this() {
        this.localSiteId = localSiteId
        this.remotePostId = remotePostId
        this.title = title
    }

    @Column var localSiteId: Int? = null
    @Column var remotePostId: Long? = null
    @Column var title: String? = null

    override fun getId(): Int = id

    override fun setId(id: Int) {
        this.id = id
    }
}
