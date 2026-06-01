package com.alchitry.hardware.usb.ftdi

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement
import java.lang.foreign.ValueLayout.*
import java.lang.invoke.MethodHandle

/**
 * Project Panama (FFM) bindings for the FTDI D2xx library (ftd2xx).
 */
object D2xx {
    private val linker = Linker.nativeLinker()
    private val lookup = D2xxNativeLoader.symbolLookup

    private fun downcall(name: String, descriptor: FunctionDescriptor): MethodHandle {
        val addr = lookup.find(name).orElseThrow { UnsatisfiedLinkError("Symbol not found: $name") }
        return linker.downcallHandle(addr, descriptor)
    }

    // ========================================================================
    // Constants
    // ========================================================================

    const val FT_OK = 0
    const val FT_INVALID_HANDLE = 1
    const val FT_DEVICE_NOT_FOUND = 2
    const val FT_DEVICE_NOT_OPENED = 3
    const val FT_IO_ERROR = 4
    const val FT_INSUFFICIENT_RESOURCES = 5
    const val FT_INVALID_PARAMETER = 6
    const val FT_INVALID_BAUD_RATE = 7
    const val FT_DEVICE_NOT_OPENED_FOR_ERASE = 8
    const val FT_DEVICE_NOT_OPENED_FOR_WRITE = 9
    const val FT_FAILED_TO_WRITE_DEVICE = 10
    const val FT_EEPROM_READ_FAILED = 11
    const val FT_EEPROM_WRITE_FAILED = 12
    const val FT_EEPROM_ERASE_FAILED = 13
    const val FT_EEPROM_NOT_PRESENT = 14
    const val FT_EEPROM_NOT_PROGRAMMED = 15
    const val FT_INVALID_ARGS = 16
    const val FT_NOT_SUPPORTED = 17
    const val FT_OTHER_ERROR = 18

    // Open flags
    const val FT_OPEN_BY_SERIAL_NUMBER = 1
    const val FT_OPEN_BY_DESCRIPTION = 2
    const val FT_OPEN_BY_LOCATION = 4

    // Parity
    const val FT_PARITY_NONE: Byte = 0
    const val FT_PARITY_ODD: Byte = 1
    const val FT_PARITY_EVEN: Byte = 2
    const val FT_PARITY_MARK: Byte = 3
    const val FT_PARITY_SPACE: Byte = 4

    // Flow control
    const val FT_FLOW_NONE: Short = 0x0000
    const val FT_FLOW_RTS_CTS: Short = 0x0100
    const val FT_FLOW_DTR_DSR: Short = 0x0200
    const val FT_FLOW_XON_XOFF: Short = 0x0400

    // Purge flags
    const val FT_PURGE_RX = 1
    const val FT_PURGE_TX = 2

    // Bit modes
    const val FT_BITMODE_RESET: Byte = 0x00
    const val FT_BITMODE_ASYNC_BITBANG: Byte = 0x01
    const val FT_BITMODE_MPSSE: Byte = 0x02
    const val FT_BITMODE_SYNC_BITBANG: Byte = 0x04
    const val FT_BITMODE_MCU_HOST: Byte = 0x08
    const val FT_BITMODE_FAST_SERIAL: Byte = 0x10
    const val FT_BITMODE_CBUS_BITBANG: Byte = 0x20
    const val FT_BITMODE_SYNC_FIFO: Byte = 0x40

    fun ftStatusToString(status: Int): String = when (status) {
        FT_OK -> "FT_OK"
        FT_INVALID_HANDLE -> "FT_INVALID_HANDLE"
        FT_DEVICE_NOT_FOUND -> "FT_DEVICE_NOT_FOUND"
        FT_DEVICE_NOT_OPENED -> "FT_DEVICE_NOT_OPENED"
        FT_IO_ERROR -> "FT_IO_ERROR"
        FT_INSUFFICIENT_RESOURCES -> "FT_INSUFFICIENT_RESOURCES"
        FT_INVALID_PARAMETER -> "FT_INVALID_PARAMETER"
        FT_INVALID_BAUD_RATE -> "FT_INVALID_BAUD_RATE"
        FT_DEVICE_NOT_OPENED_FOR_ERASE -> "FT_DEVICE_NOT_OPENED_FOR_ERASE"
        FT_DEVICE_NOT_OPENED_FOR_WRITE -> "FT_DEVICE_NOT_OPENED_FOR_WRITE"
        FT_FAILED_TO_WRITE_DEVICE -> "FT_FAILED_TO_WRITE_DEVICE"
        FT_EEPROM_READ_FAILED -> "FT_EEPROM_READ_FAILED"
        FT_EEPROM_WRITE_FAILED -> "FT_EEPROM_WRITE_FAILED"
        FT_EEPROM_ERASE_FAILED -> "FT_EEPROM_ERASE_FAILED"
        FT_EEPROM_NOT_PRESENT -> "FT_EEPROM_NOT_PRESENT"
        FT_EEPROM_NOT_PROGRAMMED -> "FT_EEPROM_NOT_PROGRAMMED"
        FT_INVALID_ARGS -> "FT_INVALID_ARGS"
        FT_NOT_SUPPORTED -> "FT_NOT_SUPPORTED"
        FT_OTHER_ERROR -> "FT_OTHER_ERROR"
        else -> "FT_STATUS($status)"
    }

    // ========================================================================
    // Struct Layouts
    // ========================================================================

    val FT_DEVICE_LIST_INFO_NODE: StructLayout = MemoryLayout.structLayout(
        JAVA_INT.withName("Flags"),
        JAVA_INT.withName("Type"),
        JAVA_INT.withName("ID"),
        JAVA_INT.withName("LocId"),
        MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("SerialNumber"),
        MemoryLayout.sequenceLayout(64, JAVA_BYTE).withName("Description"),
        ADDRESS.withName("ftHandle"),
    ).withName("FT_DEVICE_LIST_INFO_NODE")

    // ========================================================================
    // Function Downcall Handles
    // ========================================================================

    // FT_STATUS FT_CreateDeviceInfoList(LPDWORD lpdwNumDevs)
    val FT_CreateDeviceInfoList: MethodHandle = downcall(
        "FT_CreateDeviceInfoList",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_GetDeviceInfoList(FT_DEVICE_LIST_INFO_NODE *pDest, LPDWORD lpdwNumDevs)
    val FT_GetDeviceInfoList: MethodHandle = downcall(
        "FT_GetDeviceInfoList",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_Open(int deviceNumber, FT_HANDLE *pHandle)
    val FT_Open: MethodHandle = downcall(
        "FT_Open",
        FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_OpenEx(PVOID pvArg1, DWORD dwFlags, FT_HANDLE *pHandle)
    val FT_OpenEx: MethodHandle = downcall(
        "FT_OpenEx",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_Close(FT_HANDLE ftHandle)
    val FT_Close: MethodHandle = downcall(
        "FT_Close",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_Read(FT_HANDLE ftHandle, LPVOID lpBuffer, DWORD dwBytesToRead, LPDWORD lpdwBytesReturned)
    val FT_Read: MethodHandle = downcall(
        "FT_Read",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_Write(FT_HANDLE ftHandle, LPVOID lpBuffer, DWORD dwBytesToWrite, LPDWORD lpdwBytesWritten)
    val FT_Write: MethodHandle = downcall(
        "FT_Write",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_SetBaudRate(FT_HANDLE ftHandle, ULONG BaudRate)
    val FT_SetBaudRate: MethodHandle = downcall(
        "FT_SetBaudRate",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_SetDataCharacteristics(FT_HANDLE ftHandle, UCHAR uWordLength, UCHAR uStopBits, UCHAR uParity)
    val FT_SetDataCharacteristics: MethodHandle = downcall(
        "FT_SetDataCharacteristics",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE)
    )

    // FT_STATUS FT_SetFlowControl(FT_HANDLE ftHandle, USHORT usFlowControl, UCHAR uXon, UCHAR uXoff)
    val FT_SetFlowControl: MethodHandle = downcall(
        "FT_SetFlowControl",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_SHORT, JAVA_BYTE, JAVA_BYTE)
    )

    // FT_STATUS FT_SetTimeouts(FT_HANDLE ftHandle, ULONG ReadTimeout, ULONG WriteTimeout)
    val FT_SetTimeouts: MethodHandle = downcall(
        "FT_SetTimeouts",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT)
    )

    // FT_STATUS FT_SetDtr(FT_HANDLE ftHandle)
    val FT_SetDtr: MethodHandle = downcall(
        "FT_SetDtr",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_ClrDtr(FT_HANDLE ftHandle)
    val FT_ClrDtr: MethodHandle = downcall(
        "FT_ClrDtr",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_SetRts(FT_HANDLE ftHandle)
    val FT_SetRts: MethodHandle = downcall(
        "FT_SetRts",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_ClrRts(FT_HANDLE ftHandle)
    val FT_ClrRts: MethodHandle = downcall(
        "FT_ClrRts",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_GetQueueStatus(FT_HANDLE ftHandle, DWORD *dwRxBytes)
    val FT_GetQueueStatus: MethodHandle = downcall(
        "FT_GetQueueStatus",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetChars(FT_HANDLE ftHandle, UCHAR EventChar, UCHAR EventCharEnabled, UCHAR ErrorChar, UCHAR ErrorCharEnabled)
    val FT_SetChars: MethodHandle = downcall(
        "FT_SetChars",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE)
    )

    // FT_STATUS FT_SetLatencyTimer(FT_HANDLE ftHandle, UCHAR ucLatency)
    val FT_SetLatencyTimer: MethodHandle = downcall(
        "FT_SetLatencyTimer",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE)
    )

    // FT_STATUS FT_SetBitMode(FT_HANDLE ftHandle, UCHAR ucMask, UCHAR ucEnable)
    val FT_SetBitMode: MethodHandle = downcall(
        "FT_SetBitMode",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE)
    )

    // FT_STATUS FT_Purge(FT_HANDLE ftHandle, ULONG Mask)
    val FT_Purge: MethodHandle = downcall(
        "FT_Purge",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_ResetDevice(FT_HANDLE ftHandle)
    val FT_ResetDevice: MethodHandle = downcall(
        "FT_ResetDevice",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_ReadEE(FT_HANDLE ftHandle, DWORD dwWordOffset, LPWORD lpwValue)
    val FT_ReadEE: MethodHandle = downcall(
        "FT_ReadEE",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_WriteEE(FT_HANDLE ftHandle, DWORD dwWordOffset, WORD wValue)
    val FT_WriteEE: MethodHandle = downcall(
        "FT_WriteEE",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_SHORT)
    )

    // FT_STATUS FT_EraseEE(FT_HANDLE ftHandle)
    val FT_EraseEE: MethodHandle = downcall(
        "FT_EraseEE",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_GetComPortNumber(FT_HANDLE ftHandle, LPLONG lplComPortNumber)
    val FT_GetComPortNumber: MethodHandle = downcall(
        "FT_GetComPortNumber",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // ========================================================================
    // Data classes
    // ========================================================================

    data class DeviceInfo(
        val flags: Int,
        val type: Int,
        val id: Int,
        val locId: Int,
        val serialNumber: String,
        val description: String,
    )

    // ========================================================================
    // High-level helper functions
    // ========================================================================

    fun findDevices(): List<DeviceInfo> {
        Arena.ofConfined().use { arena ->
            val numDevsPtr = arena.allocate(JAVA_INT)

            var status = FT_CreateDeviceInfoList.invokeExact(numDevsPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_CreateDeviceInfoList failed: ${ftStatusToString(status)}")
            }

            val numDevs = numDevsPtr.get(JAVA_INT, 0)
            if (numDevs == 0) return emptyList()

            val nodeSize = FT_DEVICE_LIST_INFO_NODE.byteSize()
            val infoArray = arena.allocate(FT_DEVICE_LIST_INFO_NODE, numDevs.toLong())

            status = FT_GetDeviceInfoList.invokeExact(infoArray, numDevsPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_GetDeviceInfoList failed: ${ftStatusToString(status)}")
            }

            return (0 until numDevs).map { i ->
                val node = infoArray.asSlice(i.toLong() * nodeSize, nodeSize)

                val flags = node.get(JAVA_INT, FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("Flags")))
                val type = node.get(JAVA_INT, FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("Type")))
                val id = node.get(JAVA_INT, FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("ID")))
                val locId = node.get(JAVA_INT, FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("LocId")))

                val serialOffset = FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("SerialNumber"))
                val serialBytes = ByteArray(16) { node.get(JAVA_BYTE, serialOffset + it) }
                val serialNumber = String(serialBytes, 0, serialBytes.indexOf(0).let { if (it < 0) 16 else it })

                val descOffset = FT_DEVICE_LIST_INFO_NODE.byteOffset(PathElement.groupElement("Description"))
                val descBytes = ByteArray(64) { node.get(JAVA_BYTE, descOffset + it) }
                val description = String(descBytes, 0, descBytes.indexOf(0).let { if (it < 0) 64 else it })

                DeviceInfo(
                    flags = flags,
                    type = type,
                    id = id,
                    locId = locId,
                    serialNumber = serialNumber,
                    description = description,
                )
            }
        }
    }

    fun getDeviceCount(): Int {
        Arena.ofConfined().use { arena ->
            val numDevsPtr = arena.allocate(JAVA_INT)
            val status = FT_CreateDeviceInfoList.invokeExact(numDevsPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_CreateDeviceInfoList failed: ${ftStatusToString(status)}")
            }
            return numDevsPtr.get(JAVA_INT, 0)
        }
    }

    fun openByIndex(deviceIndex: Int): MemorySegment {
        Arena.ofConfined().use { arena ->
            val handlePtr = arena.allocate(ADDRESS)
            val status = FT_Open.invokeExact(deviceIndex, handlePtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_Open failed: ${ftStatusToString(status)}")
            }
            return handlePtr.get(ADDRESS, 0).reinterpret(0, Arena.global(), null)
        }
    }

    fun close(handle: MemorySegment) {
        val status = FT_Close.invokeExact(handle) as Int
        if (status != FT_OK) {
            throw RuntimeException("FT_Close failed: ${ftStatusToString(status)}")
        }
    }

    fun getComPortNumber(handle: MemorySegment): Int {
        Arena.ofConfined().use { arena ->
            val portPtr = arena.allocate(JAVA_INT)
            val status = FT_GetComPortNumber.invokeExact(handle, portPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_GetComPortNumber failed: ${ftStatusToString(status)}")
            }
            return portPtr.get(JAVA_INT, 0)
        }
    }

    private fun checkStatus(status: Int, functionName: String) {
        if (status != FT_OK) {
            throw RuntimeException("$functionName failed: ${ftStatusToString(status)}")
        }
    }

    fun setDataCharacteristics(handle: MemorySegment, wordLength: Byte, stopBits: Byte, parity: Byte) {
        val status = FT_SetDataCharacteristics.invokeExact(handle, wordLength, stopBits, parity) as Int
        checkStatus(status, "FT_SetDataCharacteristics")
    }

    fun setFlowControl(handle: MemorySegment, flowControl: Short, xon: Byte, xoff: Byte) {
        val status = FT_SetFlowControl.invokeExact(handle, flowControl, xon, xoff) as Int
        checkStatus(status, "FT_SetFlowControl")
    }

    fun setTimeouts(handle: MemorySegment, readTimeout: Int, writeTimeout: Int) {
        val status = FT_SetTimeouts.invokeExact(handle, readTimeout, writeTimeout) as Int
        checkStatus(status, "FT_SetTimeouts")
    }

    fun resetDevice(handle: MemorySegment) {
        val status = FT_ResetDevice.invokeExact(handle) as Int
        checkStatus(status, "FT_ResetDevice")
    }

    fun setChars(handle: MemorySegment, eventChar: Byte, eventCharEnabled: Byte, errorChar: Byte, errorCharEnabled: Byte) {
        val status = FT_SetChars.invokeExact(handle, eventChar, eventCharEnabled, errorChar, errorCharEnabled) as Int
        checkStatus(status, "FT_SetChars")
    }

    fun setLatencyTimer(handle: MemorySegment, latency: Byte) {
        val status = FT_SetLatencyTimer.invokeExact(handle, latency) as Int
        checkStatus(status, "FT_SetLatencyTimer")
    }

    fun setBitMode(handle: MemorySegment, mask: Byte, mode: Byte) {
        val status = FT_SetBitMode.invokeExact(handle, mask, mode) as Int
        checkStatus(status, "FT_SetBitMode")
    }

    fun purge(handle: MemorySegment, mask: Int) {
        val status = FT_Purge.invokeExact(handle, mask) as Int
        checkStatus(status, "FT_Purge")
    }

    fun setBaudRate(handle: MemorySegment, baudRate: Int) {
        val status = FT_SetBaudRate.invokeExact(handle, baudRate) as Int
        checkStatus(status, "FT_SetBaudRate")
    }

    fun setDtr(handle: MemorySegment, enabled: Boolean) {
        val status = if (enabled) {
            FT_SetDtr.invokeExact(handle) as Int
        } else {
            FT_ClrDtr.invokeExact(handle) as Int
        }
        checkStatus(status, if (enabled) "FT_SetDtr" else "FT_ClrDtr")
    }

    fun setRts(handle: MemorySegment, enabled: Boolean) {
        val status = if (enabled) {
            FT_SetRts.invokeExact(handle) as Int
        } else {
            FT_ClrRts.invokeExact(handle) as Int
        }
        checkStatus(status, if (enabled) "FT_SetRts" else "FT_ClrRts")
    }

    fun getQueueStatus(handle: MemorySegment): Int {
        Arena.ofConfined().use { arena ->
            val rxBytesPtr = arena.allocate(JAVA_INT)
            val status = FT_GetQueueStatus.invokeExact(handle, rxBytesPtr) as Int
            checkStatus(status, "FT_GetQueueStatus")
            return rxBytesPtr.get(JAVA_INT, 0)
        }
    }

    fun write(handle: MemorySegment, data: ByteArray): Int {
        Arena.ofConfined().use { arena ->
            val buffer = arena.allocate(data.size.toLong())
            MemorySegment.copy(data, 0, buffer, JAVA_BYTE, 0, data.size)
            val bytesWrittenPtr = arena.allocate(JAVA_INT)
            val status = FT_Write.invokeExact(handle, buffer, data.size, bytesWrittenPtr) as Int
            checkStatus(status, "FT_Write")
            return bytesWrittenPtr.get(JAVA_INT, 0)
        }
    }

    fun read(handle: MemorySegment, buffer: ByteArray, bytesToRead: Int = buffer.size): Int {
        Arena.ofConfined().use { arena ->
            val nativeBuf = arena.allocate(bytesToRead.toLong())
            val bytesReadPtr = arena.allocate(JAVA_INT)
            val status = FT_Read.invokeExact(handle, nativeBuf, bytesToRead, bytesReadPtr) as Int
            checkStatus(status, "FT_Read")
            val bytesRead = bytesReadPtr.get(JAVA_INT, 0)
            if (bytesRead > 0) {
                MemorySegment.copy(nativeBuf, JAVA_BYTE, 0, buffer, 0, bytesRead)
            }
            return bytesRead
        }
    }

    fun readEE(handle: MemorySegment, wordOffset: Int): Short {
        Arena.ofConfined().use { arena ->
            val valuePtr = arena.allocate(JAVA_SHORT)
            val status = FT_ReadEE.invokeExact(handle, wordOffset, valuePtr) as Int
            checkStatus(status, "FT_ReadEE")
            return valuePtr.get(JAVA_SHORT, 0)
        }
    }

    fun writeEE(handle: MemorySegment, wordOffset: Int, value: Short) {
        val status = FT_WriteEE.invokeExact(handle, wordOffset, value) as Int
        checkStatus(status, "FT_WriteEE")
    }

    fun eraseEE(handle: MemorySegment) {
        val status = FT_EraseEE.invokeExact(handle) as Int
        checkStatus(status, "FT_EraseEE")
    }
}
