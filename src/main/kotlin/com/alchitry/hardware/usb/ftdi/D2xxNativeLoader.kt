package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.Env
import java.io.File
import java.lang.foreign.SymbolLookup
import java.nio.file.Files

/**
 * Loads the native D2xx library for the current OS and architecture,
 * extracting it from JAR resources to a temporary directory if needed.
 */
object D2xxNativeLoader {
    private val libDir: File by lazy { extractNativeLibrary() }

    val symbolLookup: SymbolLookup by lazy {
        val path = libDir.toPath().resolve(libraryFileName())
        SymbolLookup.libraryLookup(path, java.lang.foreign.Arena.global())
    }

    private fun osDir(): String = when (Env.os) {
        Env.OS.Windows -> "windows"
        Env.OS.Linux -> "linux"
        Env.OS.MacOS -> "macos"
        Env.OS.Unknown -> error("Unsupported operating system")
    }

    private fun archDir(): String {
        val arch = System.getProperty("os.arch").lowercase()
        return when (Env.os) {
            Env.OS.Windows -> when (arch) {
                "amd64", "x86_64" -> "x64"
                "x86", "i386", "i686" -> "x86"
                "aarch64", "arm64" -> "arm64"
                else -> error("Unsupported architecture: $arch")
            }
            Env.OS.Linux -> when (arch) {
                "amd64", "x86_64" -> "x64"
                "x86", "i386", "i686" -> "x86"
                "aarch64", "arm64" -> "arm64"
                else -> error("Unsupported architecture: $arch")
            }
            Env.OS.MacOS -> when (arch) {
                "amd64", "x86_64", "aarch64", "arm64" -> "x64-arm64"
                else -> error("Unsupported architecture: $arch")
            }
            Env.OS.Unknown -> error("Unsupported operating system")
        }
    }

    private fun libraryFileName(): String = when (Env.os) {
        Env.OS.Windows -> "ftd2xx.dll"
        Env.OS.Linux -> "libftd2xx.so"
        Env.OS.MacOS -> "libftd2xx.dylib"
        Env.OS.Unknown -> error("Unsupported operating system")
    }

    private fun resourceFileName(): String = when (Env.os) {
        Env.OS.Windows -> {
            val arch = System.getProperty("os.arch").lowercase()
            if (arch == "amd64" || arch == "x86_64") "FTD2XX64.dll" else "ftd2xx.dll"
        }
        Env.OS.Linux -> "libftd2xx.so.1.4.34"
        Env.OS.MacOS -> "libftd2xx.1.4.30.dylib"
        Env.OS.Unknown -> error("Unsupported operating system")
    }

    private fun extractNativeLibrary(): File {
        val resourcePath = "/native/${osDir()}/${archDir()}/${resourceFileName()}"
        val inputStream = D2xxNativeLoader::class.java.getResourceAsStream(resourcePath)
            ?: error("Native library not found in resources: $resourcePath")

        val tempDir = Files.createTempDirectory("d2xx-native").toFile()
        tempDir.deleteOnExit()

        val targetFile = File(tempDir, libraryFileName())
        targetFile.deleteOnExit()

        inputStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // On Linux/macOS, ensure the file is executable
        if (Env.os != Env.OS.Windows) {
            targetFile.setExecutable(true)
        }

        return tempDir
    }
}
