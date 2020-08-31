package com.msa.oneway.sideeffects

import android.util.Log
import com.google.gson.Gson
import com.msa.oneway.Utils
import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.core.*
import com.msa.oneway.sample.data.TodoApiService
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.data.TodoRepositoryImpl
import com.msa.oneway.sample.domain.HomeStore
import com.msa.oneway.sample.domain.sideeffects.GetTodoListCoroutineSideEffect
import com.msa.oneway.sample.domain.sideeffects.GetTodoListRxSideEffect
import com.msa.oneway.sample.entities.HomeScreenState
import com.msa.oneway.sample.entities.TodoAction
import com.msa.oneway.sample.entities.TodoResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

@ExperimentalCoroutinesApi
class GetTodoListSideEffectTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val gson = Gson()

    private val todoApiService = mockk<TodoApiService>()
    private val todoRepository: TodoRepository = TodoRepositoryImpl(todoApiService)
    private val resourceRepository = mockk<ResourceRepository>()

    private val threadExecutor by lazy { CurrentThreadExecutor() }
    private val threadExecutorService by lazy { TestThreadExecutorService(threadExecutor) }
    private val scheduler by lazy { TestScheduler() }
    private val compositeDisposable by lazy { CompositeDisposable() }

    private val store by lazy { HomeStore(HomeScreenState(), threadExecutorService) }

    @Before
    fun setup() {

        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        val todoJson = Utils.getStringFromFile("response/todos.json")
        val todoResponse = gson.fromJson(todoJson, TodoResponse::class.java)

        val networkResponseSingle: Single<NetworkResponse<TodoResponse, Error>> = Single
            .just<NetworkResponse<TodoResponse, Error>>(NetworkResponse.Success(todoResponse))
            .delay(5, TimeUnit.MICROSECONDS, scheduler)

        every { todoApiService.getTodoListRx() } returns networkResponseSingle

        coEvery { todoApiService.getTodoListCoroutine() } returns todoResponse

        val coroutineDispatcherProvider =
            TestCoroutineDispatcherProvider(testCoroutineRule.testCoroutineDispatcher)

        val schedulerProvider = TestSchedulerProvider(scheduler)

        GetTodoListRxSideEffect(
            store = store,
            todoRepository = todoRepository,
            resourceRepository = resourceRepository,
            threadExecutorService = threadExecutorService,
            schedulerProvider = schedulerProvider,
            compositeDisposable = compositeDisposable
        )

        GetTodoListCoroutineSideEffect(
            store = store,
            todoRepository = todoRepository,
            resourceRepository = resourceRepository,
            threadExecutorService = threadExecutorService,
            coroutineDispatcherProvider = coroutineDispatcherProvider
        )

        TestActionListenerSideEffect(store, threadExecutorService)
        TestStateHandler(store, threadExecutorService)
    }

    @Test
    fun loadTodoRx() {
        assertTrue(store.state.loading)
        assertNull(store.state.todoResponse)

        store.dispatch(TodoAction.GetTodoListRxAction)

        scheduler.advanceTimeBy(5, TimeUnit.MICROSECONDS)

        assertFalse(store.state.loading)
        assertNotNull(store.state.todoResponse)
    }

    @Test
    fun loadTodoCoroutine() = testCoroutineRule.runBlockingTest {
        assertTrue(store.state.loading)
        assertNull(store.state.todoResponse)

        store.dispatch(TodoAction.GetTodoListCoroutineAction)

        //advanceTimeBy(1)

        assertFalse(store.state.loading)
        assertNotNull(store.state.todoResponse)
    }
}