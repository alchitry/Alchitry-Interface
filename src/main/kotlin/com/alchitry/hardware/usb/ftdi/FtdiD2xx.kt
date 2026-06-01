package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.Log
import com.alchitry.hardware.usb.SerialDevice
import com.alchitry.hardware.usb.ftdi.enums.BitMode
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import kotlin.io.encoding.Base64

class FtdiD2xx(val handle: MemorySegment) : Ftdi, SerialDevice {
    private var readTimeout = 0
    private var writeTimeout = 0

    init {
        D2xx.setDataCharacteristics(handle, 8, 0, D2xx.FT_PARITY_NONE)
        D2xx.setFlowControl(handle, D2xx.FT_FLOW_NONE, 0, 0)
    }

    override fun readEEPROM(): String {
        val buffer = ByteBuffer.allocate(2048)
        for (i in 0..1023) {
            buffer.putShort(D2xx.readEE(handle, i))
        }
        val byteArray = ByteArray(2048)
        buffer.flip()
        buffer.get(byteArray)
        return Base64.encode(byteArray)
    }

    override fun programEEPROM(data: String) {
        Log.println("Programming EEPROM:")
        val buffer = ByteBuffer.allocate(2048)
        buffer.put(Base64.decode(data))
        buffer.flip()
        Log.println("Erasing...")
        D2xx.eraseEE(handle)
        Log.progressBar("Writing...", 1024) {
            for (i in 0..1023) {
                D2xx.writeEE(handle, i, buffer.getShort())
                it.step()
            }
        }
        Log.success("Done.")
    }

    override fun setTimeouts(read: Int, write: Int) {
        readTimeout = read
        writeTimeout = write
        D2xx.setTimeouts(handle, read, write)
    }

    override fun usbReset() {
        D2xx.resetDevice(handle)
    }

    override fun setChars(eventChar: Byte, eventEnable: Boolean, errorChar: Byte, errorEnable: Boolean) {
        D2xx.setChars(
            handle,
            eventChar, if (eventEnable) 1 else 0,
            errorChar, if (errorEnable) 1 else 0
        )
    }

    override fun setLatencyTimer(time: Byte) {
        D2xx.setLatencyTimer(handle, time)
    }

    override fun setBitMode(pinDirection: Byte, bitMode: BitMode) {
        D2xx.setBitMode(handle, pinDirection, bitMode.mask)
    }

    override fun writeData(data: ByteArray): Int {
        return D2xx.write(handle, data)
    }

    override fun readData(data: ByteArray): Int {
        val bytesToRead = D2xx.getQueueStatus(handle).coerceAtMost(data.size)
        if (bytesToRead == 0) return 0
        return D2xx.read(handle, data, bytesToRead)
    }

    override fun readDataWithTimeout(data: ByteArray): Int {
        return D2xx.read(handle, data)
    }

    override fun close() {
        try {
            D2xx.close(handle)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    override fun usbPurgeBuffers() {
        D2xx.purge(handle, D2xx.FT_PURGE_RX or D2xx.FT_PURGE_TX)
    }

    override fun setBaudrate(baud: Int): Int {
        D2xx.setBaudRate(handle, baud)
        return baud
    }

    override fun setDtrRts(dtr: Boolean, rts: Boolean) {
        D2xx.setDtr(handle, dtr)
        D2xx.setRts(handle, rts)
    }

    override fun flushReadBuffer() {
        D2xx.purge(handle, D2xx.FT_PURGE_RX)
        while (D2xx.getQueueStatus(handle) > 0) {
            D2xx.read(handle, ByteArray(D2xx.getQueueStatus(handle)))
        }
    }
}
