var async = require('async'),
    utils = require('../lib/utils'),
    StopTimes = require('../models/stopTimes'),
    Stops = require('../models/stops'),
    Trips = require('../models/trips'),
    Calendars = require('../models/calendars'),
    CalendarDates = require('../models/calendarDates');

module.exports.findTrips = function (req, res) {
    //Name der Abfahrtshaltestelle
    var departureStationName = req.query.departureStationName;
    //Name der Zielhaltestelle
	var	targetStationName = req.query.targetStationName;
    //Abfahrtszeit wird vom Format SS:HH:MM in Sekunden umgewandelt
	var	departureTime = utils.timeToSeconds(req.query.departureTime);
    //Abfahrtsdatum vom Format DD.MM.YY in ein Date objekt geschrieben
	var	departureDate = utils.formatDate(req.query.departureDate);
    //Ticketstatus des Suchenden, wird zur ermittlung der Anzahl der Matches benötigt
	var	hasSeasonTicker = req.query.hasSeasonTicker;
    //ID des Anfragstellers, wird zur ermittlung der Anzahl der Matches benötigt
    var userID = req.query.userID;
    //Die Anzahl an Trips die ermittelt werden sollen
    var numberTrips = 10;
    var departureStationID;
    var targetStationID;
    var matchingTrips = [];
    var uniqueTrips = [];
    
    var totalStartTime = (new Date()).getTime();
    
    async.waterfall([
        async.apply(setStationIDs, departureStationName, targetStationName),
        findMatchingTrips,
        findServiceIDs,
        async.apply(findTrips, 10),
    ], function (err, result) {
        //ToDo Errorhandling
        formatUniqueTrips();
        console.log(uniqueTrips);
        var endTime = (new Date()).getTime(); 
        console.log('Es wurde: ' + [endTime - totalStartTime] + ' MS gebraucht um die Verbindungen zu ermitteln!');
        console.log('');
        res.json(uniqueTrips);
    });

    function setStationIDs(departueStationName, targetStationName, callback) {
        async.parallel([
            async.apply(findStationID, departueStationName),
            async.apply(findStationID, targetStationName)
        ],
        function(err, results){
            departureStationID = results[0];
            targetStationID = results[1];
            callback(null); 
        });
    }

    function findStationID(stationName, callback) {
        Stops.findOne({stop_name : stationName}, function (err, result) {
            if (err || result == null) {
                return callback(new Error('Haltestelle '+stationName+' wurde nicht gefunden.'));
            }
            console.log('stationID: '+result.stop_id);
            callback(null, result.stop_id);  
        });  
    }
    
    function findMatchingTrips(callback) {
        StopTimes.find({stop_id : {$in: [departureStationID, targetStationID]}}, function(err, results) {
            var departureTrips = [];
            var targetTrips = [];
            results.forEach( function(result) {
                if(result.stop_id == departureStationID) {
                    departureTrips.push(result);
                } else {
                    targetTrips.push(result);
                }
            });
            console.log(departureTrips.length);
            console.log(targetTrips.length);
            console.log("1");
            departureTrips.forEach( function (departureTrip) {
                targetTrips.forEach( function (targetTrip) {
                    if(departureTrip.trip_id == targetTrip.trip_id) {
                        if(departureTrip.stop_sequence < targetTrip.stop_sequence) {
                            matchingTrips.push({
                                tripID : targetTrip.trip_id, 
                                departureTime : departureTrip.departure_time, 
                                arrivalTime : targetTrip.arrival_time, 
                                departureSequence : departureTrip.stop_sequence, 
                                targetSequence : targetTrip.stop_sequence,
                                serviceID : null,
                            });
                        }
                    }
                });
            });
            console.log("2");
            if(matchingTrips.length == 0) {
                return callback(new Error('Es gibt keine direckte Verbindung zwischen den beiden Bahnhöfen'));
            }
            console.log(matchingTrips.length+' Trips von ' + departureStationName + ' nach ' + targetStationName + ' gefunden');
            callback(err);
        });
    }

    function findServiceIDs(callback) {
        async.forEachOf(matchingTrips, function (trip, key, callback) {
            Trips.findOne({trip_id : trip.tripID}, function (err, result) {
                if (err || result == null) {
                    return callback(new Error('Keine Service ID gefunden'));
                }
                
                matchingTrips[key].serviceID = result.service_id;
                callback(null, trip.service_id);  
            });
        }, function (err) {
            callback(err);
        });
    }

    function findTrips(numberTrips, callback) {
        var currentDepartureDate = departureDate;
        currentDepartureDate.setDate(currentDepartureDate.getDate()-1);
        var currentDepartureTime = departureTime+86400;
        var x = 0;
        async.whilst(
            function () {return uniqueTrips.length < numberTrips && x < 7},
            function(callback) {
                console.log('Suche für: '+utils.formatDay(departureDate));
                async.waterfall([
                    async.apply(findTripsAtDay, currentDepartureTime, currentDepartureDate),
                ], function (err) {
                    console.log('Insgesamt '+uniqueTrips.length+' passende Trips gefunden');
                    currentDepartureDate.setDate(currentDepartureDate.getDate()+1);
                    if(currentDepartureTime - 86400 > 0) {
                        currentDepartureTime -= 86400;
                    } else {
                        currentDepartureTime = 0;
                    }
                    x++;
                    callback(null);
                });
            },
            function (err) {
                callback(err);
            }
        );
    }

    function findTripsAtDay(time, date, callback) {
        var newTrips = [];
        async.forEachOf(matchingTrips, function(trip, key, callback) {
            var tripDepartureTime = trip.departureTime;

            if(tripDepartureTime >= time) {
                Calendars.find({service_id : trip.serviceID}, function (err, results) {
                    if (err || results == null) {
                        return callback(new Error('Service ID nicht in der Calendars Collection gefunden'));
                    } else {
                        var takesPlace = false;
                        results.forEach(function (result) {
                            if(result[utils.getDayName(date).toLowerCase()] == 1 && result.start_date<=utils.formatDayWithoutDots(date) && result.end_date>utils.formatDayWithoutDots(date)) {
                            takesPlace = true;
                            }
                        });
                        if(takesPlace == true) {
                            CalendarDates.find({service_id : trip.serviceID}, function (err, results) {
                                if (err) {
                                    return callback(err);
                                } else {
                                    results.forEach(function (result) {
                                        if(result.date == utils.formatDayWithoutDots(date)) {
                                            takesPlace = false;
                                        }
                                    });
                                    if(takesPlace == true) {
                                        newTrips.push({trip});
                                        console.log('passender Trip gefunden   |'+key);
                                        return callback(null);
                                    } else {
                                        return callback(null);
                                    }
                                }
                            });
                        } else {
                            return callback(null);
                        }
                    }
                });
            }
            else {
                return callback(null);
            }
        }, function (err) {
            newTrips.sort(
                function(a, b) {return parseFloat(a.trip.departureTime) - parseFloat(b.trip.departureTime);
            });
            newTrips.forEach(function (result) {
                uniqueTrips.push({
                    tripID : result.trip.tripID,
                    uniqueTripID : ''+result.trip.tripID+[utils.formatDayWithoutDots(date)],
                    departureTime : result.trip.departureTime,
                    arrivalTime : result.trip.arrivalTime,
                    departureDate : date,
                    departureSequence : result.trip.departureSequence,
                    targetSequence : result.trip.targetSequence,
                    numberMatches : 0,
            
                });
            });
            callback(err);
        });
    }

    function formatUniqueTrips() {
        uniqueTrips.forEach(function (result) {
            if(result.departureTime > 86400) {
                result.departureTime -= 86400;
                result.departureDate.setDate(result.departureDate.getDate()+1);
            }
            if(result.targetTime > 86400) {
                result.targetTime -= 86400;
            }
            result.departureTime = utils.secondsToTime(result.departureTime);
            result.arrivalTime = utils.secondsToTime(result.arrivalTime);
            result.departureDate = utils.formatDay(result.departureDate);
        });
    }
        
    //wird zur besseren Lesbarkeite der Console beim Testen benötigt
    function sleepFor( sleepDuration ){
        var now = new Date().getTime();
        while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
    }
}