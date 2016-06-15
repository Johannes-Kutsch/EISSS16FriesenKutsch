var async = require('async'),
    utils = require('../lib/utils'),
    StopTimes = require('../models/stop_times'),
    Stops = require('../models/stops'),
    Trips = require('../models/trips'),
    Routes = require('../models/routes'),
    Calendars = require('../models/calendars'),
    CalendarDates = require('../models/calendar_dates');

module.exports.findTrips = function (req, res) {
    //Name der Abfahrtshaltestelle
    var departure_station_name = req.query.departure_station_name;
    //Name der Zielhaltestelle
    var target_station_name = req.query.target_station_name;
    //Abfahrtszeit wird vom Format SS:HH:MM in Sekunden umgewandelt
    var departure_time = utils.timeToSeconds(req.query.departure_time);
    //Abfahrtsdatum vom Format DD.MM.YY in ein Date objekt geschrieben
	var	departure_date = utils.formatDate(req.query.departureDate);
    //Ticketstatus des Suchenden, wird zur ermittlung der Anzahl der Matches benötigt
	var has_season_ticket = req.query.has_season_ticket;
    //ID des Anfragstellers, wird zur ermittlung der Anzahl der Matches benötigt
	var user_id = req.query.user_id;
    //Die Anzahl an Trips die ermittelt werden sollen
	var number_trips = 1;
	var departure_trips = [];
    var target_trips = [];
    var connecting_routes = [];
    var connecting_trips = [];
    var unique_trips = [];
    var seconds_per_day = 86400;
    
    var total_start_time = (new Date()).getTime();
    
    async.waterfall([
        async.apply(findStationIDs, departure_station_name, target_station_name),
        findConectingTrips,
        async.apply(findTrips, 10),
    ], function (err, result) {
        var end_time = (new Date()).getTime(); 
        console.log('Es wurden ' + [end_time - total_start_time] + ' MS gebraucht um ' + unique_trips.length + ' Verbindungen zu ermitteln!');
        console.log('');
        res.json(unique_trips);
    });

    function findStationIDs(departue_station_name, target_station_name, callback) {
        async.parallel([
            async.apply(findStationID, departue_station_name),
            async.apply(findStationID, target_station_name)
        ],
        function(err, results){
            callback(err, results); 
        });
    }

    function findStationID(station_name, callback) {
        Stops.findOne({stop_name : station_name}, '-_id stop_id', function (err, result) {
            if(err) {
                return callback(err);
            } else if(result) {
                console.log('Station ID für ' + station_name + ' ist ' + result.stop_id);
                callback(err, result.stop_id);
            } else {
                var station_name_fragments = station_name.split(' ');
                var regex_query = [];
                station_name_fragments.forEach(function(result) {
                    regex_query.push(new RegExp('.*'+result+'.*'));
                });
                Stops.find({stop_name : {$all: regex_query}}, '-_id stop_id', function (err, results) {
                    if(err) {
                        return callback(err);
                    } else if (results.length > 1) {
                        return callback(new Error('More then one Match for '+station_name));
                    } else if (results.length == 0) {
                        return callback(new Error('No Match for '+station_name));
                    }
                    console.log('Station ID für ' + station_name + ' ist ' + results[0].stop_id);
                    callback(err, results[0].stop_id); 
                });
            }
        });  
    }

    function findConectingTrips(stop_ids, callback) {
        var start_time = (new Date()).getTime();
        async.waterfall([
            function(callback) {
                async.parallel([
                    async.apply(findTripsAtStop, stop_ids[0]),
                    async.apply(findTripsAtStop, stop_ids[1])
                ],
                function(err, results){
                    departure_trips = results[0];
                    target_trips = results[1];
                    var departure_trip_ids = [];
                    var target_trip_ids = [];
                    departure_trips.forEach(function (result) {
                        departure_trip_ids.push(result.trip_id);
                    });
                    target_trips.forEach(function (result) {
                        target_trip_ids.push(result.trip_id);
                    });
                    var end_time = (new Date()).getTime();
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + departure_trip_ids.length + '|' + target_trip_ids.length + ' Trip IDs für die Stop IDs ' + stop_ids[0] + '|' + stop_ids[1] + ' zu ermitteln ');
                    start_time = (new Date()).getTime();
                    callback(err, departure_trip_ids, target_trip_ids); 
                });
            }, 
            function(departure_trip_ids, target_trip_ids, callback) {
                async.parallel([
                    async.apply(findDistinctRouteIDs, departure_trip_ids),
                    async.apply(findDistinctRouteIDs, target_trip_ids)
                ],
                function(err, results){
                    var end_time = (new Date()).getTime(); 
                    //console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results[0].length + '|' + results[1].length + ' RouteIDs für ' + departure_trip_ids.length + '|' + target_trip_ids.length + ' Trip IDs zu ermitteln ');
                    start_time = (new Date()).getTime();
                    results[0].forEach(function (departureRouteID) {
                        results[1].forEach(function (targetRouteID) {
                            if(departureRouteID == targetRouteID) {
                                connecting_routes.push(departureRouteID);
                            }
                        });
                    });
                    var end_time = (new Date()).getTime(); 
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um die RouteIDs welche von ' + departure_station_name + ' nach ' + target_station_name + ' fahren zu ermitteln: ' + connecting_routes);
                    start_time = (new Date()).getTime();
                    callback(err); 
                });
            }, 
            findTripsOnRoute,
        ], function (err, results) {
            var start_time = (new Date()).getTime();
            results.forEach( function(trip) {
                departure_trips.forEach( function (departure_trip) {
                    if(trip.trip_id == departure_trip.trip_id) {
                        target_trips.forEach(function (target_trip) {
                            //Früher mit der stop_sequence Arbeiten!
                            if(trip.trip_id == target_trip.trip_id && departure_trip.departure_time <= target_trip.arrival_time) {
                                var connecting_trip = [];
                                connecting_trip[0] = trip;
                                connecting_trip[1] = departure_trip;
                                connecting_trip[2] = target_trip;
                                connecting_trips.push(connecting_trip);
                            }
                        });
                    }
                });
            });
            var end_time = (new Date()).getTime();
            console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + connecting_trips.length + ' Fahrten von ' + departure_station_name + ' nach ' + target_station_name + ' zu ermitteln');
            callback(err);
        });
    }

    function findTripsAtStop(stop_id, callback) {
        StopTimes.find({stop_id: stop_id}, '-_id trip_id departure_time arrival_time stop_sequence', function(err, results) {
            if (err || results == null) {
                return callback(new Error('Kein Trip an der Haltestelle ' + stop_id + ' gefunden'));
            }
            callback(null, results);      
        });
    }
        
    function findDistinctRouteIDs(trip_ids, callback) {
        Trips.distinct('route_id', {trip_id : {$in: trip_ids}}, function (err, results) {
            if (err || results == null) {
                return callback(new Error('Route ID nicht gefunden'));
            }
            callback(null, results);
        });
    }

    function findTripsOnRoute(callback) {
        var start_time = (new Date()).getTime();
        Trips.find({ route_id: { $in: connecting_routes } }, '-_id service_id trip_id route_id', function (err, results) {
            if (err || results == null) {
                    return callback(new Error('Route ID nicht gefunden'));
            }
            var end_time = (new Date()).getTime();
            console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results.length + ' Trips auf den Routen ' + connecting_routes + ' zu finden.');
            callback(null, results);
        });
    }

    function findTrips(number_trips, callback) {
        var current_departure_date = departure_date;
        current_departure_date.setDate(current_departure_date.getDate() - 1);
        var current_departure_time = departure_time + seconds_per_day;
        var days = 0;
        async.whilst(
            function () { return unique_trips.length < number_trips && days < 8 },
            function(callback) {
                console.log('Suche Fahrten für: ' + utils.formatDay(current_departure_date) + ' ab ' + utils.secondsToTime(current_departure_time) + ' Uhr...');
                async.waterfall([
                    async.apply(findTripsAtDay, current_departure_time, current_departure_date),
                ], function (err) {
                    current_departure_date.setDate(current_departure_date.getDate() + 1);
                    if(current_departure_time - seconds_per_day > 0) {
                        current_departure_time -= seconds_per_day;
                    } else {
                        current_departure_time = 0;
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
        var start_time = (new Date()).getTime();
        var new_trips = [];
        var date_formated = utils.formatDayWithoutDots(date);
        var day_name = utils.getDayName(date).toLowerCase();
        var day_querry = [];
        if (day_name == 'monday') {
            day_querry = {monday: 1};
        }
        if (day_name == 'tuesday') {
            day_querry = {tuesday: 1};
        }
        if (day_name == 'wednesday') {
            day_querry = {wednesday: 1};
        }
        if (day_name == 'thursday') {
            day_querry = {thursday: 1};
        }
        if (day_name == 'friday') {
            day_querry = {friday: 1};
        }
        if (day_name == 'saturday') {
            day_querry = {saturday: 1};
        }
        if (day_name == 'sunday') {
            day_querry = {sunday: 1};
        }

        async.forEachOf(connecting_trips, function(trip, key, callback) {
            if (trip[1].departure_time >= time) {
                Calendars.find({$and: [
                    {service_id : trip[0].service_id},
                    day_querry,
                    {start_date: {$lt: date_formated}},
                    {end_date: {$gte: date_formated}}
                    ]
                },
                function (err, results) {
                    if (err || !results.length) {
                        return callback(err);
                    }
                    CalendarDates.find({$and: [
                        {service_id : trip[0].service_id},
                        {date : { $ne: date_formated }}
                    ]
                    }, function (err, results) {
                        if (err || !results.length) {
                            return callback(err);
                        }
                        new_trips.push(trip);
                        callback(null);
                    }); 
                });
            } else {
                callback(null);
            }
        }, function (err) {
            async.eachOf(new_trips, function (trip, key, callback) {
                console.log(trip);
                Routes.findOne({route_id : trip[0].route_id}, '-_id route_short_name', function(err, result){
                    if (err) {
                        return callback(err);
                    }
                    trip.push({route_name : result.route_short_name});
                    console.log(trip);
                    callback(null);
                });
            }, function(err) {
                
                new_trips.sort(
                    function (a, b) {
                        return parseFloat(a[1].departure_time) - parseFloat(b[1].departure_time);
                });
                
                new_trips.forEach(function(trip) {
                    var departure_time = trip[1].departure_time;
                    var arrival_time = trip[2].arrival_time;
                    var departure_date = date;
                    if (departure_time > seconds_per_day) {
                        departure_time -= seconds_per_day;
                        departure_date = new Date(departure_date.getYear(), departure_date.getMonth(), departure_date.getDate() + 1);
                    }
                    if(arrival_time > seconds_per_day) {
                        arrival_time -= seconds_per_day;
                    }
                    unique_trips.push({
                        trip_id : trip[0].trip_id,
                        service_id : trip[0].service_id,
                        uniquetrip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)],
                        departure_time: utils.secondsToTime(departure_time).slice(0, 5),
                        arrival_time : utils.secondsToTime(arrival_time).slice(0,5),
                        departure_date: utils.formatDay(departure_date),
                        departure_sequence : trip[1].stop_sequence,
                        target_equence : trip[2].stop_sequence,
                        route_name : trip[3].route_name,
                        number_matches : 0,
                    });
                });
                var end_time = (new Date()).getTime();
                console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + new_trips.length + ' Fahrten für den ' + utils.formatDay(date) +' zu ermitteln'); 
                callback(err);
            });
        });
    }

    //wird zur besseren Lesbarkeite der Console beim Testen benötigt
    function sleepFor( sleepDuration ){
        var now = new Date().getTime();
        while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
    }
}