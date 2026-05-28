package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.usb.ftdi.enums.BitMode
import net.sf.yad2xx.Device
import java.io.Closeable

interface Ftdi : Closeable {
    fun setTimeouts(read: Int, write: Int)
    fun usbReset()
    fun setChars(eventChar: Byte, eventEnable: Boolean, errorChar: Byte, errorEnable: Boolean)
    fun setLatencyTimer(time: Byte)
    fun setBitMode(pinDirection: Byte, bitMode: BitMode)
    fun writeData(data: ByteArray): Int
    fun readData(data: ByteArray): Int
    fun readDataWithTimeout(data: ByteArray): Int
    fun usbPurgeBuffers()
    fun readEEPROM(): String
    fun programEEPROM(data: String)
}
