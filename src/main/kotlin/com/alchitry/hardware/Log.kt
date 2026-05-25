package com.alchitry.hardware

object Log {
    var listener: LogListener? = null

    interface LogListener {
        fun onMessage(message: String)
        fun onError(message: String)
        fun onSuccess(message: String)
        fun onProgress(taskName: String, current: Long, total: Long)
    }

    fun println(message: String = "") {
        listener?.onMessage(message) ?: kotlin.io.println(message)
    }

    fun println(message: String, @Suppress("UNUSED_PARAMETER") color: Any?) {
        println(message)
    }

    fun error(message: String) {
        listener?.onError(message) ?: System.err.println("ERROR: $message")
    }

    fun success(message: String) {
        listener?.onSuccess(message) ?: kotlin.io.println(message)
    }

    fun printlnError(message: String?) {
        error(message ?: "Unknown error")
    }

    fun exception(e: Throwable) {
        error(e.message ?: e.toString())
    }

    inline fun progressBar(taskName: String, total: Long, block: (ProgressBar) -> Unit) {
        val pb = ProgressBar(taskName, total)
        block(pb)
        pb.close()
    }

    class ProgressBar(private val taskName: String, private val total: Long) {
        private var current: Long = 0

        fun stepTo(value: Long) {
            current = value
            listener?.onProgress(taskName, current, total)
        }

        fun stepBy(amount: Long) {
            current += amount
            listener?.onProgress(taskName, current, total)
        }

        fun close() {
            // no-op
        }
    }
}
