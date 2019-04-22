package rest.scheduler

import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload

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

    @Get("/config/default")
    fun loadDefaults() = configurationService.loadDefaults()

}