package org.wordpress.android.fluxc.model.list

import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.ListActionBuilder
import org.wordpress.android.fluxc.model.list.Either.Left
import org.wordpress.android.fluxc.model.list.Either.Right
import org.wordpress.android.fluxc.store.ListStore.FetchListPayload

sealed class Either<L, R> {
    class Left<L, R>(val value: L) : Either<L, R>()

    class Right<L, R>(val value: R) : Either<L, R>()
}

class SectionedListManager<S, T>(
    private val dispatcher: Dispatcher,
    private val listDescriptor: ListDescriptor,
    private val items: List<ListItemModel>,
    private val listData: Map<Long, T>,
    private val loadMoreOffset: Int,
    val isFetchingFirstPage: Boolean,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean,
    private val fetchItem: (Long) -> Unit,
    private val fetchList: (ListDescriptor, Int) -> Unit
) {
    val size: Int = items.size

    private var dispatchedRefreshAction = false
    private var dispatchedLoadMoreAction = false
    private val fetchRemoteItemSet = HashSet<Long>()

    private fun remoteItemId(remoteItemIndex: Int): Long {
        return items[remoteItemIndex].remoteItemId
    }

    fun getItem(
        position: Int,
        shouldFetchIfNull: Boolean = true,
        shouldLoadMoreIfNecessary: Boolean = true
    ): Either<S, T>? {
        if (shouldLoadMoreIfNecessary && position > size - loadMoreOffset) {
            loadMore()
        }
        val remoteItemId = items[position].remoteItemId
        val item = listData[remoteItemId]
        if (item == null) {
            if (shouldFetchIfNull) {
                fetchItemIfNecessary(remoteItemId)
            }
            return null
        }
        return Right(item)
    }

    fun refresh(): Boolean {
        if (!isFetchingFirstPage && !dispatchedRefreshAction) {
            val fetchListPayload = FetchListPayload(listDescriptor, false, fetchList)
            dispatcher.dispatch(ListActionBuilder.newFetchListAction(fetchListPayload))
            dispatchedRefreshAction = true
            return true
        }
        return false
    }

    private fun loadMore() {
        if (canLoadMore && !dispatchedLoadMoreAction) {
            dispatcher.dispatch(ListActionBuilder.newFetchListAction(FetchListPayload(listDescriptor, true, fetchList)))
            dispatchedLoadMoreAction = true
        }
    }

    private fun fetchItemIfNecessary(remoteItemId: Long) {
        if (!fetchRemoteItemSet.contains(remoteItemId)) {
            fetchItem(remoteItemId)
            fetchRemoteItemSet.add(remoteItemId)
        }
    }
}
