# Scheduler NanoService

An implementation of basic query API for retrieving data of a public transportation service.

### Technologies

* Kotlin
* Gradle
* Micronaut Framework [https://micronaut.io/]
* JUnit
* Docker
* Postman

#### External dependencies
* Commons-codec
* Hamcrest

### Concepts

* ##### Lines
    * Three public transportation lines operate the city
    * Each line has more than 2 stops
    * Lines may share stops with each other (transit stations)
* ##### Stops
    * Each stop has an unique identifier
    * Each stop has an unique pair of integers (X,Y) as its coordinates
* ##### Timing data
    * TimeOfDay uses ISO LocalDate format '09:55:59'
    * For each Stop at a given time of the day, 
    there is one or more services available
    * Delays are dynamic information on the current state of each line

### Open Issues

* Multi-tenancy support is missing from the design. Would be nice to serve multiple cities with the same instance.
* DataSet versioning: we should be able to restore the state of stop graph/timetables/delays at any moment of time in the past.
* Direction information for lines and times is missing
* Timing data domain should be specified properly:
    * Arrival/Departure time
    * Scheduled/Expected/Actual times should follow this concept:
        * ExpectedArrivalDelay: delta between ScheduledArrival and ExpectedArrival
        * ActualArrivalDelay: delta between ScheduledArrival and ActualArrival
        * Deltas can be negative if service arrives/departs ahead of the schedule  
    * Transit time for transit stations
* Delay information per vehicle might be a better option
* Calendar of working days/holidays, service maintenance/shutdowns is missing
* Potentially useful options:
    * Transit stations should have accessibility and transit time data
    * Lines should have accessibility data: stair-less access, bicycles allowed etc...
    
## Rest API

The container image contains a reference data set suitable for testing and prototyping.
Call 


## Docker
* build with 'docker build .' and notice the container id in the console output:
```Successfully built 2a54babf60c4```
* run with  `docker run -p8081:8081 2a54babf60c4` whereas last parameter it the image name

## Postman

Use postman script to test running container by importing this file to Postman: 

Postman Example: [https://github.com/seredkin/scheduler/blob/master/postman-screenshot.png]

Postman collection: [https://github.com/seredkin/scheduler/blob/master/scheduler.postman_collection.json]

Add your own Postman requests


# Development

Open build.gradle in our IDE of choice, set up annotation processing for Micronaut:
 
[https://guides.micronaut.io/creating-your-first-micronaut-app-kotlin/guide/index.html]

By running all test with coverage, you should get ˜90% line coverage (approximation is given to current inability of IDEA to evaluate Kotlin coverage properly):

Coverage example: [https://github.com/seredkin/scheduler/blob/master/coverage-screenshot.png] 
      
