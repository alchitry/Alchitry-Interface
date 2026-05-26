import com.alchitry.hardware.AlchitryFt
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FtTests {
    @Test
    fun testFindBoards() {
        val boards = AlchitryFt.find_boards()
        assert(boards.isNotEmpty()) { "No boards found" }
        AlchitryFt.connect(0).use { device ->
            device.writeBytes(0, byteArrayOf(1,2,3,4))
            Thread.sleep(1000)
            println("Data: ${device.readBytes(0, 4).toList()}")
        }
    }

    @Test
    fun basicReadWriteTest() {
        AlchitryFt.connect(0).use { device ->
            val dataOut = byteArrayOf(1,2,3,4,5,6,7,8,9,10)
            device.writeBytes(0, dataOut)
            val dataIn = device.readBytes(0, dataOut.size)
            assert(dataIn.contentEquals(dataOut)) { "Read data does not match written data" }
        }
    }

    @Test
    fun asyncReadWrite() {
        AlchitryFt.connect(0).use { device ->
            runBlocking {
                launch {
                    val outOverlap = List(10) {
                        device.initializeOverlapped()
                    }
                    outOverlap.forEachIndexed { index, context ->
                        device.writePipeAsync(0, byteArrayOf((1+index).toByte(),2,3,4,5,6,7,8,9,10), context)
                    }
                    outOverlap.forEach {
                        assert(it.getResult(wait = true) == 10)
                    }
                }
                launch {
                    val inOverlap = List(10) {
                        device.initializeOverlapped()
                    }
                    inOverlap.forEach { context ->
                        device.readPipeAsync(0, 10, context)
                    }
                    inOverlap.forEachIndexed { index, context ->
                        assertEquals(context.getResultBytes(wait = true).asList(), byteArrayOf((1+index).toByte(),2,3,4,5,6,7,8,9,10).asList())
                    }
                }
            }
        }
    }
}