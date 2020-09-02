package com.msa.oneway.sideeffects

import android.util.Log
import com.google.gson.Gson
import com.msa.oneway.Utils
import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.core.*
import com.msa.oneway.sample.data.TodoApiService
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.data.TodoRepositoryImpl
import com.msa.oneway.sample.domain.HomeStore
import com.msa.oneway.sample.entities.HomeScreenState
import com.msa.oneway.sample.entities.TodoAction
import com.msa.oneway.sample.entities.TodoResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

@ExperimentalCoroutinesApi
class DummySideEffectTest {


    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val gson = Gson()

    private val todoApiService = mockk<TodoApiService>()
    private val todoRepository: TodoRepository = TodoRepositoryImpl(todoApiService)

    private val threadExecutor by lazy { CurrentThreadExecutor() }
    private val threadExecutorService by lazy { TestThreadExecutorService(threadExecutor) }
    private val scheduler by lazy { TestScheduler() }
    private val schedulerProvider by lazy { TestSchedulerProvider(scheduler) }
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

        val coroutineDispatcherProvider =
            TestCoroutineDispatcherProvider(testCoroutineRule.testCoroutineDispatcher)

        DummyCoroutineSideEffect(
            store = store,
            threadExecutorService = threadExecutorService,
            coroutineDispatcherProvider = coroutineDispatcherProvider
        )

        TestActionListenerSideEffect(store, threadExecutorService)
        TestStateHandler(store, threadExecutorService)
    }

    @Test
    fun demoRunWithRx() {

        val testObserver = TestObserver<NetworkResponse<TodoResponse, Error>>()
        todoRepository.getTodoListRx().delay(5, TimeUnit.MICROSECONDS).subscribe(testObserver)
        scheduler.advanceTimeBy(5, TimeUnit.MICROSECONDS)

        testObserver.awaitTerminalEvent()

        testObserver.assertNoErrors()
            .assertValue { it is NetworkResponse.Success }
            .assertComplete()
    }

    @Test
    fun demoRunWithCoroutine() = testCoroutineRule.runBlockingTest {
        assert(store.state.todoResponse == null)
        store.dispatch(TodoAction.UpdateCountAction)
        advanceTimeBy(500)
        assert(store.state.count == 2)
        println(store.state.count)
    }
}

class DummyCoroutineSideEffect(
    store: Store<*>,
    threadExecutorService: ThreadExecutorService,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseCoroutineSideEffect(
    store,
    threadExecutorService,
    coroutineDispatcherProvider
) {

    override fun handle(action: Action) {
        println("DummyCoroutineSideEffect handle action = ${action.javaClass.simpleName}")

        if (action !is TodoAction.UpdateCountAction) {
            return
        }

        launch(coroutineDispatcherProvider.IO) {
            println("start")
            delay(500)
            dispatch(TodoAction.SetCountAction(2))
            println("end")
        }
    }
}