package rest.scheduler

internal class TestContext {
        private val delayRepository = ExpectedDelayRepository()
        private val lineRepository = LineRepository()
        private val stopRepository = StopRepository()
        private val timeRepository = ScheduledTimeRepository()

        val configService = ConfigurationService(lineRepository, stopRepository, timeRepository, delayRepository)
        val schedulerService = ScheduleService(lineRepository, stopRepository, timeRepository, delayRepository)

        val apiController = ApiController(schedulerService)
        val configController = ConfigController(configService)
    }