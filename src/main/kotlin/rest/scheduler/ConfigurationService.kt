package rest.scheduler

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        timeRepository.addAll(inputData.toList().sortedBy { it.time }
        )
    }

    internal fun loadDefaults() {
        loadLines(fileAsText("$dir/$lines", md5s.getValue(lines)))
        loadStops(fileAsText("$dir/$stops", md5s.getValue(stops)))
        loadTimes(fileAsText("$dir/$times", md5s.getValue(times)))
        loadDelays(fileAsText("$dir/$delays", md5s.getValue(delays)))
    }

    private fun fileAsText(dataFile: String, expectedChecksum:String):String {
        val text = Files.lines(Paths.get(dataFile)).collect(Collectors.joining("\n"))
        val fileChecksum = DigestUtils.md5Hex(text)
        if(fileChecksum != expectedChecksum){
            log.warn("Checksums do not match for reference data file: $dataFile." +
                    "Expected $expectedChecksum but was $fileChecksum")
        }
        return text
    }

    private fun skipHeaderAndEmptyLines(file: String) = file.lines().stream().skip(1).filter { !it.isBlank() }

    companion object {
        private const val dir = "./data"
        private const val lines = "lines.csv"
        private const val stops = "stops.csv"
        private const val times = "times.csv"
        private const val delays = "delays.csv"
        /** MD5s of the reference dataset */
        private val md5s = mapOf(
                Pair(delays, "3b72eaeec8ea2cd98d45ca57fa4946a7"),
                Pair(lines, "6f07fd3507308cf97f3276479434d152"),
                Pair(stops, "bef1917bfffb3aad799da4fa4fa4ede9"),
                Pair(times, "eb76febb0cfb545cdddad72c29b862bf")
        )
        private val log: Logger = LoggerFactory.getLogger(ConfigurationService::class.java)
    }

}
