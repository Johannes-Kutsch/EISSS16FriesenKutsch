var async = require('async'),
    utils = require('../lib/utils'),
    StopTimes = require('../models/stopTimes'),
    Stops = require('../models/stops'),
    Trips = require('../models/trips'),
    Routes = require('../models/routes'),
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
    var numberTrips = 1;
    var departureTrips = [];
    var targetTrips = [];
    var connectingRoutes = [];
    var connectingTrips = [];
    var uniqueTrips = [];
    
    var totalStartTime = (new Date()).getTime();
    
    async.waterfall([
        async.apply(findStationIDs, departureStationName, targetStationName),
        findConectingTrips,
        async.apply(findTrips, 10),
    ], function (err, result) {
        //ToDo Errorhandling
        formatUniqueTrips();
        var endTime = (new Date()).getTime(); 
        console.log('Es wurden ' + [endTime - totalStartTime] + ' MS gebraucht um die Verbindungen zu ermitteln!');
        console.log('');
        res.json(uniqueTrips);
    });

    function findStationIDs(departueStationName, targetStationName, callback) {
        async.parallel([
            async.apply(findStationID, departueStationName),
            async.apply(findStationID, targetStationName)
        ],
        function(err, results){
            callback(err, results); 
        });
    }

    function findStationID(stationName, callback) {
        Stops.findOne({stop_name : stationName}, '-_id stop_id', function (err, result) {
            if (err || result == null) {
                return callback(new Error('Haltestelle '+stationName+' wurde nicht gefunden.'));
            }
            console.log('Station ID für ' + stationName + ' ist ' + result.stop_id);
            callback(null, result.stop_id);  
        });  
    }

    function findConectingTrips(stopIDs, callback) {
        var startTime = (new Date()).getTime();
        async.waterfall([
            function(callback) {
                async.parallel([
                    //async.apply(findTripsAtStop2, stopIDs[0], stopIDs[1]),
                    async.apply(findTripsAtStop, stopIDs[0]),
                    async.apply(findTripsAtStop, stopIDs[1])
                ],
                function(err, results){
                    departureTrips = results[0];
                    targetTrips = results[1];
                    var departureTripIDs = [];
                    var targetTripIDs = [];
                    departureTrips.forEach( function(result) {
                        departureTripIDs.push(result.trip_id);
                    });
                    targetTrips.forEach( function(result) {
                        targetTripIDs.push(result.trip_id);
                    });
                    var endTime = (new Date()).getTime();
                    console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um ' + departureTrips.length + '|' + targetTrips.length + ' Trip IDs für die Stop IDs ' + stopIDs[0] + '|' +stopIDs[1] + ' zu ermitteln ');
                    startTime = (new Date()).getTime();
                    callback(err, departureTripIDs, targetTripIDs); 
                });
            }, 
            function(departureTripIDs, targetTripIDs, callback) {
                async.parallel([
                    async.apply(findDistinctRouteIDs, departureTripIDs),
                    async.apply(findDistinctRouteIDs, targetTripIDs)
                ],
                function(err, results){
                    var endTime = (new Date()).getTime(); 
                    console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um ' + results[0].length + '|' + results[1].length + ' RouteIDs für ' + departureTripIDs.length + '|' + targetTripIDs.length + ' Trip IDs zu ermitteln ');
                    startTime = (new Date()).getTime();
                    results[0].forEach(function (departureRouteID) {
                        results[1].forEach(function (targetRouteID) {
                            if(departureRouteID == targetRouteID) {
                                connectingRoutes.push(departureRouteID);
                            }
                        });
                    });
                    var endTime = (new Date()).getTime(); 
                    console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um die RouteIDs welche von ' + departureStationName + ' nach ' + targetStationName + ' fahren zu ermitteln: ' + connectingRoutes);
                    startTime = (new Date()).getTime();
                    callback(err); 
                });
            }, 
            findTripsOnRoute,
        ], function (err, results) {
            var startTime = (new Date()).getTime();
            results.forEach( function(trip) {
                departureTrips.forEach( function (departureTrip) {
                    if(trip.trip_id == departureTrip.trip_id) {
                        targetTrips.forEach( function (targetTrip) {
                            //Früher mit der stop_sequence Arbeiten!
                            if(trip.trip_id == targetTrip.trip_id && departureTrip.departure_time <= targetTrip.arrival_time) {
                                var connectingTrip = [];
                                connectingTrip[0] = trip;
                                connectingTrip[1] = departureTrip;
                                connectingTrip[2] = targetTrip;
                                connectingTrips.push(connectingTrip);
                            }
                        });
                    }
                });
            });
            var endTime = (new Date()).getTime();
            console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um ' + connectingTrips.length + ' Fahrten von ' + departureStationName + ' nach ' + targetStationName +' zu ermitteln'); 
            callback(err);
        });
    }

    function findTripsAtStop(stopID, callback) {
        StopTimes.find({stop_id: stopID}, '-_id trip_id departure_time arrival_time stop_sequence', function(err, results) {
            if (err || results == null) {
                return callback(new Error('Kein Trip an der Haltestelle ' + stopID + ' gefunden'));
            }
            callback(null, results);      
        });
    }
        
    function findDistinctRouteIDs(tripIDs, callback) {
        Trips.distinct('route_id', {trip_id : {$in: tripIDs}}, function (err, results) {
            if (err || results == null) {
                return callback(new Error('Route ID nicht gefunden'));
            }
            callback(null, results);
        });
    }

    function findTripsOnRoute(callback) {
        var startTime = (new Date()).getTime();
        Trips.find({route_id : {$in: connectingRoutes}},'-_id service_id trip_id route_id', function (err, results) {
            if (err || results == null) {
                    return callback(new Error('Route ID nicht gefunden'));
            }
            var endTime = (new Date()).getTime();
            console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um ' + results.length + ' Trips auf den Routen ' + connectingRoutes + ' zu finden.');
            callback(null, results);
        });
    }

    function findTrips(numberTrips, callback) {
        var currentDepartureDate = departureDate;
        currentDepartureDate.setDate(currentDepartureDate.getDate()-1);
        var currentDepartureTime = departureTime+86400;
        var days = 0;
        async.whilst(
            //Waterfall entfernen?!?!?
            function () {return uniqueTrips.length < numberTrips && days < 8},
            function(callback) {
                console.log('Suche Fahrten für: '+utils.formatDay(departureDate)+ ' ab ' + utils.secondsToTime(currentDepartureTime) + ' Uhr');
                async.waterfall([
                    async.apply(findTripsAtDay, currentDepartureTime, currentDepartureDate),
                ], function (err) {
                    currentDepartureDate.setDate(currentDepartureDate.getDate()+1);
                    if(currentDepartureTime - 86400 > 0) {
                        currentDepartureTime -= 86400;
                    } else {
                        currentDepartureTime = 0;
                    }
                    days++;
                    callback(null);
                });
            },
            function (err) {
                callback(err);
            }
        );
    }

    function findTripsAtDay(time, date, callback) {
        var startTime = (new Date()).getTime();
        var newTrips = [];
        var dateFormated = utils.formatDayWithoutDots(date);
        var dayName = utils.getDayName(date).toLowerCase();
        var dayQuerry = [];
        if (dayName == 'monday') {
            dayQuerry = {monday: 1};
        }
        if (dayName == 'tuesday') {
            dayQuerry = {tuesday: 1};
        }
        if (dayName == 'wednesday') {
            dayQuerry = {wednesday: 1};
        }
        if (dayName == 'thursday') {
            dayQuerry = {thursday: 1};
        }
        if (dayName == 'friday') {
            dayQuerry = {friday: 1};
        }
        if (dayName == 'saturday') {
            dayQuerry = {saturday: 1};
        }
        if (dayName == 'sunday') {
            dayQuerry = {sunday: 1};
        }

        async.forEachOf(connectingTrips, function(trip, key, callback) {
            if(trip[1].departure_time >= time){
                Calendars.find({$and: [
                    {service_id : trip[0].service_id},
                    dayQuerry,
                    {start_date: {$lt: dateFormated}},
                    {end_date: {$gte: dateFormated}}
                    ]
                },
                function (err, results) {
                    if (err || !results.length) {
                        return callback(err);
                    }
                    CalendarDates.find({$and: [
                        {service_id : trip[0].service_id},
                        {date : { $ne: dateFormated }}
                    ]
                    }, function (err, results) {
                        if (err || !results.length) {
                            return callback(err);
                        }
                        newTrips.push(trip);
                        callback(null);
                    }); 
                });
            } else {
                callback(null);
            }
        }, function (err) {
            async.eachOf(newTrips, function (trip, key, callback) {
                Routes.findOne({route_id : trip[0].route_id}, '-_id route_short_name', function(err, result){
                    if (err) {
                        return callback(err);
                        trip[key][0].push({routeName : result.route_short_name});
                    }
                    callback(null);
                });
            }, function(err) {
                
                newTrips.sort(
                    function(a, b) {return parseFloat(a[1].departureTime) - parseFloat(b[1].departureTime);
                });
                
                newTrips.forEach(function(trip) {
                    uniqueTrips.push({
                        tripID : trip[0].trip_id,
                        serviceID : trip[0].service_id,
                        uniqueTripID : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)],
                        departureTime : trip[1].departure_time,
                        arrivalTime : trip[2].arrival_time,
                        departureDate : date,
                        departureSequence : trip[1].stop_sequence,
                        targetSequence : trip[2].stop_sequence,
                        routeName : trip[0].routeName,
                        numberMatches : 0,
                    });
                });
                
                console.log(uniqueTrips.length);
                var endTime = (new Date()).getTime();
                console.log('Es wurden ' + [endTime - startTime] + ' MS gebraucht um ' + newTrips.length + ' Fahrten für den ' + utils.formatDay(date) +' zu ermitteln'); 
                callback(err);
            });
        });
    }

    function formatUniqueTrips() {
        uniqueTrips.forEach(function (result) {
            if(result.departureTime > 86400) {
                result.departureTime -= 86400;
                result.departureDate.setDate(result.departureDate.getDate()+1);
            }
            if(result.arrivalTime > 86400) {
                result.arrivalTime -= 86400;
            }
            result.departureTime = utils.secondsToTime(result.departureTime).slice(0,5);
            result.arrivalTime = utils.secondsToTime(result.arrivalTime).slice(0,5);
            result.departureDate = utils.formatDay(result.departureDate);
        });
    }
        
    //wird zur besseren Lesbarkeite der Console beim Testen benötigt
    function sleepFor( sleepDuration ){
        var now = new Date().getTime();
        while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
    }
}