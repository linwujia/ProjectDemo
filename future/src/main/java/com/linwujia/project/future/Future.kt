@file:Suppress("NOTHING_TO_INLINE")

package com.linwujia.project.future

import com.linwujia.project.future.impl.FutureInIO
import com.linwujia.project.future.impl.FutureInMain
import com.linwujia.project.future.impl.FutureInNow
import kotlinx.coroutines.runBlocking

fun postIO(action: FutureAction<Unit, Unit>) {
    future {
        io(action = action)
    }
}

fun postMain(action: FutureAction<Unit, Unit>) {
    future {
        main(action = action)
    }
}

fun future(scope: FutureScope.() -> Unit) {
    FutureScope().apply {
        scope()
        start()
    }
}

internal typealias FutureAction<Result, NextResult> = suspend FutureScope.(Result?) -> NextResult
internal typealias ExceptionHandler = suspend FutureScope.(Throwable) -> Unit
internal typealias WhenCompleteHandler = () -> Unit

@DslMarker
internal annotation class FutureDslMarker

class FutureScope {
    private val mThread = Thread.currentThread()

    private val mFutures = arrayListOf<Future<Unit, *>>()
    private var mQuitParentFutureChain = false

    @Volatile
    private var mStart = false

    fun <Result> main(runAction: Future.OperationAfterException = Future.OperationAfterException.INTERRUPT_ON_EXCEPTION, @FutureDslMarker action: FutureAction<Unit, Result>): Future<Unit, Result> {
        if (mStart) {
            throw IllegalStateException("Future had passed , can not add more future")
        }
        checkThread()

        val future = FutureInMain<Unit, Result>(runAction, null, action)

        mFutures.add(future)
        return future
    }

    fun <Result> io(runAction: Future.OperationAfterException = Future.OperationAfterException.INTERRUPT_ON_EXCEPTION, @FutureDslMarker action: FutureAction<Unit, Result>): Future<Unit, Result> {
        if (mStart) {
            throw IllegalStateException("Future had passed , can not add more future")
        }
        checkThread()

        val future = FutureInIO<Unit, Result>(runAction, null, action)
        mFutures.add(future)
        return future
    }

    fun <Result> now(runAction: Future.OperationAfterException = Future.OperationAfterException.INTERRUPT_ON_EXCEPTION, @FutureDslMarker action: FutureAction<Unit, Result>): Future<Unit, Result> {
        if (mStart) {
            throw IllegalStateException("Future had passed , can not add more future")
        }
        checkThread()

        val future = FutureInNow<Unit, Result>(runAction, null, action)

        mFutures.add(future)
        return future
    }

    @FutureDslMarker
    fun quit() = null.also {
        mQuitParentFutureChain = true
    }

    internal fun hasQuit() = mQuitParentFutureChain

    private fun checkThread() = if (Thread.currentThread() != mThread) {
        throw IllegalAccessException("future scope should init on the thread it created")
    } else {
    }

    internal fun start() {
        checkThread()

        if (mStart) {
            return
        }

        mStart = true

        mFutures.forEach {
            it.start()
        }

        mFutures.clear()
    }
}

abstract class Future<Parameter, Result>(runAction: OperationAfterException,
                                         parent: Future<*, Parameter>? = null,
                                         action: FutureAction<Parameter, Result>
) {

    private val mParent = parent

    //scope for children
    private lateinit var mFutureScope: FutureScope

    //error handler
    private var mErrorHandler: ExceptionHandler? = null
    //call immediately after when computation returns or error is thrown
    private var mWhenCompleteHandler: WhenCompleteHandler? = null

    //child chain
    private var mNextFuture: Future<Result, *>? = null
    //real action
    private var mFutureAction: FutureAction<Parameter, Result>? = action

    private var mExceptionOperationAction = runAction

    internal fun start() {
        if (mParent != null) {
            throw IllegalAccessException("Future should call start with root future")
        }

        comeToFutureInternal(null)
    }

    private fun comeToFutureInternal(parameter: Parameter?) {
        try {
            startFuture(parameter)
        } catch (unCaught: Throwable) {
            unCaught.printStackTrace()
        }
    }

    @Throws(Throwable::class)
    protected abstract fun startFuture(parameter: Parameter?)

    open infix fun <NextResult> then(@FutureDslMarker then: FutureAction<Result, NextResult>): Future<Result, NextResult> = FutureInNow(mExceptionOperationAction, this, then).also {
        mNextFuture = it
    }

    open infix fun <NextResult> main(@FutureDslMarker main: FutureAction<Result, NextResult>): Future<Result, NextResult> = FutureInMain(mExceptionOperationAction, this, main).also {
        mNextFuture = it
    }

    open infix fun <NextResult> io(@FutureDslMarker io: FutureAction<Result, NextResult>): Future<Result, NextResult> = FutureInIO(mExceptionOperationAction, this, io).also {
        mNextFuture = it
    }

    open infix fun catchError(errorHandler: ExceptionHandler): Future<Parameter, Result> {
        mErrorHandler = errorHandler
        return this
    }

    open infix fun whenComplete(whenCompleteHandler: WhenCompleteHandler): Future<Parameter, Result> {
        mWhenCompleteHandler = whenCompleteHandler
        return this
    }

    protected fun invokeAction(parameter: Parameter?): Result? =
            runBlocking {
                mFutureAction?.invoke(FutureScope().also {
                    mFutureScope = it
                }, parameter).also {
                    mFutureScope.start()
                }
            }

    protected fun proceedNextFutureChecked(throwable: Throwable?, result: Result?) {
        if (throwable != null) {
            throwable.printStackTrace()

            runBlocking {
                mErrorHandler?.invoke(mFutureScope, throwable)
                mFutureScope.start()
            }
            proceedNextFutureInternal(false, result)
        } else {
            proceedNextFutureInternal(true, result)
        }
    }

    private fun proceedNextFutureInternal(noException: Boolean, result: Result?) {
        //quit when calling FutureScope.quit()
        val nextFuture: Future<Result, *>
        try {
            // call complete immediately
            mWhenCompleteHandler?.invoke()

            if (mFutureScope.hasQuit()) {
                return
            }

            nextFuture = mNextFuture ?: return
        } finally {
            //detach run action to avoid resources leak
            unBound()
        }

        when {
            noException -> nextFuture.comeToFutureInternal(result)
            mExceptionOperationAction == OperationAfterException.PROCEED_WITH_EXCEPTION -> nextFuture.comeToFutureInternal(null)
        }
    }

    private fun unBound() {
        mNextFuture = null
        mFutureAction = null
        mErrorHandler = null
    }

    enum class OperationAfterException {
        INTERRUPT_ON_EXCEPTION, PROCEED_WITH_EXCEPTION
    }
}