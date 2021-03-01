package com.linwujia.project.future.impl

import com.linwujia.project.future.Future
import com.linwujia.project.future.FutureAction

internal class FutureInNow<Parameter, Result>(runAction: OperationAfterException = OperationAfterException.INTERRUPT_ON_EXCEPTION,
                                              parent: Future<*, Parameter>? = null,
                                              action: FutureAction<Parameter, Result>
) : Future<Parameter, Result>(runAction, parent, action) {

    override fun startFuture(parameter: Parameter?) {
        var result: Result? = null
        var exception: Throwable? = null
        try {
            result = invokeAction(parameter)
        } catch (e: Throwable) {
            exception = e
        } finally {
            proceedNextFutureChecked(exception, result)
        }
    }
}