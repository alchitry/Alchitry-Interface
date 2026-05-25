package com.alchitry.hardware

import me.tongfei.progressbar.*
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

object Log {
    var listener: LogListener? = null

    interface LogListener {
        fun onMessage(message: String)
        fun onError(message: String)
        fun onSuccess(message: String)
        fun progressBar(taskName: String, total: Long): ProgressBar
    }

    fun println(message: String = "") {
        listener?.onMessage(message) ?: kotlin.io.println(message)
    }

    fun error(message: String) {
        listener?.onError(message) ?: System.err.println("ERROR: $message")
    }

    fun success(message: String) {
        listener?.onSuccess(message) ?: kotlin.io.println(message)
    }

    fun exception(e: Throwable) {
        error(e.message ?: e.toString())
    }

    val defaultBarStyle: ProgressBarStyle = if (Env.isWindows) {
        ProgressBarStyle.ASCII
    } else {
        ProgressBarStyleBuilder().apply {
            leftBracket("\u001b[38;2;225;154;26m│")
            rightBracket("│\u001b[0m")
            block('█')
            fractionSymbols(" ▏▎▍▌▋▊▉")
            rightSideFractionSymbol(' ')
        }.build()
    }

    inline fun progressBar(name: String, max: Long, block: (ProgressBar) -> Unit) {
        val progressBar = listener?.progressBar(name, max) ?: ProgressBarBuilder().apply {
            setStyle(defaultBarStyle)
            setTaskName(name)
            setInitialMax(max)
            setUpdateIntervalMillis(250)
            setConsumer(ConsoleProgressBarConsumer(PrintStream(FileOutputStream(FileDescriptor.out))))
        }.build()
        progressBar.use(block)
    }

}
