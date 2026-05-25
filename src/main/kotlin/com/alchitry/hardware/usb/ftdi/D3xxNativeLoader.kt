package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.Env
import com.alchitry.hardware.Log
import java.io.File
import java.lang.foreign.SymbolLookup
import java.nio.file.Files

/**
 * Loads the native D3xx library for the current OS and architecture,
 * extracting it from JAR resources to a temporary directory if needed.
 */
object D3xxNativeLoader {
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
        return when {
            arch == "amd64" || arch == "x86_64" -> "x64"
            arch == "x86" || arch == "i386" || arch == "i686" -> "x86"
            arch == "aarch64" || arch == "arm64" -> "arm64"
            else -> error("Unsupported architecture: $arch")
        }
    }

    private fun libraryFileName(): String = when (Env.os) {
        Env.OS.Windows -> "FTD3XXWU.dll"
        Env.OS.Linux -> "libftd3xx.so"
        Env.OS.MacOS -> "libftd3xx.dylib"
        Env.OS.Unknown -> error("Unsupported operating system")
    }

    private fun resourceFileName(): String = when (Env.os) {
        Env.OS.Windows -> "FTD3XXWU.dll"
        Env.OS.Linux -> "libftd3xx.so.1.1.6"
        Env.OS.MacOS -> "libftd3xx.1.1.6.dylib"
        Env.OS.Unknown -> error("Unsupported operating system")
    }

    private fun extractNativeLibrary(): File {
        val resourcePath = "/native/${osDir()}/${archDir()}/${resourceFileName()}"
        val inputStream = D3xxNativeLoader::class.java.getResourceAsStream(resourcePath)
            ?: error("Native library not found in resources: $resourcePath")

        val tempDir = Files.createTempDirectory("d3xx-native").toFile()
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

        Log.println("Extracted D3xx native library to: ${targetFile.absolutePath}")
        return tempDir
    }
}
