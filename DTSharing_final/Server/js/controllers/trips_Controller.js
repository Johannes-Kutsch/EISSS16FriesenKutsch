var async = require('async'),
    utils = require('../lib/utils'),
    Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    StopTimes = require('../models/stop_times'),
    Stops = require('../models/stops'),
    Trips = require('../models/trips'),
    Routes = require('../models/routes'),
    Calendars = require('../models/calendars'),
    CalendarDates = require('../models/calendar_dates');

module.exports.findTrips = function (req, res) {
    //"Define"
    //Sekunden die ein Tag hat
    var seconds_per_day = 86400;
    //Die Anzahl an Trips die wenigstens ermittelt werden sollen
	var min_number_trips = 10;
    
    //Querydaten in Formate umwandeln mit denen gearbeitet werden kann
    //Abfahrtszeit wird vom Format SS:HH:MM in Sekunden umgewandelt
    var departure_time = utils.timeToSeconds(req.query.departure_time);
    //Abfahrtsdatum vom Format DD.MM.YY in ein Date objekt geschrieben
	var	departure_date = utils.formatDate(req.query.departure_date);
    
    //Arrays zum zwischenspeichern
    //Trips die am Startbahnhof halten
	var departure_trips = [];
    //Trips die am Zielbahnhof halten
    var target_trips = [];
    //Routen die an beiden Bahnhöfen halten
    var connecting_routes = [];
    //Trips die an beiden Bahnhöfen halten
    var connecting_trips = [];
    //Die auf den Abfahrtstag und die Abfahrtszeit zugeschnittenen Trips, welche als response übermittelt werden
    var unique_trips = [];
    
    //wird für die Zeitangaben in der Konsole benötigt
    var total_start_time = (new Date()).getTime();
    
    //Die im waterfall aufgelisteten asynchronen Funktionen werden der Reihe nach durchgearbeitet
    async.waterfall([
        //Die ID's welche zu den übergebenen Bahnhofsnamen passen werden ermittelt
        async.apply(findStationIDs, req.query.departure_station_name, req.query.target_station_name),
        
        //trips die von Bahnhof a zu Bahnhof b fahren werden ermittelt
        findConectingTrips,
        
        //die trips mit konkreten Abfahrtsdaten anreichern
        findUniqueTrips,
    ], function (err, result) {
        if(err) {
            //Es gab einen Fehler während der Ermittlung der trips
            if(err.type == '200') {
                //Es gab einen custom Fehler, etwa weil einem Bahnhofsnamen keine ID zugeordnert werden konnte
                //weil der Fehler auf falsche Eingaben des Benutzers zurückzuführen ist der Server also wie gewollt gearbeitet hat, wird die error_message mit einem 200er Statuscoode übermittelt
                //die Nutzung eines Statuscode welcher nicht dem 2xx muster entspricht würde "dreckigen" Code zum verarbeiten des Bodys Clientseitig voraussetzen
                //die Nutzung des 204er Statuscodes wurde in Erwägung gezogen, jedoch schnell verworfen, da die Nachricht eine error_message beinhaltet
                res.send({
                    error_message: err.message
                });
                console.error(err);
                return;
            } else {
                //Es gab einen Datenbankfehler
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
        } 

        //Überprüfen ob unique_trips ermittelt wurden
        if(!unique_trips.length) {
            //es wurden keine unique_trips ermittelt
            res.send({
                error_message: 'Es wurde keine direkte Verbindung zwischen ' + req.query.departure_station_name + ' und ' + req.query.target_station_name + ' gefunden.'
            });
            console.error(err);
            return;
        }
        
        //die unique_trips werden als response übermittelt
        res.json(unique_trips);
        
        console.log('Es wurden insgesamt ' + [(new Date()).getTime() - total_start_time] + ' MS gebraucht um ' + unique_trips.length + ' Verbindungen zu ermitteln!');
        
    });

    //ruft die function findStationID zwei mal parallel auf um die zum departue_station_name und target_station_name passenden stop_id's zu ermitteln
    function findStationIDs(departue_station_name, target_station_name, callback) {
        async.parallel([
            async.apply(findStationID, departue_station_name),
            async.apply(findStationID, target_station_name)
        ],
        function(err, results){
            callback(err, results); 
        });
    }

    //ermittelt die zu einem station_name gehöhrende stop_id und seinen stop_name
    function findStationID(station_name, callback) {
        
        //Es werden Stops gesucht die exakt mit dem station_name übereinstimmen
        Stops.find({stop_name : station_name}, '-_id stop_id stop_name', function (err, results) {
            
            if(err) {
                //Es gabe einen Datenbankfehler
                return callback(err);
                
            } else if(results.length == 1) {
                //Es wurde genau ein Bahnhof gefunden
                console.log('Station ID für ' + station_name + ' ist ' + results[0].stop_id);
                callback(err, results[0]);
            } else {
                //Es konnte kein Bahnhof ermittelt werden, die Suche wird etwas aufgelockert
                
                //Der einzelnen Wörte im station_name werden voneinander getrennt
                var station_name_fragments = station_name.split(' ');
                
                //query für die Datenbankabfrage
                var regex_query = [];
                
                //jedes Wort in station_name_fragments wird so formatiert das es caseinsensitive ist und an einer beliebiegen Stelle im Bahnhofsnamen stehen darf
                station_name_fragments.forEach(function(result) {
                    regex_query.push(new RegExp('.*'+result+'.*', 'i'));
                });
                
                //Es wird versucht einen Bahnhof zu finden dessen Namen alle Fragemente beinhaltet
                //so wird z.b. der Bahnhof Köln, Hansaring bei einem station_name von Hansaring, Hansaring Köln oder Hansaring, Köln gefunden
                Stops.find({stop_name : {$all: regex_query}}, '-_id stop_id stop_name', function (err, results) {
                    if(err) {
                        //Es gab einen Datebankfehler
                        return callback(err);
                    } else if (results.length > 1) {
                        //es wurde mehr als ein Bahnhof gefunden
                        return callback(new customError('200','Es wurde mehr als ein Bahnhof mit dem Namen '+station_name+' gefunden.'));
                    } else if (results.length == 0) {
                        //es wurde kein Bahnhof gefunden
                        return callback(new customError('200','Es wurde kein Bahnhof mit dem Namen '+station_name+' gefunden.'));
                    }
                    
                    console.log('Station ID für ' + station_name + ' ist ' + results[0].stop_id);
                    
                    //Die ermittelten Bahofsdaten werden weitergegeben
                    callback(err, results[0]); 
                });
            }
        });  
    }

    function findConectingTrips(stops, callback) {
        var start_time = (new Date()).getTime();
        async.waterfall([
            function(callback) {
                async.parallel([
                    async.apply(findTripsAtStop, stops[0].stop_id),
                    async.apply(findTripsAtStop, stops[1].stop_id)
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
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + departure_trip_ids.length + '|' + target_trip_ids.length + ' Trip IDs für die Stops ' + stops[0].stop_name + '|' + stops[1].stop_name + ' zu ermitteln ');
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
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results[0].length + '|' + results[1].length + ' RouteIDs für ' + departure_trip_ids.length + '|' + target_trip_ids.length + ' Trip IDs zu ermitteln ');
                    start_time = (new Date()).getTime();
                    results[0].forEach(function (departureRouteID) {
                        results[1].forEach(function (targetRouteID) {
                            if(departureRouteID == targetRouteID) {
                                connecting_routes.push(departureRouteID);
                            }
                        });
                    });
                    if(connecting_routes.length == 0) {
                        return callback(new customError('200','Es wurde keine direkte Verbindung von '+ stops[0].stop_name + ' nach ' + stops[1].stop_name +' gefunden.'));
                    }
                    var end_time = (new Date()).getTime(); 
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um die RouteIDs welche von ' + stops[0].stop_name + ' nach ' + stops[1].stop_name + ' fahren zu ermitteln: ' + connecting_routes);
                    start_time = (new Date()).getTime();
                    callback(err); 
                });
            }, 
            findTripsOnRoute,
        ], function (err, results) {
            if(err) {
                return callback(err);
            }
            var start_time = (new Date()).getTime();
            results.forEach( function(trip) {
                departure_trips.forEach( function (departure_trip) {
                    if(trip.trip_id == departure_trip.trip_id) {
                        target_trips.forEach(function (target_trip) {
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
            console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + connecting_trips.length + ' Fahrten von ' + stops[0].stop_name + ' nach ' + stops[1].stop_name + ' zu ermitteln');
            callback(err, stops);
        });
    }

    function findTripsAtStop(stop_id, callback) {
        StopTimes.find({stop_id: stop_id}, '-_id trip_id departure_time arrival_time stop_sequence', function(err, results) {
            if(err) {
                return callback(err);
            }
            callback(null, results);      
        });
    }
        
    function findDistinctRouteIDs(trip_ids, callback) {
        Trips.distinct('route_id', {trip_id : {$in: trip_ids}}, function (err, results) {
            if(err) {
                return callback(err);
            }
            callback(null, results);
        });
    }

    function findTripsOnRoute(callback) {
        var start_time = (new Date()).getTime();
        Trips.find({ route_id: { $in: connecting_routes } }, '-_id service_id trip_id route_id', function (err, results) {
            if(err) {
                return callback(err);
            }
            var end_time = (new Date()).getTime();
            console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results.length + ' Trips auf den Routen ' + connecting_routes + ' zu finden.');
            callback(null, results);
        });
    }

    function findUniqueTrips(stops, callback) {
        var current_departure_date = departure_date;
        current_departure_date.setDate(current_departure_date.getDate() - 1);
        var current_departure_time = departure_time + seconds_per_day;
        var days = 0;
        async.whilst(
            function () { return unique_trips.length < min_number_trips && days < 8 },
            function(callback) {
                console.log('Suche Fahrten für: ' + utils.formatDay(current_departure_date) + ' ab ' + utils.secondsToTime(current_departure_time) + ' Uhr...');
                async.waterfall([
                    async.apply(findTripsAtDay, current_departure_time, current_departure_date, stops),
                ], function (err) {
                    if(err) {
                        return callback(err);
                    }
                    current_departure_date.setDate(current_departure_date.getDate() + 1);
                    if(current_departure_time - seconds_per_day > 0) {
                        current_departure_time -= seconds_per_day;
                    } else {
                        current_departure_time = 0;
                    }
                    days++;
                    callback(err);
                });
            },
            function (err) {
                callback(err);
            }
        );
    }

    function findTripsAtDay(time, date, stops, callback) {
        var start_time = (new Date()).getTime();
        var new_trips = [];
        var date_formated = utils.formatDayWithoutDots(date);
        var day_name = utils.getDayName(date).toLowerCase();
        
        var day_querry;
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

        async.each(connecting_trips, function(trip, callback) {
            if (trip[1].departure_time >= time) {
                Calendars.find({
                    $and: [
                        {service_id : trip[0].service_id},
                        {start_date: {$lt: date_formated}},
                        {end_date: {$gte: date_formated}},
                        day_querry
                    ]}, '_id', function (err, results) {
                    if (err) {
                        return callback(err);
                    } else if(!results.length) {
                        return callback(null);
                    }
                    CalendarDates.findOne({
                        service_id : trip[0].service_id,
                        date : date_formated
                    }, function (err, result) {
                        if (err) {
                            return callback(err);
                        } else if(!result) {
                            new_trips.push(trip);
                        }
                        callback(null);
                    }); 
                });
            } else {
                callback(null);
            }
        }, function (err) {
            if(err) {
                return callback(err);
            }
            async.each(new_trips, function (trip, callback) {
                async.parallel([
                    function(callback) {
                        Routes.findOne({route_id : trip[0].route_id}, '-_id route_short_name', function(err, result){
                            if (err) {
                                return callback(err);
                            }
                            callback(null, result);
                        });
                    }, function(callback) {
                        var query;
                        if(req.query.has_season_ticket == 'true') {
                            query = {
                                owner_user_id : {$ne: req.query.user_id},
                                unique_trip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)], 
                                has_season_ticket : false, 
                                owner_sequence_id_departure_station : {$gte: trip[1].stop_sequence}, 
                                owner_sequence_id_target_station : {$lte: trip[2].stop_sequence},
                                partner_user_id : null
                            }
                        } else {
                            query = {
                                owner_user_id : {$ne: req.query.user_id},
                                unique_trip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)], 
                                has_season_ticket : true, 
                                owner_sequence_id_departure_station : {$lte: trip[1].stop_sequence}, 
                                owner_sequence_id_target_station : {$gte: trip[2].stop_sequence},
                                partner_user_id : null
                            }
                        }
                        Dt_trips.find(query, '_id', function (err, results) {
                            if(err) {
                                callback(err);
                            }
                            callback(null, results.length);
                        });
                    }
                ], function (err, results) {
                    trip.push({
                        route_name : results[0].route_short_name,
                        number_matches : results[1]
                    });
                    callback(err);
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
                        departure_date = new Date(departure_date.getFullYear(), departure_date.getMonth(), departure_date.getDate() + 1);
                    }
                    if(arrival_time > seconds_per_day) {
                        arrival_time -= seconds_per_day;
                    }
                    unique_trips.push({
                        trip_id : trip[0].trip_id,
                        unique_trip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)],
                        departure_time: utils.secondsToTime(departure_time).slice(0, 5),
                        arrival_time : utils.secondsToTime(arrival_time).slice(0,5),
                        departure_date: utils.formatDay(departure_date),
                        sequence_id_departure_station : trip[1].stop_sequence,
                        sequence_id_target_station : trip[2].stop_sequence,
                        departure_station_name : stops[0].stop_name,
                        target_station_name : stops[1].stop_name,
                        route_name : trip[3].route_name,
                        number_matches : trip[3].number_matches,
                    });
                });
                var end_time = (new Date()).getTime();
                console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + new_trips.length + ' Fahrten für den ' + utils.formatDay(date) +' zu ermitteln'); 
                callback(err);
            });
        });
    }

    //Custom Fehler erzeugen
    function customError(type, message) {
        this.type = type;
        this.message = message;
    }

    //wird zur besseren Lesbarkeite der Console beim Testen benötigt
    function sleepFor( sleepDuration ){
        var now = new Date().getTime();
        while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
    }
}