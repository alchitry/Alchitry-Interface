package com.alchitry.hardware

import com.alchitry.hardware.usb.UsbUtil
import com.alchitry.hardware.usb.ftdi.enums.PortInterfaceType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BoardSerializer : KSerializer<Board> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Board", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Board) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): Board {
        val name = decoder.decodeString()
        return Board.fromName(name) ?: error("Unknown board type: $name")
    }
}

@Serializable(with = BoardSerializer::class)
sealed class Board {
    companion object {
        fun fromName(name: String): Board? =
            Board::class.allSealedObjects()
                .firstOrNull { it.name.equals(name, ignoreCase = true) || it.alias.equals(name, ignoreCase = true) }

        val All: List<Board> by lazy {
            listOf(
                AlchitryPtV2,
                AlchitryAuV2,
                AlchitryCuV2,
                AlchitryAu,
                AlchitryAuPlus,
                AlchitryCu
            )
        }
    }

    abstract val name: String
    abstract val alias: String
    abstract val fpgaName: String
    abstract val usbDescriptor: UsbUtil.UsbDescriptor
    abstract val serialUsbDescriptor: UsbUtil.UsbDescriptor
    abstract val supportsRamLoading: Boolean

    sealed interface XilinxBoard {
        val bridgeFile: String
        val idCode: String
    }

    data object AlchitryPtV2 : Board(), XilinxBoard {
        override val name = "Alchitry Pt V2"
        override val alias = "PtV2"
        override val fpgaName = "xc7a100tfgg484-2"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Pt V2",
                "Alchitry Pt V2",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val bridgeFile = "/bridges/pt_v2.bin"
        override val idCode = "13631093"
        override val supportsRamLoading = true
    }

    data object AlchitryAuV2 : Board(), XilinxBoard {
        override val name = "Alchitry Au V2"
        override val alias = "AuV2"
        override val fpgaName = "xc7a35tftg256-2"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Au V2",
                "Alchitry Au V2",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val bridgeFile = "/bridges/au_v2.bin"
        override val idCode = "0362D093"
        override val supportsRamLoading = true
    }

    data object AlchitryCuV2 : Board() {
        override val name = "Alchitry Cu V2"
        override val alias = "CuV2"
        override val fpgaName = "ICE40HX8K-CB132IC"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Cu V2",
                "Alchitry Cu V2",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val supportsRamLoading = false
    }

    data object AlchitryAu : Board(), XilinxBoard {
        override val name = "Alchitry Au"
        override val alias = "Au"
        override val fpgaName = "xc7a35tftg256-1"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Au",
                "Alchitry Au",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val bridgeFile = "/bridges/au.bin"
        override val idCode = "0362D093"
        override val supportsRamLoading = true
    }

    data object AlchitryAuPlus : Board(), XilinxBoard {
        override val name = "Alchitry Au+"
        override val alias = "Au+"
        override val fpgaName = "xc7a100tftg256-1"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Au+",
                "Alchitry Au+",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val bridgeFile = "/bridges/au_plus.bin"
        override val idCode = "13631093"
        override val supportsRamLoading = true
    }

    data object AlchitryCu : Board() {
        override val name = "Alchitry Cu"
        override val alias = "Cu"
        override val fpgaName = "ICE40HX8K-CB132IC"
        override val usbDescriptor =
            UsbUtil.UsbDescriptor(
                "Alchitry Cu",
                "Alchitry Cu",
                PortInterfaceType.INTERFACE_A
            )
        override val serialUsbDescriptor = usbDescriptor.copy(d2xxInterface = PortInterfaceType.INTERFACE_B)
        override val supportsRamLoading = false
    }
}
