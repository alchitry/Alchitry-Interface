package com.alchitry.hardware.usb

import com.alchitry.hardware.Board
import com.alchitry.hardware.Log
import com.alchitry.hardware.usb.ftdi.LatticeSpi
import com.alchitry.hardware.usb.ftdi.XilinxJtag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

interface BoardLoader {
    suspend fun eraseFlash()
    suspend fun writeBin(binFile: File, flash: Boolean) {
        writeBin(withContext(Dispatchers.IO) {
            binFile.readBytes()
        }, flash)

    }

    suspend fun writeBin(binData: ByteArray, flash: Boolean)

    companion object {
        private suspend inline fun useLoader(board: Board, boardIdx: Int, block: (BoardLoader) -> Unit): Boolean {
            UsbUtil.lock.withLock {
                UsbUtil.openFtdiDevice(board, boardIdx).use { ftdi ->
                    if (ftdi == null)
                        return false
                    val loader = when (board) {
                        is Board.XilinxBoard -> XilinxJtag.init(ftdi, board)
                        Board.AlchitryCu, Board.AlchitryCuV2 -> LatticeSpi.init(ftdi)
                    }
                    block(loader)
                }
                return true
            }
        }

        suspend fun erase(board: Board, boardIdx: Int): Boolean {
            return useLoader(board, boardIdx) { loader ->
                try {
                    loader.eraseFlash()
                } catch (t: Throwable) {
                    t.message?.let { Log.error(it) }
                    throw t
                }
            }
        }

        suspend fun load(board: Board, boardIdx: Int, binFile: File, flash: Boolean): Boolean {
            return useLoader(board, boardIdx) { loader ->
                try {
                    loader.writeBin(binFile, flash)
                } catch (t: Throwable) {
                    t.message?.let { Log.error(it) }
                    throw t
                }
            }
        }

        suspend fun load(board: Board, boardIdx: Int, binData: ByteArray, flash: Boolean): Boolean {
            return useLoader(board, boardIdx) { loader ->
                try {
                    loader.writeBin(binData, flash)
                } catch (t: Throwable) {
                    t.message?.let { Log.error(it) }
                    throw t
                }
            }
        }
    }
}