package rest.scheduler

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class ConfigurationService(
        val lineRepository: LineRepository,
        val stopRepository: StopRepository,
        val timeRepository: ScheduledTimeRepository,
        val delayRepository: ExpectedDelayRepository
) {
    internal fun clearAll() {
        delayRepository.clear()
        timeRepository.clear()
        stopRepository.clear()
        lineRepository.clear()
    }


    internal fun loadLines(file: String) {
        val inputLines = skipHeaderAndEmptyLines(file)
                .map { val values = it.split(","); Line(values[0].toLong(), values[1]) }.toList()
        lineRepository.clear()
        lineRepository.addAll(inputLines)
    }

    internal fun loadDelays(file: String) {
        val delays = skipHeaderAndEmptyLines(file)
                .map { val values = it.split(","); Delay(values[0], values[1].toInt()) }.toList()
        delayRepository.clear()
        delayRepository.addAll(delays.map { it.toExpectedDelay() })
    }

    internal fun loadStops(file: String) {
        val stops = file.lines().stream().skip(1).filter { !it.isBlank() }
                .map { val values = it.split(","); Stop(values[0].toLong(), values[1].toInt(), values[2].toInt()) }.toList()
        stopRepository.clear()
        stopRepository.addAll(stops)
    }


    internal fun loadTimes(file: String) {
        val inputData = skipHeaderAndEmptyLines(file)
                .map {
                    val values = it.split(",");
                    Times(values[0].toLong(), values[1].toLong(), values[2]).toScheduledTime()
                }
        timeRepository.clear()
        timeRepository.addAll(inputData.toList().sortedBy{ it.time }
        )
    }

    internal fun loadDefaults() {
            loadLines(fileAsText("./data/lines.csv"))
            loadStops(fileAsText("./data/stops.csv"))
            loadTimes(fileAsText("./data/times.csv"))
            loadDelays(fileAsText("./data/delays.csv"))
    }

    private fun fileAsText(stopsFile: String) = Files.lines(Paths.get(stopsFile)).collect(Collectors.joining("\n"))

    private fun skipHeaderAndEmptyLines(file: String) = file.lines().stream().skip(1).filter { !it.isBlank() }


}
