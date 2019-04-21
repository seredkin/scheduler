package rest.scheduler

import io.micronaut.http.MediaType
import io.micronaut.http.multipart.CompletedFileUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.*

object ConfigControllerTest {
    @Test
    fun testConfigController() {
        val ctx = TestContext()
        val configController = ctx.configController
        configController.clearData()
        assert(ctx.apiController.citySchedule().body()!!.lines.isEmpty())
        configController.uploadConfig("lines", TestFileUpload("lines"))
        configController.uploadConfig("stops", TestFileUpload("stops"))
        configController.uploadConfig("times", TestFileUpload("times"))
        configController.uploadConfig("delays", TestFileUpload("delays"))

        val (cityName, validAt, lines, stops, delays, cityTimetable, stopTimeTable) = ctx.apiController.citySchedule().body()!!

        assert(cityName.isNotBlank())
        assert(!validAt.isAfter(ZonedDateTime.now()))
        assert(lines.size == delays.size)//as we never arrive on time :)
        assertEquals(stops.map { it.id }.sorted(), stopTimeTable.keys.sorted())
        assertEquals(stops.map { it.id }.sorted().toSet(), cityTimetable.flatMap { it.value }.map { it.stopId }.sorted().toSet())

    }

    private class TestFileUpload(private val file: String) : CompletedFileUpload {
        override fun getFilename(): String {
            return "./data/$file.csv"
        }

        override fun isComplete() = true

        override fun getByteBuffer(): ByteBuffer {
            error("not implemented")
        }

        override fun getName(): String {
            error("not implemented")
        }

        override fun getSize(): Long {
            error("not implemented")
        }

        override fun getBytes(): ByteArray {
            error("not implemented")
        }

        override fun getContentType(): Optional<MediaType> {
            error("not implemented")
        }

        override fun getInputStream(): InputStream {
            return Files.newInputStream(Paths.get(this.filename))
        }

    }

}

