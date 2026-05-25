package com.alchitry.hardware

import java.util.*

object Env {
    enum class OS { Unknown, Windows, Linux, MacOS }

    val os: OS = System.getProperty("os.name").lowercase(Locale.getDefault()).let {
        when {
            it.startsWith("win", ignoreCase = true) -> OS.Windows
            it.startsWith("linux", ignoreCase = true) -> OS.Linux
            it.equals("Mac OS X", ignoreCase = true) -> OS.MacOS
            else -> OS.Unknown
        }
    }
    val isWindows: Boolean get() = os == OS.Windows
    val isLinux: Boolean get() = os == OS.Linux
    val isMac: Boolean get() = os == OS.MacOS
}
