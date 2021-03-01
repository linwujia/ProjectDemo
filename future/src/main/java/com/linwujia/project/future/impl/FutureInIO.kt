package com.linwujia.project.future.impl

import com.linwujia.project.future.Future
import com.linwujia.project.future.FutureAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

internal class FutureInIO<Parameter, Result>(runAction: OperationAfterException = OperationAfterException.INTERRUPT_ON_EXCEPTION,
                                             parent: Future<*, Parameter>? = null,
                                             action: FutureAction<Parameter, Result>
) : Future<Parameter, Result>(runAction, parent, action) {

    override fun startFuture(parameter: Parameter?) {
        GlobalScope.async(Dispatchers.IO) {
            var result: Result? = null
            var exception: Throwable? = null
            try {
                result = invokeAction(parameter)
            } catch (e: Exception) {
                exception = e
            } finally {
                proceedNextFutureChecked(exception, result)
            }
        }
    }
}