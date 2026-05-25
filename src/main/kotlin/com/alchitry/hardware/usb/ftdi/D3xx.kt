package com.alchitry.hardware.usb.ftdi

import com.alchitry.hardware.Env
import com.alchitry.hardware.Log
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

    // OptionalFeatureSupport flags
    const val CONFIGURATION_OPTIONAL_FEATURE_DISABLEALL = 0
    const val CONFIGURATION_OPTIONAL_FEATURE_DISABLECANCELSESSIONUNDERRUN = (0x1 shl 1)
    const val CONFIGURATION_OPTIONAL_FEATURE_ENABLENOTIFICATIONMESSAGE_INCHALL = (0xF shl 2)
    const val CONFIGURATION_OPTIONAL_FEATURE_DISABLEUNDERRUN_INCHALL = (0xF shl 6)

    // Pipe direction
    const val FT_PIPE_DIR_IN = 0
    const val FT_PIPE_DIR_OUT = 1
    const val FT_PIPE_DIR_COUNT = 2

    // ========================================================================
    // Struct Layouts
    // ========================================================================

    // Linux/macOS only struct for pipe transfer configuration
    val FT_PIPE_TRANSFER_CONF: StructLayout = MemoryLayout.structLayout(
        JAVA_INT.withName("fPipeNotUsed"),           // BOOL
        JAVA_INT.withName("fNonThreadSafeTransfer"),  // BOOL
        JAVA_BYTE.withName("bURBCount"),              // BYTE
        MemoryLayout.paddingLayout(1),
        JAVA_SHORT.withName("wURBBufferCount"),        // WORD
        JAVA_INT.withName("dwURBBufferSize"),          // DWORD
        JAVA_INT.withName("dwStreamingSize"),          // DWORD
    ).withName("FT_PIPE_TRANSFER_CONF")

    // Linux/macOS only struct for transfer configuration
    val FT_TRANSFER_CONF: StructLayout = MemoryLayout.structLayout(
        JAVA_SHORT.withName("wStructSize"),            // WORD
        MemoryLayout.paddingLayout(2),
        MemoryLayout.sequenceLayout(FT_PIPE_DIR_COUNT.toLong(), FT_PIPE_TRANSFER_CONF).withName("pipe"),
        JAVA_INT.withName("fStopReadingOnURBUnderrun"), // BOOL
        JAVA_INT.withName("fBitBangMode"),              // BOOL
        JAVA_INT.withName("fKeepDeviceSideBufferAfterReopen"), // BOOL
    ).withName("FT_TRANSFER_CONF")

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

    // FT_WritePipe / FT_ReadPipe have different signatures on Windows vs Linux/macOS:
    //   Linux/macOS: FT_STATUS FT_WritePipe(FT_HANDLE, UCHAR ucPipeID, PUCHAR, ULONG, PULONG, DWORD dwTimeoutInMs)
    //   Windows:     FT_STATUS FT_WritePipe(FT_HANDLE, UCHAR ucPipeID, PUCHAR, ULONG, PULONG, LPOVERLAPPED)
    private val isWindows = Env.isWindows

    val FT_WritePipe: MethodHandle = downcall(
        "FT_WritePipe",
        if (isWindows)
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
        else
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    val FT_ReadPipe: MethodHandle = downcall(
        "FT_ReadPipe",
        if (isWindows)
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
        else
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    // FT_WritePipeEx / FT_ReadPipeEx also differ:
    //   Linux/macOS: last param is DWORD dwTimeoutInMs
    //   Windows:     last param is LPOVERLAPPED
    val FT_WritePipeEx: MethodHandle = downcall(
        "FT_WritePipeEx",
        if (isWindows)
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
        else
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
    )

    val FT_ReadPipeEx: MethodHandle = downcall(
        "FT_ReadPipeEx",
        if (isWindows)
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
        else
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

    // FT_STATUS FT_WritePipeAsync(FT_HANDLE, UCHAR ucFifoID, PUCHAR, ULONG, PULONG, LPOVERLAPPED)
    // Only exists on Linux/macOS. On Windows, use FT_WritePipe with an OVERLAPPED.
    val FT_WritePipeAsync: MethodHandle? = optionalDowncall(
        "FT_WritePipeAsync",
        FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_BYTE, ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
    )

    // FT_STATUS FT_ReadPipeAsync(FT_HANDLE, UCHAR ucFifoID, PUCHAR, ULONG, PULONG, LPOVERLAPPED)
    // Only exists on Linux/macOS. On Windows, use FT_ReadPipe with an OVERLAPPED.
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

    // ========================================================================
    // Data classes for device info results
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

    /**
     * Enumerates all connected D3xx devices and returns their info.
     *
     * Demonstrates proper Panama FFM usage:
     * - Allocating pointer-to-DWORD via [Arena.ofConfined]
     * - Reading back scalar values from [MemorySegment]
     * - Allocating arrays of structs and reading fields with offsets
     * - Extracting null-terminated C strings from byte arrays
     */
    fun findDevices(): List<DeviceInfo> {
        Arena.ofConfined().use { arena ->
            // Allocate a DWORD (4 bytes) to receive the device count.
            val numDevsPtr = arena.allocate(JAVA_INT)

            // FT_CreateDeviceInfoList writes the count into numDevsPtr.
            var status = FT_CreateDeviceInfoList.invokeExact(numDevsPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_CreateDeviceInfoList failed: ${ftStatusToString(status)}")
            }

            val numDevs = numDevsPtr.get(JAVA_INT, 0)
            if (numDevs == 0) return emptyList()

            // Allocate an array of FT_DEVICE_LIST_INFO_NODE structs.
            val nodeSize = FT_DEVICE_LIST_INFO_NODE.byteSize()
            val infoArray = arena.allocate(FT_DEVICE_LIST_INFO_NODE, numDevs.toLong())

            // FT_GetDeviceInfoList fills the array and updates the count.
            status = FT_GetDeviceInfoList.invokeExact(infoArray, numDevsPtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_GetDeviceInfoList failed: ${ftStatusToString(status)}")
            }

            // Parse each struct from the array.
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
                val descBytes = ByteArray(32) { node.get(JAVA_BYTE, descOffset + it) }
                val description = String(descBytes, 0, descBytes.indexOf(0).let { if (it < 0) 32 else it })

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

    /**
     * Represents an open connection to a D3xx device.
     * The [handle] is the native FT_HANDLE used for all subsequent D3xx calls.
     * Call [close] when finished to release the device.
     *
     * Provides high-level helper methods for reading and writing data,
     * including overlapped (async) I/O via [readPipeAsync] and [writePipeAsync].
     */
    class DeviceConnection(val handle: MemorySegment) : AutoCloseable {
        override fun close() {
            val status = FT_Close.invokeExact(handle) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_Close failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Sets default pipe timeouts, clears stream pipes, and flushes pipes.
         * Mirrors the Rust set_defaults() function.
         */
        fun setDefaults() {
            setPipeTimeout(0x02, 5000)
            setPipeTimeout(0x82.toByte(), 5000)
            clearStreamPipe(allWritePipes = true, allReadPipes = true, pipeId = 0)
            flushPipe(0x02)
            flushPipe(0x82.toByte())
        }

        /**
         * Sets the timeout for a specific pipe.
         */
        fun setPipeTimeout(pipeId: Byte, timeoutMs: Int) {
            val status = FT_SetPipeTimeout.invokeExact(handle, pipeId, timeoutMs) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_SetPipeTimeout failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Flushes the specified pipe.
         */
        fun flushPipe(pipeId: Byte) {
            val status = FT_FlushPipe.invokeExact(handle, pipeId) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_FlushPipe failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Aborts pending transfers on the specified pipe.
         */
        fun abortPipe(pipeId: Byte) {
            val status = FT_AbortPipe.invokeExact(handle, pipeId) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_AbortPipe failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Sets up streaming on a pipe.
         */
        fun setStreamPipe(allWritePipes: Boolean, allReadPipes: Boolean, pipeId: Byte, streamSize: Int) {
            val status = FT_SetStreamPipe.invokeExact(
                handle,
                if (allWritePipes) 1 else 0,
                if (allReadPipes) 1 else 0,
                pipeId,
                streamSize
            ) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_SetStreamPipe failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Clears streaming on a pipe.
         */
        fun clearStreamPipe(allWritePipes: Boolean, allReadPipes: Boolean, pipeId: Byte) {
            val status = FT_ClearStreamPipe.invokeExact(
                handle,
                if (allWritePipes) 1 else 0,
                if (allReadPipes) 1 else 0,
                pipeId
            ) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_ClearStreamPipe failed: ${ftStatusToString(status)}")
            }
        }

        /**
         * Synchronous read using FT_ReadPipeEx (Linux/macOS) or FT_ReadPipe (Windows).
         * Returns the bytes actually read.
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param buffer pre-allocated [MemorySegment] to read into.
         * @param length number of bytes to read.
         * @param timeoutMs timeout in milliseconds (Linux/macOS only, ignored on Windows).
         * @return number of bytes actually transferred.
         */
        fun readPipe(fifoId: Byte, buffer: MemorySegment, length: Int, timeoutMs: Int = 1000): Int {
            Arena.ofConfined().use { arena ->
                val bytesTransferred = arena.allocate(JAVA_INT)
                val status: Int
                if (isWindows) {
                    // On Windows, FT_ReadPipeEx takes LPOVERLAPPED — pass NULL for synchronous.
                    status = FT_ReadPipeEx.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, MemorySegment.NULL
                    ) as Int
                } else {
                    status = FT_ReadPipeEx.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, timeoutMs
                    ) as Int
                }
                if (status != FT_OK && status != FT_TIMEOUT) {
                    throw RuntimeException("FT_ReadPipeEx failed: ${ftStatusToString(status)}")
                }
                return bytesTransferred.get(JAVA_INT, 0)
            }
        }

        /**
         * Synchronous write using FT_WritePipeEx (Linux/macOS) or FT_WritePipe (Windows).
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param buffer [MemorySegment] containing data to write.
         * @param length number of bytes to write.
         * @param timeoutMs timeout in milliseconds (Linux/macOS only, ignored on Windows).
         * @return number of bytes actually transferred.
         */
        fun writePipe(fifoId: Byte, buffer: MemorySegment, length: Int, timeoutMs: Int = 1000): Int {
            Arena.ofConfined().use { arena ->
                val bytesTransferred = arena.allocate(JAVA_INT)
                val status: Int
                if (isWindows) {
                    status = FT_WritePipeEx.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, MemorySegment.NULL
                    ) as Int
                } else {
                    status = FT_WritePipeEx.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, timeoutMs
                    ) as Int
                }
                if (status != FT_OK) {
                    throw RuntimeException("FT_WritePipeEx failed: ${ftStatusToString(status)}")
                }
                return bytesTransferred.get(JAVA_INT, 0)
            }
        }

        /**
         * Represents an initialized overlapped I/O context.
         * Must be released with [close] (or via [use]) after the operation completes.
         */
        inner class OverlappedContext(val overlapped: MemorySegment, private val arena: Arena) : AutoCloseable {
            /**
             * Waits for the overlapped operation to complete and returns the number of bytes transferred.
             *
             * @param wait if true, blocks until the operation completes; if false, returns immediately
             *             with [FT_IO_INCOMPLETE] status if not yet done.
             */
            fun getResult(wait: Boolean = true): Int {
                Arena.ofConfined().use { resultArena ->
                    val bytesTransferred = resultArena.allocate(JAVA_INT)
                    val status = FT_GetOverlappedResult.invokeExact(
                        handle, overlapped, bytesTransferred, if (wait) 1 else 0
                    ) as Int
                    if (status != FT_OK && status != FT_IO_INCOMPLETE && status != FT_TIMEOUT) {
                        throw RuntimeException("FT_GetOverlappedResult failed: ${ftStatusToString(status)}")
                    }
                    return bytesTransferred.get(JAVA_INT, 0)
                }
            }

            override fun close() {
                FT_ReleaseOverlapped.invokeExact(handle, overlapped) as Int
                arena.close()
            }
        }

        /**
         * Initializes an overlapped I/O context for use with [readPipeAsync] or [writePipeAsync].
         * The returned [OverlappedContext] must be closed after the operation completes.
         */
        fun initializeOverlapped(): OverlappedContext {
            // Allocate enough space for the OVERLAPPED struct.
            // On Linux it's the Event struct (~24 bytes), on Windows it's OVERLAPPED (~32 bytes on x64).
            // We allocate generously and let FT_InitializeOverlapped fill it.
            val arena = Arena.ofConfined()
            val overlapped = arena.allocate(64) // generous allocation for any platform
            val status = FT_InitializeOverlapped.invokeExact(handle, overlapped) as Int
            if (status != FT_OK) {
                arena.close()
                throw RuntimeException("FT_InitializeOverlapped failed: ${ftStatusToString(status)}")
            }
            return OverlappedContext(overlapped, arena)
        }

        /**
         * Starts an asynchronous read operation using the overlapped interface.
         *
         * On Linux/macOS, uses [FT_ReadPipeAsync].
         * On Windows, uses [FT_ReadPipe] with an OVERLAPPED structure.
         *
         * The operation may return [FT_IO_PENDING] which is normal for async I/O.
         * Use [OverlappedContext.getResult] to wait for completion.
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param buffer pre-allocated [MemorySegment] to read into. Must remain valid until the operation completes.
         * @param length number of bytes to read.
         * @param overlappedCtx the overlapped context from [initializeOverlapped].
         * @return number of bytes transferred so far (may be 0 if pending).
         */
        fun readPipeAsync(
            fifoId: Byte,
            buffer: MemorySegment,
            length: Int,
            overlappedCtx: OverlappedContext
        ): Int {
            Arena.ofConfined().use { arena ->
                val bytesTransferred = arena.allocate(JAVA_INT)
                val status: Int
                if (isWindows) {
                    // Windows: FT_ReadPipe(handle, pipeId, buf, len, &transferred, overlapped)
                    status = FT_ReadPipe.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, overlappedCtx.overlapped
                    ) as Int
                } else {
                    // Linux/macOS: FT_ReadPipeAsync(handle, fifoId, buf, len, &transferred, overlapped)
                    val readAsync = FT_ReadPipeAsync
                        ?: throw UnsupportedOperationException("FT_ReadPipeAsync not available on this platform")
                    status = readAsync.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, overlappedCtx.overlapped
                    ) as Int
                }
                if (status != FT_OK && status != FT_IO_PENDING) {
                    throw RuntimeException("readPipeAsync failed: ${ftStatusToString(status)}")
                }
                return bytesTransferred.get(JAVA_INT, 0)
            }
        }

        /**
         * Starts an asynchronous write operation using the overlapped interface.
         *
         * On Linux/macOS, uses [FT_WritePipeAsync].
         * On Windows, uses [FT_WritePipe] with an OVERLAPPED structure.
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param buffer [MemorySegment] containing data to write. Must remain valid until the operation completes.
         * @param length number of bytes to write.
         * @param overlappedCtx the overlapped context from [initializeOverlapped].
         * @return number of bytes transferred so far (may be 0 if pending).
         */
        fun writePipeAsync(
            fifoId: Byte,
            buffer: MemorySegment,
            length: Int,
            overlappedCtx: OverlappedContext
        ): Int {
            Arena.ofConfined().use { arena ->
                val bytesTransferred = arena.allocate(JAVA_INT)
                val status: Int
                if (isWindows) {
                    // Windows: FT_WritePipe(handle, pipeId, buf, len, &transferred, overlapped)
                    status = FT_WritePipe.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, overlappedCtx.overlapped
                    ) as Int
                } else {
                    // Linux/macOS: FT_WritePipeAsync(handle, fifoId, buf, len, &transferred, overlapped)
                    val writeAsync = FT_WritePipeAsync
                        ?: throw UnsupportedOperationException("FT_WritePipeAsync not available on this platform")
                    status = writeAsync.invokeExact(
                        handle, fifoId, buffer, length, bytesTransferred, overlappedCtx.overlapped
                    ) as Int
                }
                if (status != FT_OK && status != FT_IO_PENDING) {
                    throw RuntimeException("writePipeAsync failed: ${ftStatusToString(status)}")
                }
                return bytesTransferred.get(JAVA_INT, 0)
            }
        }

        /**
         * Convenience: performs a synchronous read and returns the data as a [ByteArray].
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param count number of bytes to read.
         * @param timeoutMs timeout in milliseconds (Linux/macOS only).
         * @return the bytes read (may be fewer than [count] on timeout).
         */
        fun readBytes(fifoId: Byte, count: Int, timeoutMs: Int = 1000): ByteArray {
            Arena.ofConfined().use { arena ->
                val buffer = arena.allocate(count.toLong())
                val transferred = readPipe(fifoId, buffer, count, timeoutMs)
                return buffer.asSlice(0, transferred.toLong()).toArray(JAVA_BYTE)
            }
        }

        /**
         * Convenience: performs a synchronous write from a [ByteArray].
         *
         * @param fifoId FIFO channel ID (0-3).
         * @param data the bytes to write.
         * @param timeoutMs timeout in milliseconds (Linux/macOS only).
         * @return number of bytes actually written.
         */
        fun writeBytes(fifoId: Byte, data: ByteArray, timeoutMs: Int = 1000): Int {
            Arena.ofConfined().use { arena ->
                val buffer = arena.allocate(data.size.toLong())
                MemorySegment.copy(data, 0, buffer, JAVA_BYTE, 0, data.size)
                return writePipe(fifoId, buffer, data.size, timeoutMs)
            }
        }
    }

    /**
     * Checks the chip configuration and updates it if needed.
     * Sets FIFOClock=100MHz, FIFOMode=245, ChannelConfig=1InPipe,
     * enables DISABLEUNDERRUN_INCHALL and DISABLECANCELSESSIONUNDERRUN,
     * and disables ENABLENOTIFICATIONMESSAGE_INCHALL.
     *
     * Opens and closes the device by index internally.
     */
    fun checkChannelConfig(deviceIndex: Int) {
        Arena.ofConfined().use { arena ->
            val handlePtr = arena.allocate(ADDRESS)
            val idx = MemorySegment.ofAddress(deviceIndex.toLong())

            var status = FT_Create.invokeExact(idx, FT_OPEN_BY_INDEX, handlePtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_Create (by index) failed: ${ftStatusToString(status)}")
            }
            val handle = handlePtr.get(ADDRESS, 0)
            var closed = false

            try {
                val conf = arena.allocate(FT_60XCONFIGURATION)

                status = FT_GetChipConfiguration.invokeExact(handle, conf) as Int
                if (status != FT_OK) {
                    throw RuntimeException("FT_GetChipConfiguration failed: ${ftStatusToString(status)}")
                }

                val fifoClock = conf.get(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("FIFOClock")))
                val fifoMode = conf.get(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("FIFOMode")))
                val channelConfig = conf.get(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("ChannelConfig")))
                val optFeatureOffset = FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("OptionalFeatureSupport"))
                val optFeature = conf.get(JAVA_SHORT, optFeatureOffset)

                val newFifoClock = CONFIGURATION_FIFO_CLK_100.toByte()
                val newFifoMode = CONFIGURATION_FIFO_MODE_245.toByte()
                val newChannelConfig = CONFIGURATION_CHANNEL_CONFIG_1.toByte()
                var newOptFeature = optFeature.toInt()
                newOptFeature = newOptFeature or CONFIGURATION_OPTIONAL_FEATURE_DISABLEUNDERRUN_INCHALL
                newOptFeature = newOptFeature or CONFIGURATION_OPTIONAL_FEATURE_DISABLECANCELSESSIONUNDERRUN
                newOptFeature = newOptFeature and CONFIGURATION_OPTIONAL_FEATURE_ENABLENOTIFICATIONMESSAGE_INCHALL.inv()

                if (fifoClock != newFifoClock || fifoMode != newFifoMode ||
                    channelConfig != newChannelConfig || optFeature != newOptFeature.toShort()
                ) {
                    conf.set(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("FIFOClock")), newFifoClock)
                    conf.set(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("FIFOMode")), newFifoMode)
                    conf.set(JAVA_BYTE, FT_60XCONFIGURATION.byteOffset(PathElement.groupElement("ChannelConfig")), newChannelConfig)
                    conf.set(JAVA_SHORT, optFeatureOffset, newOptFeature.toShort())

                    status = FT_SetChipConfiguration.invokeExact(handle, conf) as Int
                    if (status != FT_OK) {
                        throw RuntimeException("FT_SetChipConfiguration failed: ${ftStatusToString(status)}")
                    }
                    Log.println("New chip configuration!")

                    // FT_SetChipConfiguration resets the device, causing it to re-enumerate on USB.
                    // Close the handle before the device resets.
                    FT_Close.invokeExact(handle) as Int
                    closed = true

                    // Wait for the device to re-enumerate.
                    Thread.sleep(1000)

                    // Re-create the device info list so the OS recognizes the device again.
                    val reEnumPtr = arena.allocate(JAVA_INT)
                    FT_CreateDeviceInfoList.invokeExact(reEnumPtr) as Int
                }
            } finally {
                if (!closed) {
                    FT_Close.invokeExact(handle) as Int
                }
            }
        }
    }

    /**
     * Connects to a device by its index, matching the Rust connect() flow:
     * 1. Checks and updates chip configuration if needed.
     * 2. Sets transfer params for all 4 pipes (Linux/macOS only).
     * 3. Opens the device by index.
     *
     * Returns a [DeviceConnection] that holds the native FT_HANDLE.
     * Use it with Kotlin's [use] for automatic cleanup:
     * ```
     * connectDevice(0).use { conn ->
     *     // use conn.handle with FT_WritePipe, FT_ReadPipe, etc.
     * }
     * ```
     */
    fun connectDevice(deviceIndex: Int): DeviceConnection {
        checkChannelConfig(deviceIndex)

        Arena.ofConfined().use { arena ->
            // Set transfer params for all 4 pipes (Linux/macOS only).
            FT_SetTransferParams?.let { setTransferParams ->
                val conf = arena.allocate(FT_TRANSFER_CONF)
                // Zero-initialized by default. Set wStructSize.
                conf.set(JAVA_SHORT, 0, FT_TRANSFER_CONF.byteSize().toShort())

                // Set fNonThreadSafeTransfer = true for both IN and OUT pipes.
                val pipeArrayOffset = FT_TRANSFER_CONF.byteOffset(PathElement.groupElement("pipe"))
                val pipeSize = FT_PIPE_TRANSFER_CONF.byteSize()
                val nonThreadSafeOffset = FT_PIPE_TRANSFER_CONF.byteOffset(PathElement.groupElement("fNonThreadSafeTransfer"))

                // pipe[FT_PIPE_DIR_IN].fNonThreadSafeTransfer = 1
                conf.set(JAVA_INT, pipeArrayOffset + (FT_PIPE_DIR_IN * pipeSize) + nonThreadSafeOffset, 1)
                // pipe[FT_PIPE_DIR_OUT].fNonThreadSafeTransfer = 1
                conf.set(JAVA_INT, pipeArrayOffset + (FT_PIPE_DIR_OUT * pipeSize) + nonThreadSafeOffset, 1)

                for (i in 0 until 4) {
                    val status = setTransferParams.invokeExact(conf, i) as Int
                    if (status != FT_OK) {
                        throw RuntimeException("FT_SetTransferParams failed for pipe $i: ${ftStatusToString(status)}")
                    }
                }
            }

            // Open device by index.
            val handlePtr = arena.allocate(ADDRESS)
            val idx = MemorySegment.ofAddress(deviceIndex.toLong())

            val status = FT_Create.invokeExact(idx, FT_OPEN_BY_INDEX, handlePtr) as Int
            if (status != FT_OK) {
                throw RuntimeException("FT_Create (by index) failed for index $deviceIndex: ${ftStatusToString(status)}")
            }

            val handle = handlePtr.get(ADDRESS, 0)
            val connection = DeviceConnection(handle)
            connection.setDefaults()
            return connection
        }
    }
}
