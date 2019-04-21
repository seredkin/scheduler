package rest.scheduler

import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.runtime.Micronaut
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import javax.inject.Singleton
import kotlin.text.Charsets.UTF_8

object Application {

    @JvmStatic
    fun main(args: Array<String>) = Micronaut.build().packages("rest.scheduler").mainClass(Application.javaClass).start()

}

fun Delay.toExpectedDelay(receivedAt: ZonedDateTime = now()) = ExpectedDelay(lineName, delay.times(60), receivedAt)

fun CompletedFileUpload.asText() = this.inputStream.bufferedReader(UTF_8).use { it.readText() }

fun Times.toScheduledTime() = ArrivalTime(lineId, stopId, LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME))


@Singleton
class LineRepository {
    private val data = ConcurrentSkipListMap<Long, Line>()
    fun clear() = data.clear()

    fun addAll(allLines: Collection<Line>) = data.putAll(allLines.map { Pair(it.id, it) })

    fun fetchAll() = data.toMap().values
    fun findById(lineId: Long) = Optional.ofNullable(data[lineId])
}

@Singleton
class StopRepository {
    private val data = ConcurrentSkipListMap<Long, Stop>()
    private val spatialData = ConcurrentSkipListMap<String, Stop>()
    fun clear() {
        data.clear(); spatialData.clear()
    }

    fun addAll(allStops: Collection<Stop>) {
        data.putAll(allStops.map { Pair(it.id, it) })
        spatialData.putAll(allStops.map { Pair("${it.x}.${it.y}", it) })
    }

    fun fetchAll() = data.toMap().values
    fun findBySpatial(x: Int, y: Int):Stop = Optional.ofNullable(spatialData["$x.$y"])
            .orElseGet { error("Stop not found in coordinates $x.$y") }

}

@Singleton
class ScheduledTimeRepository {
    private val timeIndex = ConcurrentSkipListMap<LocalTime, List<ArrivalTime>>()
    private val stopIndex = ConcurrentSkipListMap<Long, List<ArrivalTime>>()

    fun clear() = timeIndex.clear()

    fun addAll(allArrivalTimes: Collection<ArrivalTime>){
        allArrivalTimes.groupBy { it.time }.forEach {
            timeIndex.merge(it.key, it.value) { t, u -> t.plus(u).sortedBy { arrival -> arrival.time } }
        }
        allArrivalTimes.groupBy { it.stopId }.forEach {
            stopIndex.merge(it.key, it.value) { t, u -> t.plus(u).sortedBy { arrival -> arrival.time } }
        }
    }

    fun findByStopId(stopId: Long): List<ArrivalTime> = stopIndex[stopId]!!
    fun fetchAllTimes() = timeIndex.toMap()
    fun fetchAllStops() = stopIndex.toMap()
}

@Singleton
class ExpectedDelayRepository {
    private val data = ConcurrentSkipListMap<String, ExpectedDelay>()
    fun clear() = data.clear()

    fun addAll(allTimes: Collection<ExpectedDelay>) = data.putAll(allTimes.map { Pair(it.lineName, it) })

    fun findByLineName(lineName: String): Optional<ExpectedDelay> = Optional.ofNullable(data[lineName])
    fun fetchAll() = data.values
}

