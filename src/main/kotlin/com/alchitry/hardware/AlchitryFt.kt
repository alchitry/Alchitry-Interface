package com.alchitry.hardware

import com.alchitry.hardware.usb.ftdi.D3xx
import com.alchitry.hardware.usb.ftdi.D3xx.FT_DEVICE_600
import com.alchitry.hardware.usb.ftdi.D3xx.FT_DEVICE_601
import com.alchitry.hardware.usb.ftdi.D3xx.FT_FLAGS_SUPERSPEED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class FtType(open val superSpeed: Boolean) {
    data class Ft(override val superSpeed: Boolean) : FtType(superSpeed)
    data class FtPlus(override val superSpeed: Boolean) : FtType(superSpeed)

    companion object {
        fun fromDeviceInfo(deviceInfo: D3xx.DeviceInfo): FtType? {
            val superSpeed = (deviceInfo.flags and FT_FLAGS_SUPERSPEED) > 0
            return when (deviceInfo.type) {
                FT_DEVICE_600 -> FtType.Ft(superSpeed)
                FT_DEVICE_601 -> FtType.FtPlus(superSpeed)
                else -> null
            }
        }
    }
}

class AlchitryFt(val type: FtType, private val connection: D3xx.DeviceConnection) : AutoCloseable {
    fun asyncWriteData(data: ByteArray): D3xx.DeviceConnection.OverlappedContext {
        val overlappedContext = connection.initializeOverlapped()
        connection.writePipeAsync(0, data, overlappedContext)
        return overlappedContext
    }

    suspend fun waitForWriteResult(context: D3xx.DeviceConnection.OverlappedContext): Pair<Int, D3xx.FtStatus> =
        withContext(Dispatchers.IO) {
            context.getResult(true)
        }

    fun checkWriteResult(context: D3xx.DeviceConnection.OverlappedContext): Pair<Int, D3xx.FtStatus> =
        context.getResult(false)

    fun asyncReadData(bytes: Int): D3xx.DeviceConnection.OverlappedContext {
        val overlappedContext = connection.initializeOverlapped()
        connection.readPipeAsync(0, bytes, overlappedContext)
        return overlappedContext
    }

    suspend fun waitForReadResult(context: D3xx.DeviceConnection.OverlappedContext): Pair<ByteArray, D3xx.FtStatus> =
        withContext(Dispatchers.IO) {
            context.getResultBytes(true)
        }

    fun checkReadResult(context: D3xx.DeviceConnection.OverlappedContext): Pair<ByteArray, D3xx.FtStatus> =
        context.getResultBytes(false)

    override fun close() {
        connection.close()
    }

    fun isConnected(): Boolean = connection.isConnected()

    companion object {
        fun find_boards(): List<FtType> {
            val devices = D3xx.findDevices().mapNotNull { device -> FtType.fromDeviceInfo(device) }
            println(devices)
            return devices
        }

        fun connect(index: Int): AlchitryFt {
            val device =
                D3xx.findDevices().getOrNull(index) ?: throw IllegalArgumentException("No device found at index $index")
            val ft = FtType.fromDeviceInfo(device)
                ?: throw RuntimeException("The device at index $index was not an Ft or Ft+")
            val connection = D3xx.connectDevice(index)
            return AlchitryFt(ft, connection)
        }
    }
}