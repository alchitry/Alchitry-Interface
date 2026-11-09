package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.Board
import kotlin.io.encoding.Base64
import kotlin.random.Random

/**
 * Generates a random alphanumeric serial number suitable for FTDI EEPROMs.
 */
fun generateRandomSerial(length: Int = 6, random: Random = Random.Default): String {
    val charPool = ('0'..'9') + ('A'..'Z').toList()
    return (1..length)
        .map { charPool.random(random) }
        .joinToString("")
}

/**
 * Generates FT2232H EEPROM configuration image with Xilinx JTAG signature in the user area.
 *
 * @param board Name of the board placed in the Xilinx user area.
 * @param description Product description string descriptor (defaults to board name).
 * @param serial Serial number string descriptor (defaults to random 6-character alphanumeric string).
 * @param vendor Vendor name placed in the Xilinx user area (defaults to "Alchitry").
 * @param manufacturer Manufacturer string descriptor (defaults to "Xilinx").
 * @return Base64-encoded 2048-byte EEPROM image.
 */
fun generateEEPROM(
    board: String,
    description: String = board,
    serial: String = generateRandomSerial(),
    vendor: String = "Alchitry",
    manufacturer: String = "Xilinx"
): String {
    val buf = ByteArray(256)

    // Base FT2232H configuration header
    buf[0] = 0x08.toByte()
    buf[1] = 0x01.toByte() // Word 0: 0x0108
    buf[2] = 0x04.toByte()
    buf[3] = 0x03.toByte() // Word 1: VID = 0x0403
    buf[4] = 0x60.toByte()
    buf[5] = 0x10.toByte() // Word 2: PID = 0x6010
    buf[6] = 0x07.toByte()
    buf[7] = 0x00.toByte() // Word 3: bcdDevice = 0x0700 (FT2232H)
    buf[8] = 0x32.toByte()
    buf[9] = 0x80.toByte() // Word 4: Config = 0x8032
    buf[10] = 0x00.toByte()
    buf[11] = 0x08.toByte() // Word 5: 0x0800
    buf[12] = 0x00.toByte()
    buf[13] = 0x00.toByte() // Word 6: 0x0000

    // String descriptor layout calculation
    val mfgOffset = 0x9A
    val mfgLen = 2 + manufacturer.length * 2

    val descOffset = mfgOffset + mfgLen
    val descLen = 2 + description.length * 2

    val serialOffset = descOffset + descLen
    val serialLen = 2 + serial.length * 2

    // Header pointers (Words 7, 8, 9)
    buf[14] = mfgLen.toByte()
    buf[15] = mfgOffset.toByte()
    buf[16] = descLen.toByte()
    buf[17] = descOffset.toByte()
    buf[18] = serialLen.toByte()
    buf[19] = serialOffset.toByte()

    // Word 0x0C: 0x5600
    buf[24] = 0x00.toByte()
    buf[25] = 0x56.toByte()

    // Xilinx User Area starting at Word 0x0D (byte offset 0x1A)
    // Structure: 32-bit signature (0x584A0003: 0x03, 0x00, 0x4A, 0x58), vendor string (\0-terminated), board string (\0-terminated)
    val uaData = mutableListOf<Byte>(0x03, 0x00, 0x4A, 0x58)
    uaData.addAll(vendor.encodeToByteArray().toList())
    uaData.add(0)
    uaData.addAll(board.encodeToByteArray().toList())
    uaData.add(0)

    if (uaData.size % 2 != 0) {
        uaData.add(0)
    }

    // Write User Area into buffer with 16-bit word byte swap
    for (i in 0 until uaData.size step 2) {
        buf[0x1A + i] = uaData[i + 1]
        buf[0x1A + i + 1] = uaData[i]
    }

    // Helper to write FTDI string descriptors (UTF-16LE in word format: high byte char, low byte 0x00)
    fun writeStringDesc(offset: Int, str: String) {
        val length = 2 + str.length * 2
        buf[offset] = 0x03.toByte()
        buf[offset + 1] = length.toByte()
        for (idx in str.indices) {
            val code = str[idx].code
            buf[offset + 2 + idx * 2] = 0x00.toByte()
            buf[offset + 3 + idx * 2] = (code and 0xFF).toByte()
        }
    }

    writeStringDesc(mfgOffset, manufacturer)
    writeStringDesc(descOffset, description)
    writeStringDesc(serialOffset, serial)

    // Terminator at end of serial string descriptor
    val endOffset = serialOffset + serialLen
    buf[endOffset] = 0x03.toByte()
    buf[endOffset + 1] = 0x02.toByte()

    // 16-bit rotating checksum over words 0..126 (bytes 0..253)
    var checksum = 0xAAAA
    for (i in 0 until 127) {
        val word = (buf[i * 2].toInt() and 0xFF) or ((buf[i * 2 + 1].toInt() and 0xFF) shl 8)
        checksum = (word xor checksum) and 0xFFFF
        checksum = ((checksum shl 1) or (checksum ushr 15)) and 0xFFFF
    }

    buf[254] = (checksum and 0xFF).toByte()
    buf[255] = ((checksum ushr 8) and 0xFF).toByte()

    // FTDI EEPROMs repeat the 256-byte image across 2048 bytes (8x)
    val fullBuffer = ByteArray(2048)
    for (i in 0 until 8) {
        System.arraycopy(buf, 0, fullBuffer, i * 256, 256)
    }

    return Base64.encode(fullBuffer)
}

/**
 * Generates FT2232H EEPROM configuration image for a known [Board].
 */
fun generateEEPROM(
    board: Board,
    serial: String = generateRandomSerial(),
    vendor: String = "Alchitry",
    manufacturer: String = "Xilinx"
): String {
    val prod = board.usbDescriptor.product ?: board.name
    return generateEEPROM(prod, prod, serial, vendor, manufacturer)
}