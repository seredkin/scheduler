package rest.scheduler

import io.micronaut.http.HttpResponse.*
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.reactivex.Flowable
import kotlin.streams.toList

@Controller
class ConfigController(private val configurationService: ConfigurationService) {

    @Post("/config/{config}", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadConfig(@PathVariable config: String, @Body file: CompletedFileUpload): MutableHttpResponse<String>? {
        val fileText = file.asText()
        when (config) {
            "lines" -> configurationService.loadLines(fileText)
            "stops" -> configurationService.loadStops(fileText)
            "times" -> configurationService.loadTimes(fileText)
            "delays" -> configurationService.loadDelays(fileText)
            else -> return badRequest("Nothing uploaded. Check request data and schema")
        }
        return ok("Config $config uploaded")
    }

    @Get("/config/clear")
    fun clearData() = configurationService.clearAll()

}