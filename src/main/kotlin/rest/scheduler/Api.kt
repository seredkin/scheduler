package rest.scheduler

import java.time.LocalTime
import java.time.ZonedDateTime

data class Line(val id: Long, val name: String)
data class Stop(val id: Long, val x: Int, val y: Int)
data class Times(val lineId: Long, val stopId: Long, val time: String)
data class ArrivalTime(val lineId: Long, val stopId: Long, val time: LocalTime)
data class Delay(val lineName: String, val delay: Int)
data class ExpectedDelay(val lineName: String, val delaySeconds: Int, val reportedAt: ZonedDateTime)
data class CityScheduleResponse(
        val lines: Collection<Line>, val stops: Collection<Stop>,
        val delays: Collection<ExpectedDelay>,
        val cityTimetable: Map<LocalTime, List<ArrivalTime>>,
        val stopTimeTable: Map<Long, List<ArrivalTime>>
)

data class StopScheduleResponse(
        val stopId: Long,
        val localTime: LocalTime,
        val arrivalTimes: Pair<LocalTime, List<ArrivalTime>>
)