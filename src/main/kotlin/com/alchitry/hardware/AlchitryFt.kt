package com.alchitry.hardware

import com.alchitry.hardware.usb.ftdi.D3xx
import com.alchitry.hardware.usb.ftdi.D3xx.FT_DEVICE_600
import com.alchitry.hardware.usb.ftdi.D3xx.FT_DEVICE_601
import com.alchitry.hardware.usb.ftdi.D3xx.FT_FLAGS_SUPERSPEED

sealed class FtType(open val superSpeed: Boolean) {
    data class Ft(override val superSpeed: Boolean) : FtType(superSpeed)
    data class FtPlus(override val superSpeed: Boolean) : FtType(superSpeed)
}

class AlchitryFt {

    companion object {
        fun find_boards(): List<FtType> {
            val devices = D3xx.findDevices().mapNotNull { device ->
                val superSpeed = (device.flags and FT_FLAGS_SUPERSPEED) > 0
                when (device.type) {
                    FT_DEVICE_600 -> FtType.Ft(superSpeed)
                    FT_DEVICE_601 -> FtType.FtPlus(superSpeed)
                    else -> null
                }
            }
            println(devices)
            return devices
        }

        fun connect(index: Int) = D3xx.connectDevice(index)
    }
}