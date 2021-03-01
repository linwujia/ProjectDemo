package com.linwujia.project.future

import kotlin.contracts.ExperimentalContracts

@UseExperimental(ExperimentalContracts::class)
fun main(args: Array<String>) {
    startTestAutomatic()
    Thread.sleep(1000)
}

@ExperimentalContracts
fun startTestAutomatic() {

    future {
        io(Future.OperationAfterException.PROCEED_WITH_EXCEPTION) {
           throw Exception()
        }catchError {
            println("catchError")
        } whenComplete {
            println("whenComplete")
        } then {
            println("then")
        }
    }

    Thread.sleep(10000)
}