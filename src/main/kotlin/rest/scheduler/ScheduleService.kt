package rest.scheduler

import java.time.LocalTime
import java.time.LocalTime.now
import java.time.ZonedDateTime
import java.util.LinkedHashMap
import java.util.concurrent.ConcurrentSkipListMap
import javax.inject.Singleton

@Singleton
class ScheduleService(val lineRepository: LineRepository,
                      val stopRepository: StopRepository,
                      val timeRepository: ScheduledTimeRepository,
                      internal val delayRepository: ExpectedDelayRepository) {

    internal fun expectedDelay(lineId: Long) = lineRepository.findById(lineId)
            .map {
                delayRepository.findByLineName(it.name)
                        .orElse(ExpectedDelay("No delays reported for line ${lineId}", 0, ZonedDateTime.now()))
            }.orElseGet { error("Line with Id $lineId not found") }

    internal fun wrapScheduleAroundGivenTime(stop: Stop, localTime: LocalTime, wrapOverMidnight: Boolean = false): Map<LocalTime, Line> {
        val scheduleForStop = ConcurrentSkipListMap(
                timeRepository.findByStopId(stop.id).map {
                    Pair(it.time, lineRepository.findById(it.lineId).get())
                }.toMap()
        )
        val stopScheduleFromGivenTime = LinkedHashMap<LocalTime, Line>()
        stopScheduleFromGivenTime.putAll(scheduleForStop.tailMap(localTime, true))
        if(wrapOverMidnight) {
            stopScheduleFromGivenTime.putAll(scheduleForStop.headMap(localTime, false))
        }
        return stopScheduleFromGivenTime.toMap()
    }

    internal fun stopScheduleStartingFrom(x: Int, y: Int, localTime: LocalTime = now()): Map<LocalTime, Line> {
        val stop = stopRepository.findBySpatial(x, y)
        return wrapScheduleAroundGivenTime(stop = stop, localTime = localTime, wrapOverMidnight = false)
    }

    internal fun arrivingVehicles(stopId: Long, fromTime: LocalTime = now()): Pair<LocalTime, List<ArrivalTime>> {
        return Pair(now(), timeRepository.findByStopId(stopId).filter { !it.time.isBefore(fromTime)  })
    }
}
