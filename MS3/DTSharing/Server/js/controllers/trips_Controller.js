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
    //Die Anzahl an Trips (Fahrten) die wenigstens ermittelt werden sollen
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
    //Routen (Linien) die an beiden Bahnhöfen halten
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
                
                console.log('Station ID für ' + station_name + ' ist ' + results[0].stop_id);
                
                //Es wurde genau ein Bahnhof gefunden
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
                //so wird z.b. der Bahnhof Köln, Hansaring bei einem station_name von Hansaring oder Hansaring Köln gefunden
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

    //Es werden alle trips ermittelt die von Bahnhof stops[0] nach stops[1] fahren
    function findConectingTrips(stops, callback) {
        var start_time = (new Date()).getTime();
        
        //Die Funktionen werden nacheinander aufgerufen
        async.waterfall([
            
            //Es werden alle trips die an den stops halten ermittelt 
            function(callback) {
                
                //Als erstes werden die Trips an beiden Haltestellen ermittelt
                async.parallel([
                    async.apply(findTripsAtStop, stops[0].stop_id),
                    async.apply(findTripsAtStop, stops[1].stop_id)
                ],
                function(err, results){
                    //Die Trips werden gespeichert, sie werden später erneut gebraucht.
                    departure_trips = results[0];
                    target_trips = results[1];
                    
                    //Die trip_id's werden in zwei Arrays gespeichert
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
                    
                    //Die trip_id's werden weitergegeben
                    callback(err, departure_trip_ids, target_trip_ids); 
                });
            }, 
            
            //Um nicht vergleichen zu müssen welche trips an beiden Bahnhöfen halten (im Schnitt ca 2500 * 2500 Vergleiche) werden die Routen (Linien) auf denen diese trips fahren miteinander verglichen (im Schnitt ca 6*6 Vergleiche)
            function(departure_trip_ids, target_trip_ids, callback) {
                //Nachdem die trip_ids ermittelt wurden können die Routen auf denen diese Trips fahren ermittelt werden
                async.parallel([
                    async.apply(findDistinctRouteIDs, departure_trip_ids),
                    async.apply(findDistinctRouteIDs, target_trip_ids)
                ],
                function(err, results){
                    
                    var end_time = (new Date()).getTime(); 
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results[0].length + '|' + results[1].length + ' RouteIDs für ' + departure_trip_ids.length + '|' + target_trip_ids.length + ' Trip IDs zu ermitteln ');
                    start_time = (new Date()).getTime();
                    
                    //Es wird ermittelt welche Routen an beiden Bahnhöfen halten
                    results[0].forEach(function (departureRouteID) {
                        results[1].forEach(function (targetRouteID) {
                            if(departureRouteID == targetRouteID) {
                                connecting_routes.push(departureRouteID);
                            }
                        });
                    });
                    
                    if(connecting_routes.length == 0) {
                        //es gibt keine Routen die an beiden Bahnhöfen halten
                        return callback(new customError('200','Es wurde keine direkte Verbindung von '+ stops[0].stop_name + ' nach ' + stops[1].stop_name +' gefunden.'));
                    }

                    var end_time = (new Date()).getTime(); 
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um die RouteIDs welche von ' + stops[0].stop_name + ' nach ' + stops[1].stop_name + ' fahren zu ermitteln: ' + connecting_routes);
                    start_time = (new Date()).getTime();
                                        
                    //Die Routen wurden ermittelt und gespeichert
                    callback(err);
                    
                });
                
            //Es werden alle Trips ermittelt die auf den ermittelten Routen fahren
            }, function(callback) {
                var start_time = (new Date()).getTime();
                
                
                Trips.find({ route_id: { $in: connecting_routes } }, '-_id service_id trip_id route_id', function (err, results) {
                    if(err) {
                        //Es gabe einen Datenbankfehler
                        return callback(err);
                    }

                    var end_time = (new Date()).getTime();
                    console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + results.length + ' Trips auf den Routen ' + connecting_routes + ' zu finden.');
                                        
                    //Die ermittelten Trips werden weitergegeben
                    callback(null, results);
                                        
                });
            },
        
        //Es wurden jetzt alle Trips ermittelt welche auf Routen liegen die an beiden Bahnhöfen halten
        //Dies beinhaltet jedoch auch Trips welche in die falsche Richtung fahren
        //In einigen seltenen fällen sind auch Trips enthalten welche nicht an einem der angegeben Bahnhöfe halten, da die Route zwar normalerweise an diesem Bahnhof hält, 
        //der Bahnhof jedoch für diesen Trip übersprungen wird oder dieser Trip nicht bis zur Endhaltestelle fährt
        
        //Es sollen nun alle Trips ermittelt werden die an beiden Bahnhöfen halten, zu der Route gehöhren und zuerst an der departure_station und danach an der target_station halten
        //Für die Ermittlung dieser Trips wurden zwei Varianten getestet:
        //Bei der ersten Variante wurden diese Trips aus der Collection stop_times ausgelesen
        //Bei der zweiten Vatiante wurden die trips welche auf einer Route fahren mit vorher gespeicherten departure_trips und target_trips verglichen
        //Die zweite Variante hat sich als deutlich schneller erwiesen und wurde deshalb genommen
        ], function (err, results) {
            if(err) {
                //Es gabe einen Fehler
                return callback(err);
            }
            
            var start_time = (new Date()).getTime();
            
            //Es werden alle trips die auf der Route sind durchlaufen
            results.forEach( function(trip) {
                
                //Es werden alle departure_trips durchlaufen
                departure_trips.forEach( function (departure_trip) {
                    
                    //Es wird verglichen ob die trip_id beider trips identisch ist
                    if(trip.trip_id == departure_trip.trip_id) {
                        //Die trip ID ist identisch
                        
                        //Es werden alle target_trips durchlaufen
                        target_trips.forEach(function (target_trip) {
                            
                            //Es wird verglichen ob die trip_id beider trips identisch ist und die Abfahrtszeit am Startbahnhof geringer als die Ankunftszeit am Zielbahnhof ist
                            if(trip.trip_id == target_trip.trip_id && departure_trip.departure_time <= target_trip.arrival_time) {
                                //Es wurde ein Trip gefunden der an beiden Bahnhöfen hält und in die richtige Richtung fährt
                                
                                //Es wird ein Array mit den Informationen über den trip sowie Abfahrtszeit, Ankunftszeit usw erstellt
                                var connecting_trip = [];
                                connecting_trip[0] = trip;
                                connecting_trip[1] = departure_trip;
                                connecting_trip[2] = target_trip;
                                
                                //Das Array wird in connecting_trips gespeichert
                                connecting_trips.push(connecting_trip);
                            }
                        });
                    }
                });
            });

            var end_time = (new Date()).getTime();
            console.log('Es wurden ' + [end_time - start_time] + ' MS gebraucht um ' + connecting_trips.length + ' Fahrten von ' + stops[0].stop_name + ' nach ' + stops[1].stop_name + ' zu ermitteln');
                        
            //Die connecting_trips wurden ermittelt
            callback(err, stops);
            
        });
    }

    //Es werden alle Trips die an einer Haltestelle halten ermittelt 
    function findTripsAtStop(stop_id, callback) {
        StopTimes.find({stop_id: stop_id}, '-_id trip_id departure_time arrival_time stop_sequence', function(err, results) {
            callback(err, results);      
        });
    }
        
    //Es werden alle Routes die zu den trip_ids gehöhren ermittelt 
    function findDistinctRouteIDs(trip_ids, callback) {
        Trips.distinct('route_id', {trip_id : {$in: trip_ids}}, function (err, results) {
            callback(err, results);
        });
    }

    //Die ermittelten conecting_trips werden sollen mit konkreten Daten angereichert werden
    //Es wird also das Object erstellt was als response gesendet werden soll
    function findUniqueTrips(stops, callback) {
            
        //Der Tag an dem Trips gesucht werden sollen
        var current_departure_date = departure_date;
        //Die Uhrzeit ab der Trips gesucht werden sollen
        var current_departure_time = departure_time;
            
        //Die gtfs-Daten sind so aufgebaut das die Abfahrtszeit von einigen Trips größer als 24:00 ist
        //Ein Trip der um 02:00 Nachts beginnt hat somit eine Abfahrtszeit von 26:00 in der Datenbank
        //Um diese Trips ermitteln zu können muss die Abfahrtszeit also um 24 Stunden erhöht werden
        current_departure_time += seconds_per_day;    
        //Als ausgleich dafür das die Uhrzeit um 24 Stunden erhöht wurde muss das Datum um ein verrignert werden
        //Dies geschieht hauptsächlich um Fehler innerhalb der unique_trip_id zu vermeiden
        current_departure_date.setDate(current_departure_date.getDate() - 1);
        
        //Variable die angibbt für wieviele Tage bereits gesucht wurde, wird benötigt um Endlossschleifen zu vermeiden
        var days = 0;
            
        //asynchrone whileloop
        async.whilst(
            
            //Bedingung zum beenden der Loop, es wurden entweder mindestens 10 Trips gesucht oder eine Woche lang nach Trips gesucht 
            //(<= 7, da wegen der obrigen Datums und Zeitanpassung die Schleife 8 mal durchlaufen werden muss um 7 Tage abzudecken)
            function () { return unique_trips.length < min_number_trips && days <= 7 },
            
            function(callback) {
            
                console.log('Suche Fahrten für: ' + utils.formatDay(current_departure_date) + ' ab ' + utils.secondsToTime(current_departure_time) + ' Uhr...');
            
                async.waterfall([
                    //Suche nach trips die dem aktuellem Datum und der aktuellen Uhrzeit entsprechen
                    async.apply(findTripsAtDay, current_departure_time, current_departure_date, stops),
            
                //Die Suche für den aktuellen Tag ist beendet
                ], function (err) {
                    
                    if(err) {
                        //Es gab einen Fehler
                        return callback(err);
                    }
                    
                    //Das Datum an dem gesucht werden soll wird um 1 erhöht
                    current_departure_date.setDate(current_departure_date.getDate() + 1);
                    
                    //Die Abfahrtszeit soll um 24 Stunden veringert werden, falls sie negativ werden sollte wird sie einfach auf 0 gesetzt
                    if(current_departure_time - seconds_per_day > 0) {
                        //Abfahrtszeit wird um 24 Stunden veringert
                        current_departure_time -= seconds_per_day;
                    } else {
                        //Abfahrtszeit wird 0 gesetzt
                        current_departure_time = 0;
                    }
                    
                    //Es wurde ein weiterer durchlauf durchgeführt
                    days++;
                    callback(err);
                });
            },
            function (err) {
                callback(err);
            }
        );
    }

    //Ermittelt alle Trips an einem Tag
    function findTripsAtDay(time, date, stops, callback) {
        
        //Zeit für die Konsolenausgabe
        var start_time = (new Date()).getTime();
        
        //Aray in dem die ermittelten Trips gespeichert werden
        var new_trips = [];
        
        //Das Datum wird umformatiert
        var date_formated = utils.formatDayWithoutDots(date);
        
        //Der Name des Tages, an dem Datum für das gesucht wird, wird ermittelt
        var day_name = utils.getDayName(date).toLowerCase();
        
        //Es wird ein query abhängig von dem eben ermittelten Namen erstellt
        var day_query;
        if (day_name == 'monday') {
            day_query = {monday: 1};
        }
        if (day_name == 'tuesday') {
            day_query = {tuesday: 1};
        }
        if (day_name == 'wednesday') {
            day_query = {wednesday: 1};
        }
        if (day_name == 'thursday') {
            day_query = {thursday: 1};
        }
        if (day_name == 'friday') {
            day_query = {friday: 1};
        }
        if (day_name == 'saturday') {
            day_query = {saturday: 1};
        }
        if (day_name == 'sunday') {
            day_query = {sunday: 1};
        }

        //Es wird für jeden connecting_trip überprüft ob er am aktuellem Tag fährt
        async.each(connecting_trips, function(trip, callback) {
            
            //Der connecting_trip soll nach der angegebenen departure_time losfahren
            if (trip[1].departure_time >= time) {
                
                //In der Calendars Collection muss überprüft werden ob der connecting_trip am aktuellem Wochentag fährt
                Calendars.findOne({
                    $and: [
                        //Der Eintrag in der Calendars Collection braucht die selbe service_id wie der connecting_trip
                        {service_id : trip[0].service_id},
                        //Der Eintrag muss noch gültig sein
                        {start_date: {$lt: date_formated}},
                        {end_date: {$gte: date_formated}},
                        //Der aktuelle Wochentag braucht in dem Eintrag einen Wert von 1
                        day_query
                    ]}, '_id', function (err, result) {
                        
                    if (err) {
                        //Es gab einen Datenbankfehler
                        return callback(err);
                        
                    } else if(!result) {
                        //Es wurde kein result gefunden, der connecting_trip fährt am aktuellem Tag nicht
                        return callback(null);
                    }
                        
                    //Es muss überprüft werden ob für das aktuelle Datum Ausnahmen vorliegen
                    CalendarDates.findOne({
                        //Die service_id muss übereinstimmen
                        service_id : trip[0].service_id,
                        //Der Eintrag muss für das aktuelle Datum existieren
                        date : date_formated,
                        //Ein exception_type von 2 gibt an das der connecting_trip nicht stattfindet
                        //Eine Überprüfung auf weitere exception_types wurde ausgelassen, da diese im aktuellem Datensatz nicht vorkommen
                        exception_type : 2
                    }, function (err, result) {
                        
                        if (err) {
                            //Es gab einen Datenbankfehler
                            return callback(err);
                        } else if(!result) {
                            //Wenn die Abfrage kein Ergebniss liefert findet der connecting_trip statt
                            new_trips.push(trip);
                        }
                        //Es wurde eine Ausnahme für den heutigen Tag gefunden
                        callback(null);
                    }); 
                });
            } else {
                callback(null);
            }
        
        //Wird aufgerufen nachdem alle connecting_trips durchlaufen wurden
        }, function (err) {
            
            if(err) {
                //Es gab einen Fehler
                return callback(err);
            }
            
            //Die neu gefunden Trips müssen mit dem route_name und der number_matches angereichert werden
            //jeder trip in new_trips wird durchlaufen
            async.each(new_trips, function (trip, callback) {
                
                //Die Ermittlung der Daten kann parallel stattfinden
                async.parallel([
                    
                    //Der route_name wird ermittelt
                    function(callback) {
                        Routes.findOne({route_id : trip[0].route_id}, '-_id route_short_name', function(err, result){
                            //Der route_name und/oder ein auftrender Datenbankfehler werden weitergegeben
                            callback(err, result.route_short_name);
                        });
                        
                    //Die Anzahl an Matches werden ermittelt
                    }, function(callback) {
                        
                        //Es muss ein query für die Datenbankabfrage erstellt werden
                        var query;
                        
                        //Jenachdem ob der user ein season_ticket besitzt oder nicht muss das query anders aufgebaut werden
                        if(req.query.has_season_ticket == 'true') {
                            
                            //user besitz ein season_ticket
                            query = {
                                owner_user_id : {$ne: req.query.user_id},
                                unique_trip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)], 
                                has_season_ticket : false, 
                                owner_sequence_id_departure_station : {$gte: trip[1].stop_sequence}, 
                                owner_sequence_id_target_station : {$lte: trip[2].stop_sequence},
                                partner_user_id : null
                            }
                        } else {
                            
                            //user besitzt kein season_ticket
                            query = {
                                owner_user_id : {$ne: req.query.user_id},
                                unique_trip_id : ''+trip[0].trip_id+[utils.formatDayWithoutDots(date)], 
                                has_season_ticket : true, 
                                owner_sequence_id_departure_station : {$lte: trip[1].stop_sequence}, 
                                owner_sequence_id_target_station : {$gte: trip[2].stop_sequence},
                                partner_user_id : null
                            }
                        }
                        
                        //Es wird nach matches gesucht
                        Dt_trips.find(query, '_id', function (err, results) {
                            //Die Anzahl an Ergebnissen (matches) wird weitergegeben und/oder ein auftrender Datenbankfehler werden weitergegeben
                            callback(err, results.length);
                        });
                    }
                    
                //Wird aufgerufen nachdem die number_matches und der route_name ermittelt wurden
                ], function (err, results) {
                    
                    if(err) {
                        //Es gab einen Fehler
                        return callback(err);
                    }
                    
                    //Die ermittelten Daten werden im trip gespeichert
                    trip.push({
                        route_name : results[0],
                        number_matches : results[1]
                    });
                    
                    callback(null);
                });
                
            //Wird aufgerufen nachdem alle new_trips durchlaufen wurden
            }, function(err) {
                
                if(err) {
                    //Es gab einen Fehler
                    return callback(err);
                }
                
                //Da bis jetzt asynchron gearbeitet wurde müssen die neu ermittelten Trips nach Abfahrtszeit sortiert werden
                new_trips.sort(
                    function (a, b) {
                        return parseFloat(a[1].departure_time) - parseFloat(b[1].departure_time);
                });
                
                //Die neu Ermittelten Trips müssen in ein Format gebracht werden das an den Client übermittelt werden soll
                new_trips.forEach(function(trip) {
                    var departure_time = trip[1].departure_time;
                    var arrival_time = trip[2].arrival_time;
                    var departure_date = date;
                    
                    //Falls der Trip nach mitternacht stattgefunden hat soll die Uhrzeit um 24 Stunden verringert werden und das Datum um eins erhöht werden
                    if (departure_time > seconds_per_day) {
                        departure_time -= seconds_per_day;
                        departure_date = new Date(departure_date.getFullYear(), departure_date.getMonth(), departure_date.getDate() + 1);
                    }
                    if(arrival_time > seconds_per_day) {
                        arrival_time -= seconds_per_day;
                    }
                    
                    //Der Trip wird zu den unique_trips hinzugefügt
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
                
                callback(null);
            });
        });
    }

    //Custom Fehler erzeugen
    function customError(type, message) {
        this.type = type;
        this.message = message;
    }
}