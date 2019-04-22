package rest.scheduler

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@Controller
class ApiController(private val scheduleService: ScheduleService) {

    // Indicate if a given line is currently delayed
    @Get("/delay/{lineId}")
    fun isDelayed(lineId: Long): HttpResponse<ExpectedDelay> = ok(scheduleService.expectedDelay(lineId))

    // Find a vehicle for a given time and X & Y coordinates
    @Get("/line/{x}.{y}/{timeOfDay}")
    fun spatialSearch(x: Int, y: Int, timeOfDay: String): HttpResponse<List<Line>> {
        val localTime = LocalTime.parse(timeOfDay, DateTimeFormatter.ISO_TIME)
        return ok(scheduleService.stopScheduleStartingFrom(x, y, localTime).values.flatMap { listOf(it) })
    }

    // Return the vehicle arriving next at a given stop
    @Get("/next/{stopId}/{localTime}")
    fun stopSchedule(@PathVariable stopId: Long, @PathVariable localTime: String? = null): HttpResponse<StopScheduleResponse> {
        val atTime = Optional.ofNullable(localTime)
                .map { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
                .orElse(LocalTime.now())
        return ok(
                StopScheduleResponse(
                        stopId = stopId,
                        localTime = atTime,
                        arrivalTimes = scheduleService.arrivingVehicles(stopId, atTime)
                )
        )
    }

    @Get("/schedule")
    fun citySchedule(): HttpResponse<CityScheduleResponse> =
            ok(CityScheduleResponse(
                    lines = scheduleService.lineRepository.fetchAll(),
                    stops = scheduleService.stopRepository.fetchAll(),
                    delays = scheduleService.delayRepository.fetchAll(),
                    cityTimetable = scheduleService.timeRepository.fetchAllTimes(),
                    stopTimeTable = scheduleService.timeRepository.fetchAllStops()
            ))
}

