import com.alchitry.hardware.AlchitryFt
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class FtTests {
    @Test
    fun testFindBoards() {
        val boards = AlchitryFt.find_boards()
        AlchitryFt.connect(0).use { device ->
            device.writeBytes(0, byteArrayOf(1,2,3,4))
            Thread.sleep(1000)
            println("Data: ${device.readBytes(0, 4).toList()}")
        }
    }
}