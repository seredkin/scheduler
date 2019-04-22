package rest.scheduler

import io.micronaut.http.MediaType
import io.micronaut.http.multipart.CompletedFileUpload
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Optional

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

        val (lines, stops, delays, cityTimetable, stopTimeTable) = ctx.apiController.citySchedule().body()!!

        assert(lines.size == delays.size)//as we never arrive on time :)
        assertThat(stops.map { it.id }.sorted(), equalTo(stopTimeTable.keys.sorted()))
        assertThat(stops.map { it.id }.sorted().toSet(), equalTo(cityTimetable.flatMap { it.value }.map { it.stopId }.sorted().toSet()))

    }

    @Test
    fun testConfigControllerEx() {
        val ctx = TestContext()
        val configController = ctx.configController
        val httpResponse = configController.uploadConfig("fake", TestFileUpload("lines"))
        assertThat(httpResponse!!.code(), equalTo(400))
    }

    @Test fun loadDefaults(){
        val ctx = TestContext()
        val configController = ctx.configController
        configController.loadDefaults()
        assertThat(ctx.configService.lineRepository.fetchAll(), not(empty()))
        assertThat(ctx.configService.stopRepository.fetchAll(), not(empty()))
        assertThat(ctx.configService.delayRepository.fetchAll(), not(empty()))
        assertThat(ctx.configService.delayRepository.fetchAll(), not(empty()))
        configController.clearData()
        assertThat(ctx.configService.lineRepository.fetchAll(), empty())
        assertThat(ctx.configService.stopRepository.fetchAll(), empty())
        assertThat(ctx.configService.delayRepository.fetchAll(), empty())
        assertThat(ctx.configService.delayRepository.fetchAll(), empty())
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

