package org.wordpress.android.fluxc.list

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import com.nhaarman.mockitokotlin2.firstValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.list.ListDescriptor
import org.wordpress.android.fluxc.model.list.ListState
import org.wordpress.android.fluxc.model.list.PagedListItemType
import org.wordpress.android.fluxc.model.list.PagedListWrapper
import org.wordpress.android.fluxc.store.ListStore.ListError
import org.wordpress.android.fluxc.store.ListStore.ListErrorType.GENERIC_ERROR
import org.wordpress.android.fluxc.store.ListStore.ListErrorType.PERMISSION_ERROR
import org.wordpress.android.fluxc.store.ListStore.OnListStateChanged

private fun onlyOnce() = times(1)

@RunWith(MockitoJUnitRunner::class)
class PagedListWrapperTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockLiveData = MutableLiveData<PagedList<PagedListItemType<String>>>()
    private val mockDispatcher = mock<Dispatcher>()
    private val mockListDescriptor = mock<ListDescriptor>()
    private val mockRefresh = mock<() -> Unit>()
    private val mockInvalidate = mock<() -> Unit>()
    private val mockIsListEmpty = mock<() -> Boolean>()

    private fun createPagedListWrapper(lifecycle: Lifecycle = mock()) = PagedListWrapper(
            data = mockLiveData,
            dispatcher = mockDispatcher,
            listDescriptor = mockListDescriptor,
            lifecycle = lifecycle,
            refresh = mockRefresh,
            invalidate = mockInvalidate,
            isListEmpty = mockIsListEmpty
    )

    @Test
    fun `registers dispatcher and observes lifecycle in init`() {
        val mockLifecycle = mock<Lifecycle>()

        val pagedListWrapper = createPagedListWrapper(mockLifecycle)

        verify(mockDispatcher, onlyOnce()).register(pagedListWrapper)
        verify(mockLifecycle, onlyOnce()).addObserver(pagedListWrapper)
    }

    @Test
    fun `isListEmpty is updated in init`() {
        createPagedListWrapper()

        verify(mockIsListEmpty, onlyOnce()).invoke()
    }

    @Test
    fun `unregisters dispatcher and stops observing lifecycle on destroy`() {
        val lifecycle = LifecycleRegistry(mock())
        assertThat(lifecycle.observerCount).isEqualTo(0)
        lifecycle.markState(Lifecycle.State.CREATED)

        val pagedListWrapper = createPagedListWrapper(lifecycle)
        assertThat(lifecycle.observerCount).isEqualTo(1)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        verify(mockDispatcher, onlyOnce()).register(pagedListWrapper)
        verify(mockDispatcher, onlyOnce()).unregister(pagedListWrapper)
        assertThat(lifecycle.observerCount).isEqualTo(0)
    }

    @Test
    fun `fetchFirstPage invokes refresh property`() {
        val pagedListWrapper = createPagedListWrapper()

        pagedListWrapper.fetchFirstPage()

        verify(mockRefresh, onlyOnce()).invoke()
    }

    @Test
    fun `invalidateData invokes invalidate property`() {
        val pagedListWrapper = createPagedListWrapper()

        pagedListWrapper.invalidateData()

        verify(mockInvalidate, onlyOnce()).invoke()
    }

    @Test
    fun `fetchingFirstPage ListState is propagated correctly`() {
        testListStateIsPropagatedCorrectly(ListState.FETCHING_FIRST_PAGE)
    }

    @Test
    fun `loadingMore ListState is propagated correctly`() {
        testListStateIsPropagatedCorrectly(ListState.LOADING_MORE)
    }

    @Test
    fun `default ListState is propagated correctly`() {
        testListStateIsPropagatedCorrectly(ListState.defaultState)
    }

    @Test
    fun `permission ListError is propagated correctly`() {
        testListStateIsPropagatedCorrectly(ListState.ERROR, ListError(PERMISSION_ERROR))
    }

    @Test
    fun `generic ListError is propagated correctly`() {
        testListStateIsPropagatedCorrectly(ListState.ERROR, ListError(GENERIC_ERROR))
    }

    private fun testListStateIsPropagatedCorrectly(listState: ListState, listError: ListError? = null) {
        val pagedListWrapper = createPagedListWrapper()
        val isFetchingFirstPageObserver = mock<Observer<Boolean>>()
        val isLoadingMoreObserver = mock<Observer<Boolean>>()
        val listErrorObserver = mock<Observer<ListError?>>()
        pagedListWrapper.isFetchingFirstPage.observeForever(isFetchingFirstPageObserver)
        pagedListWrapper.isLoadingMore.observeForever(isLoadingMoreObserver)
        pagedListWrapper.listError.observeForever(listErrorObserver)

        val event = OnListStateChanged(mockListDescriptor, listState, listError)
        pagedListWrapper.onListStateChanged(event)

        captureAndVerifySingleValue(isFetchingFirstPageObserver, listState.isFetchingFirstPage())
        captureAndVerifySingleValue(isLoadingMoreObserver, listState.isLoadingMore())
        captureAndVerifySingleValue(listErrorObserver, listError)
    }

    private inline fun <reified T> captureAndVerifySingleValue(observer: Observer<T>, result: T) {
        val captor = ArgumentCaptor.forClass(T::class.java)
        verify(observer, onlyOnce()).onChanged(captor.capture())
        assertThat(captor.firstValue).isEqualTo(result)
    }
}