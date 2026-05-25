package com.alchitry.hardware.usb.ftdi

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement
import java.lang.foreign.ValueLayout.*
import java.lang.invoke.MethodHandle

/**
 * Project Panama (FFM) bindings for the FTDI D3xx library.
 *
 * Provides downcall handles for all exported functions in ftd3xx.h,
 * along with Kotlin-friendly constants, enum values, and struct layouts.
 */
object D3xx {
    private val linker = Linker.nativeLinker()
    private val lookup = D3xxNativeLoader.symbolLookup

    private fun downcall(name: String, descriptor: FunctionDescriptor): MethodHandle {
        val addr = lookup.find(name).orElseThrow { UnsatisfiedLinkError("Symbol not found: $name") }
        return linker.downcallHandle(addr, descriptor)
    }

    /** Returns null if the symbol is not exported by this platform's library. */
    private fun optionalDowncall(name: String, descriptor: FunctionDescriptor): MethodHandle? {
        val addr = lookup.find(name)
        if (addr.isEmpty) return null
        return linker.downcallHandle(addr.get(), descriptor)
    }

    // ========================================================================
    // Constants
    // ========================================================================

    // Create flags
    const val FT_OPEN_BY_SERIAL_NUMBER = 0x00000001
    const val FT_OPEN_BY_DESCRIPTION = 0x00000002
    const val FT_OPEN_BY_LOCATION = 0x00000004
    const val FT_OPEN_BY_GUID = 0x00000008
    const val FT_OPEN_BY_INDEX = 0x00000010

    // ListDevices flags
    const val FT_LIST_ALL = 0x20000000
    const val FT_LIST_BY_INDEX = 0x40000000
    const val FT_LIST_NUMBER_ONLY = 0x80000000.toInt()

    // GPIO
    const val FT_GPIO_DIRECTION_IN = 0
    const val FT_GPIO_DIRECTION_OUT = 1
    const val FT_GPIO_VALUE_LOW = 0
    const val FT_GPIO_VALUE_HIGH = 1
    const val FT_GPIO_0 = 0
    const val FT_GPIO_1 = 1

    // ========================================================================
    // FT_STATUS enum values
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
    const val FT_MEM_INSUFFICIENT_ERROR = 11
    const val FT_OVERFLOW_ERROR = 12
    const val FT_INVALID_ARGS = 16
    const val FT_NOT_SUPPORTED = 17
    const val FT_NO_MORE_ITEMS = 18
    const val FT_TIMEOUT = 19
    const val FT_OPERATION_ABORTED = 20
    const val FT_RESERVED_PIPE = 21
    const val FT_INVALID_CONTROL_REQUEST_DIRECTION = 22
    const val FT_INVALID_CONTROL_REQUEST_TYPE = 23
    const val FT_IO_PENDING = 24
    const val FT_IO_INCOMPLETE = 25
    const val FT_HANDLE_EOF = 26
    const val FT_BUSY = 27
    const val FT_NO_SYSTEM_RESOURCES = 28
    const val FT_DEVICE_LIST_NOT_READY = 29
    const val FT_DEVICE_NOT_CONNECTED = 30
    const val FT_INCORRECT_DEVICE_PATH = 31
    const val FT_OTHER_ERROR = 32

    fun ftStatusToString(status: Int): String = when (status) {
        FT_OK -> "FT_OK"
        FT_INVALID_HANDLE -> "FT_INVALID_HANDLE"
        FT_DEVICE_NOT_FOUND -> "FT_DEVICE_NOT_FOUND"
        FT_DEVICE_NOT_OPENED -> "FT_DEVICE_NOT_OPENED"
        FT_IO_ERROR -> "FT_IO_ERROR"
        FT_INSUFFICIENT_RESOURCES -> "FT_INSUFFICIENT_RESOURCES"
        FT_INVALID_PARAMETER -> "FT_INVALID_PARAMETER"
        FT_TIMEOUT -> "FT_TIMEOUT"
        FT_OTHER_ERROR -> "FT_OTHER_ERROR"
        else -> "FT_STATUS($status)"
    }

    // ========================================================================
    // FT_DEVICE enum values
    // ========================================================================
    const val FT_DEVICE_UNKNOWN = 3
    const val FT_DEVICE_600 = 600
    const val FT_DEVICE_601 = 601
    const val FT_DEVICE_602 = 602
    const val FT_DEVICE_603 = 603

    // ========================================================================
    // FT_FLAGS enum values
    // ========================================================================
    const val FT_FLAGS_OPENED = 1
    const val FT_FLAGS_HISPEED = 2
    const val FT_FLAGS_SUPERSPEED = 4

    // ========================================================================
    // CONFIGURATION enums
    // ========================================================================
    const val CONFIGURATION_FIFO_CLK_100 = 0
    const val CONFIGURATION_FIFO_CLK_66 = 1
    const val CONFIGURATION_FIFO_CLK_50 = 2
    const val CONFIGURATION_FIFO_CLK_40 = 3

    const val CONFIGURATION_FIFO_MODE_245 = 0
    const val CONFIGURATION_FIFO_MODE_600 = 1

    const val CONFIGURATION_CHANNEL_CONFIG_4 = 0
    const val CONFIGURATION_CHANNEL_CONFIG_2 = 1
    const val CONFIGURATION_CHANNEL_CONFIG_1 = 2
    const val CONFIGURATION_CHANNEL_CONFIG_1_OUTPIPE = 3
    const val CONFIGURATION_CHANNEL_CONFIG_1_INPIPE = 4

    // ========================================================================
    // Struct Layouts
    // ========================================================================

    val FT_DEVICE_LIST_INFO_NODE: StructLayout = MemoryLayout.structLayout(
        JAVA_INT.withName("Flags"),
        JAVA_INT.withName("Type"),
        JAVA_INT.withName("ID"),
        JAVA_INT.withName("LocId"),
        MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("SerialNumber"),
        MemoryLayout.sequenceLayout(32, JAVA_BYTE).withName("Description"),
        ADDRESS.withName("ftHandle"),
    ).withName("FT_DEVICE_LIST_INFO_NODE")

    val FT_DEVICE_DESCRIPTOR: StructLayout = MemoryLayout.structLayout(
        JAVA_BYTE.withName("bLength"),
        JAVA_BYTE.withName("bDescriptorType"),
        JAVA_SHORT.withName("bcdUSB"),
        JAVA_BYTE.withName("bDeviceClass"),
        JAVA_BYTE.withName("bDeviceSubClass"),
        JAVA_BYTE.withName("bDeviceProtocol"),
        JAVA_BYTE.withName("bMaxPacketSize0"),
        JAVA_SHORT.withName("idVendor"),
        JAVA_SHORT.withName("idProduct"),
        JAVA_SHORT.withName("bcdDevice"),
        JAVA_BYTE.withName("iManufacturer"),
        JAVA_BYTE.withName("iProduct"),
        JAVA_BYTE.withName("iSerialNumber"),
        JAVA_BYTE.withName("bNumConfigurations"),
    ).withName("FT_DEVICE_DESCRIPTOR")

    val FT_CONFIGURATION_DESCRIPTOR: StructLayout = MemoryLayout.structLayout(
        JAVA_BYTE.withName("bLength"),
        JAVA_BYTE.withName("bDescriptorType"),
        JAVA_SHORT.withName("wTotalLength"),
        JAVA_BYTE.withName("bNumInterfaces"),
        JAVA_BYTE.withName("bConfigurationValue"),
        JAVA_BYTE.withName("iConfiguration"),
        JAVA_BYTE.withName("bmAttributes"),
        JAVA_BYTE.withName("MaxPower"),
        MemoryLayout.paddingLayout(3), // align to natural size
    ).withName("FT_CONFIGURATION_DESCRIPTOR")

    val FT_INTERFACE_DESCRIPTOR: StructLayout = MemoryLayout.structLayout(
        JAVA_BYTE.withName("bLength"),
        JAVA_BYTE.withName("bDescriptorType"),
        JAVA_BYTE.withName("bInterfaceNumber"),
        JAVA_BYTE.withName("bAlternateSetting"),
        JAVA_BYTE.withName("bNumEndpoints"),
        JAVA_BYTE.withName("bInterfaceClass"),
        JAVA_BYTE.withName("bInterfaceSubClass"),
        JAVA_BYTE.withName("bInterfaceProtocol"),
        JAVA_BYTE.withName("iInterface"),
    ).withName("FT_INTERFACE_DESCRIPTOR")

    val FT_PIPE_INFORMATION: StructLayout = MemoryLayout.structLayout(
        JAVA_INT.withName("PipeType"),
        JAVA_BYTE.withName("PipeId"),
        MemoryLayout.paddingLayout(1),
        JAVA_SHORT.withName("MaximumPacketSize"),
        JAVA_BYTE.withName("Interval"),
        MemoryLayout.paddingLayout(3),
    ).withName("FT_PIPE_INFORMATION")

    val FT_SETUP_PACKET: StructLayout = MemoryLayout.structLayout(
        JAVA_BYTE.withName("RequestType"),
        JAVA_BYTE.withName("Request"),
        JAVA_SHORT.withName("Value"),
        JAVA_SHORT.withName("Index"),
        JAVA_SHORT.withName("Length"),
    ).withName("FT_SETUP_PACKET")

    val FT_60XCONFIGURATION: StructLayout = MemoryLayout.structLayout(
        JAVA_SHORT.withName("VendorID"),
        JAVA_SHORT.withName("ProductID"),
        MemoryLayout.sequenceLayout(128, JAVA_BYTE).withName("StringDescriptors"),
        JAVA_BYTE.withName("bInterval"),
        JAVA_BYTE.withName("PowerAttributes"),
        JAVA_SHORT.withName("PowerConsumption"),
        JAVA_BYTE.withName("Reserved2"),
        JAVA_BYTE.withName("FIFOClock"),
        JAVA_BYTE.withName("FIFOMode"),
        JAVA_BYTE.withName("ChannelConfig"),
        JAVA_SHORT.withName("OptionalFeatureSupport"),
        JAVA_BYTE.withName("BatteryChargingGPIOConfig"),
        JAVA_BYTE.withName("FlashEEPROMDetection"),
        JAVA_INT.withName("MSIO_Control"),
        JAVA_INT.withName("GPIO_Control"),
    ).withName("FT_60XCONFIGURATION")

    // ========================================================================
    // Helper to get struct field VarHandle
    // ========================================================================

    fun <T : MemoryLayout> T.varHandle(name: String): java.lang.invoke.VarHandle =
        this.varHandle(PathElement.groupElement(name))

    // ========================================================================
    // Function Downcall Handles
    // ========================================================================

    // FT_STATUS FT_Create(PVOID pvArg, DWORD dwFlags, FT_HANDLE *pftHandle)
    val FT_Create: MethodHandle = downcall(
        "FT_Create",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_Close(FT_HANDLE ftHandle)
    val FT_Close: MethodHandle = downcall(
        "FT_Close",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_GetVIDPID(FT_HANDLE, PUSHORT puwVID, PUSHORT puwPID)
    val FT_GetVIDPID: MethodHandle = downcall(
        "FT_GetVIDPID",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_WritePipe(FT_HANDLE, UCHAR ucPipeID, PUCHAR pucBuffer, ULONG ulBufferLength, PULONG pulBytesTransferred, DWORD dwTimeoutInMs)
    val FT_WritePipe: MethodHandle = downcall(
        "FT_WritePipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_ReadPipe(FT_HANDLE, UCHAR ucPipeID, PUCHAR pucBuffer, ULONG ulBufferLength, PULONG pulBytesTransferred, DWORD dwTimeoutInMs)
    val FT_ReadPipe: MethodHandle = downcall(
        "FT_ReadPipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_WritePipeEx(FT_HANDLE, UCHAR ucFifoID, PUCHAR, ULONG, PULONG, DWORD)
    val FT_WritePipeEx: MethodHandle = downcall(
        "FT_WritePipeEx",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_ReadPipeEx(FT_HANDLE, UCHAR, PUCHAR, ULONG, PULONG, DWORD)
    val FT_ReadPipeEx: MethodHandle = downcall(
        "FT_ReadPipeEx",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_GetOverlappedResult(FT_HANDLE, LPOVERLAPPED, PULONG, BOOL)
    val FT_GetOverlappedResult: MethodHandle = downcall(
        "FT_GetOverlappedResult",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_InitializeOverlapped(FT_HANDLE, LPOVERLAPPED)
    val FT_InitializeOverlapped: MethodHandle = downcall(
        "FT_InitializeOverlapped",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_ReleaseOverlapped(FT_HANDLE, LPOVERLAPPED)
    val FT_ReleaseOverlapped: MethodHandle = downcall(
        "FT_ReleaseOverlapped",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetStreamPipe(FT_HANDLE, BOOL, BOOL, UCHAR, ULONG)
    val FT_SetStreamPipe: MethodHandle = downcall(
        "FT_SetStreamPipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_BYTE, JAVA_INT)
    )

    // FT_STATUS FT_ClearStreamPipe(FT_HANDLE, BOOL, BOOL, UCHAR)
    val FT_ClearStreamPipe: MethodHandle = downcall(
        "FT_ClearStreamPipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_BYTE)
    )

    // FT_STATUS FT_FlushPipe(FT_HANDLE, UCHAR)
    val FT_FlushPipe: MethodHandle = downcall(
        "FT_FlushPipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE)
    )

    // FT_STATUS FT_AbortPipe(FT_HANDLE, UCHAR)
    val FT_AbortPipe: MethodHandle = downcall(
        "FT_AbortPipe",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE)
    )

    // FT_STATUS FT_GetDeviceDescriptor(FT_HANDLE, PFT_DEVICE_DESCRIPTOR)
    val FT_GetDeviceDescriptor: MethodHandle = downcall(
        "FT_GetDeviceDescriptor",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetConfigurationDescriptor(FT_HANDLE, PFT_CONFIGURATION_DESCRIPTOR)
    val FT_GetConfigurationDescriptor: MethodHandle = downcall(
        "FT_GetConfigurationDescriptor",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetInterfaceDescriptor(FT_HANDLE, UCHAR, PFT_INTERFACE_DESCRIPTOR)
    val FT_GetInterfaceDescriptor: MethodHandle = downcall(
        "FT_GetInterfaceDescriptor",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS)
    )

    // FT_STATUS FT_GetPipeInformation(FT_HANDLE, UCHAR, UCHAR, PFT_PIPE_INFORMATION)
    val FT_GetPipeInformation: MethodHandle = downcall(
        "FT_GetPipeInformation",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE, ADDRESS)
    )

    // FT_STATUS FT_GetStringDescriptor(FT_HANDLE, UCHAR, PFT_STRING_DESCRIPTOR)
    val FT_GetStringDescriptor: MethodHandle = downcall(
        "FT_GetStringDescriptor",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS)
    )

    // FT_STATUS FT_GetDescriptor(FT_HANDLE, UCHAR, UCHAR, PUCHAR, ULONG, PULONG)
    val FT_GetDescriptor: MethodHandle = downcall(
        "FT_GetDescriptor",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_ControlTransfer(FT_HANDLE, FT_SETUP_PACKET, PUCHAR, ULONG, PULONG)
    val FT_ControlTransfer: MethodHandle = downcall(
        "FT_ControlTransfer",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, FT_SETUP_PACKET, ADDRESS, JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_SetNotificationCallback(FT_HANDLE, FT_NOTIFICATION_CALLBACK, PVOID)
    val FT_SetNotificationCallback: MethodHandle = downcall(
        "FT_SetNotificationCallback",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS)
    )

    // VOID FT_ClearNotificationCallback(FT_HANDLE)
    val FT_ClearNotificationCallback: MethodHandle = downcall(
        "FT_ClearNotificationCallback",
        FunctionDescriptor.ofVoid(ADDRESS)
    )

    // FT_STATUS FT_GetChipConfiguration(FT_HANDLE, PVOID)
    val FT_GetChipConfiguration: MethodHandle = downcall(
        "FT_GetChipConfiguration",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetChipConfiguration(FT_HANDLE, PVOID)
    val FT_SetChipConfiguration: MethodHandle = downcall(
        "FT_SetChipConfiguration",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetFirmwareVersion(FT_HANDLE, PULONG)
    val FT_GetFirmwareVersion: MethodHandle = downcall(
        "FT_GetFirmwareVersion",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_ResetDevicePort(FT_HANDLE)
    val FT_ResetDevicePort: MethodHandle = downcall(
        "FT_ResetDevicePort",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_CycleDevicePort(FT_HANDLE)
    val FT_CycleDevicePort: MethodHandle = downcall(
        "FT_CycleDevicePort",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_CreateDeviceInfoList(LPDWORD)
    val FT_CreateDeviceInfoList: MethodHandle = downcall(
        "FT_CreateDeviceInfoList",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_GetDeviceInfoList(FT_DEVICE_LIST_INFO_NODE*, LPDWORD)
    val FT_GetDeviceInfoList: MethodHandle = downcall(
        "FT_GetDeviceInfoList",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetDeviceInfoDetail(DWORD, LPDWORD, LPDWORD, LPDWORD, LPDWORD, LPVOID, LPVOID, FT_HANDLE*)
    val FT_GetDeviceInfoDetail: MethodHandle = downcall(
        "FT_GetDeviceInfoDetail",
        FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_ListDevices(PVOID, PVOID, DWORD)
    val FT_ListDevices: MethodHandle = downcall(
        "FT_ListDevices",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_IsDevicePath(FT_HANDLE, LPCSTR)
    val FT_IsDevicePath: MethodHandle = downcall(
        "FT_IsDevicePath",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetDriverVersion(FT_HANDLE, LPDWORD)
    val FT_GetDriverVersion: MethodHandle = downcall(
        "FT_GetDriverVersion",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_GetLibraryVersion(LPDWORD)
    val FT_GetLibraryVersion: MethodHandle = downcall(
        "FT_GetLibraryVersion",
        FunctionDescriptor.of(JAVA_INT, ADDRESS)
    )

    // FT_STATUS FT_SetPipeTimeout(FT_HANDLE, UCHAR, DWORD)
    val FT_SetPipeTimeout: MethodHandle = downcall(
        "FT_SetPipeTimeout",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_INT)
    )

    // FT_STATUS FT_EnableGPIO(FT_HANDLE, DWORD, DWORD)
    val FT_EnableGPIO: MethodHandle = downcall(
        "FT_EnableGPIO",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT)
    )

    // FT_STATUS FT_WriteGPIO(FT_HANDLE, DWORD, DWORD)
    val FT_WriteGPIO: MethodHandle = downcall(
        "FT_WriteGPIO",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT)
    )

    // FT_STATUS FT_ReadGPIO(FT_HANDLE, DWORD*)
    val FT_ReadGPIO: MethodHandle = downcall(
        "FT_ReadGPIO",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetGPIOPull(FT_HANDLE, DWORD, DWORD)
    val FT_SetGPIOPull: MethodHandle = downcall(
        "FT_SetGPIOPull",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT)
    )

    // ========================================================================
    // Platform-specific functions (Linux/macOS only)
    // ========================================================================

    // FT_STATUS FT_WritePipeAsync(FT_HANDLE, UCHAR, PUCHAR, ULONG, PULONG, LPOVERLAPPED)
    val FT_WritePipeAsync: MethodHandle? = optionalDowncall(
        "FT_WritePipeAsync",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_ReadPipeAsync(FT_HANDLE, UCHAR, PUCHAR, ULONG, PULONG, LPOVERLAPPED)
    val FT_ReadPipeAsync: MethodHandle? = optionalDowncall(
        "FT_ReadPipeAsync",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetGPIO(FT_HANDLE, UCHAR ucDirection, UCHAR ucValue)
    val FT_SetGPIO: MethodHandle? = optionalDowncall(
        "FT_SetGPIO",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, JAVA_BYTE)
    )

    // FT_STATUS FT_GetGPIO(FT_HANDLE, UCHAR, FT_NOTIFICATION_CALLBACK, PVOID, USHORT)
    val FT_GetGPIO: MethodHandle? = optionalDowncall(
        "FT_GetGPIO",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, ADDRESS, JAVA_SHORT)
    )

    // FT_STATUS FT_SetTransferParams(FT_TRANSFER_CONF*, DWORD dwFifoID)
    val FT_SetTransferParams: MethodHandle? = optionalDowncall(
        "FT_SetTransferParams",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_GetTransferParams(FT_TRANSFER_CONF*, DWORD dwFifoID)
    val FT_GetTransferParams: MethodHandle? = optionalDowncall(
        "FT_GetTransferParams",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_GetReadQueueStatus(FT_HANDLE, UCHAR ucFifoID, LPDWORD)
    val FT_GetReadQueueStatus: MethodHandle? = optionalDowncall(
        "FT_GetReadQueueStatus",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS)
    )

    // FT_STATUS FT_GetWriteQueueStatus(FT_HANDLE, UCHAR ucFifoID, LPDWORD)
    val FT_GetWriteQueueStatus: MethodHandle? = optionalDowncall(
        "FT_GetWriteQueueStatus",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS)
    )

    // FT_STATUS FT_GetUnsentBuffer(FT_HANDLE, UCHAR ucFifoID, BYTE*, LPDWORD)
    val FT_GetUnsentBuffer: MethodHandle? = optionalDowncall(
        "FT_GetUnsentBuffer",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, ADDRESS)
    )

    // ========================================================================
    // Platform-specific functions (Windows only)
    // ========================================================================

    // FT_STATUS FT_GetSuspendTimeout(FT_HANDLE, PULONG)
    val FT_GetSuspendTimeout: MethodHandle? = optionalDowncall(
        "FT_GetSuspendTimeout",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_SetSuspendTimeout(FT_HANDLE, ULONG)
    val FT_SetSuspendTimeout: MethodHandle? = optionalDowncall(
        "FT_SetSuspendTimeout",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_STATUS FT_GetPipeTimeout(FT_HANDLE, UCHAR, LPDWORD)
    val FT_GetPipeTimeout: MethodHandle? = optionalDowncall(
        "FT_GetPipeTimeout",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS)
    )

    // ========================================================================
    // Notification callback descriptor (for creating upcall stubs)
    // ========================================================================

    /** FunctionDescriptor for FT_NOTIFICATION_CALLBACK: void(PVOID, int, PVOID) */
    val NOTIFICATION_CALLBACK_DESCRIPTOR: FunctionDescriptor =
        FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS)
}
