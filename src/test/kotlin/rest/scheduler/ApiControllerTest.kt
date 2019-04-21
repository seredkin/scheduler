package rest.scheduler


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

object ApiControllerTest {

    @Test
    fun testReferenceDataSetOk() = main(arrayOf("debug"))

    @JvmStatic
    fun main(args: Array<String>) {
        val ctx = TestContext()
        val confService = ctx.configService
        val schedulerService = ctx.schedulerService
        val apiController = ctx.apiController
        confService.clearAll()
        confService.loadDefaults()
        assertEquals(12, confService.stopRepository.fetchAll().size)
        assertEquals(3, confService.lineRepository.fetchAll().size)

        //There are 15 stops in total and 2 transit stations, 2 lines and 3 lines crossing
        assertEquals(13, confService.timeRepository.fetchAllTimes().size)
        assertEquals(3, confService.delayRepository.fetchAll().size)

        if (args.contains("debug")) {
            println(" --- Reference DataSet Loaded --- ")
            println(apiController.citySchedule().getBody(CityScheduleResponse::class.java).get())
        }

        //each request returns 0 or 1 expected delays if the line is known
        schedulerService.lineRepository.fetchAll().forEach {
            val delay = apiController.isDelayed(it.id).body()!!
            if (delay.delaySeconds != 0) {
                assertEquals(delay.lineName, it.name)
            } else {
                assert(delay.lineName.startsWith("No delays"))
            }
        }

        //Check upcoming arrivals for each spatial point: at 09:59 there must be al least one service available
        val allLines = apiController.citySchedule().body()!!.lines
        assert(allLines.isNotEmpty())
        val earliestStart = schedulerService.fetchEarliestTime()
        schedulerService.stopRepository.fetchAll().forEach { stop ->
            schedulerService.timeRepository.findByStopId(stop.id).forEach { arrivalTime ->
                val nextArrivingLines: List<Line> = apiController.spatialSearch(stop.x, stop.y, earliestStart.toString()).body()!!
                assert(nextArrivingLines.isNotEmpty())
                assert(allLines.containsAll(nextArrivingLines))
            }
        }

        //Find a closing service for each line
        schedulerService.timeRepository.fetchAllTimes().forEach { pair ->
            pair.value.forEach { time ->
                val stopArrivals = apiController.stopSchedule(time.stopId, pair.key.toString()).body()!!.arrivalTimes
                assert(!stopArrivals.first.isBefore(pair.key))//equals or after
            }
        }

        //At 09:59am all lines are on duty
        val earliestTime = schedulerService.fetchEarliestTime()
        schedulerService.stopRepository.fetchAll().forEach {
            val stopScheduleStartingFrom = schedulerService.stopScheduleStartingFrom(it.x, it.y, earliestTime)
            if (stopScheduleStartingFrom.values.flatMap { line -> listOf(line) }.isEmpty()) {
                error("Found empty stop/service: $it; at $earliestTime")
            }
        }

        //At 10:16am no lines are on duty
        val latestTime = schedulerService.fetchLatestTime()
        schedulerService.stopRepository.fetchAll().forEach {
            val stopScheduleStartingFrom = schedulerService.stopScheduleStartingFrom(it.x, it.y, latestTime)
            if (stopScheduleStartingFrom.values.flatMap { line -> listOf(line) }.isNotEmpty()) {
                error("Found operating stop/service: $it; at $latestTime")
            }
        }

        //Extra  test for tomorrow's schedules
        val busiestStop = schedulerService.stopRepository.fetchAll().find { it.id == 3L }!!
        val wrapScheduleAroundGivenTime = schedulerService.wrapScheduleAroundGivenTime(busiestStop, latestTime.plusMinutes(1), true)
        assert(wrapScheduleAroundGivenTime.keys.first().isBefore(latestTime))
    }

    private fun ScheduleService.fetchLatestTime() =
            this.timeRepository.fetchAllTimes().keys.last().plusMinutes(1).plusSeconds(1)

    private fun ScheduleService.fetchEarliestTime() =
            this.timeRepository.fetchAllTimes().keys.first().minusSeconds(1)


}