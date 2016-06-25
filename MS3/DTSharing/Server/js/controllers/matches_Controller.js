var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    Ratings = require('../models/ratings'),
    async = require('async'),
    mongoose = require('mongoose');
 
//Es sollen alle zu den parametern passenden matches ermittelt werden
module.exports.findMatches = function (req, res) {
    
    //query für die Datenbankabfrage
    var query;
    
    //Überprüfung des Ticketstatusses des Users
    if(req.query.has_season_ticket == 'true') {
        
        //der User besitzt ein Dauerticket
        query = {
            owner_user_id : {$ne: req.query.user_id}, //Der User darf sich nicht selber finden
            unique_trip_id : req.query.unique_trip_id, //Die unique_trip_id muss identisch sein
            has_season_ticket : false, //Der Partner darf kein season_ticket haben
            owner_sequence_id_departure_station : {$gte: req.query.sequence_id_departure_station}, //Der Partner muss an der selben oder einer späteren Haltestelle einsteigen
            owner_sequence_id_target_station : {$lte: req.query.sequence_id_target_station}, //Der Partner muss an der selben oder einer früheren Haltestelle aussteigen
            partner_user_id : null //Der dt_trip darf noch keinen Partner haben
        }
    } else {
        query = {
            
            //Der User besitzt kein Dauerticket
            owner_user_id : {$ne: req.query.user_id}, //Der User darf sich nicht selber finden
            unique_trip_id : req.query.unique_trip_id, //Die unique_trip_id muss identisch sein
            has_season_ticket : true, //Der Partner muss ein season_ticket haben
            owner_sequence_id_departure_station : {$lte: req.query.sequence_id_departure_station}, //Der Partner muss an der selben oder einer früheren Haltestelle einsteigen
            owner_sequence_id_target_station : {$gte: req.query.sequence_id_target_station}, //Der Partner muss an der selben oder einer späteren Haltestelle aussteigen
            partner_user_id : null //Der dt_trip darf noch keinen Partner haben
        }
    }
    
    //passende dt_trips werden Ermittelt
    Dt_trips.find(query, '_id trip_id date route_name owner_user_id owner_departure_time owner_departure_station_name owner_arrival_time owner_target_station_name', function (err, results) {
        
        if(err) {
            //Fehler bei der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //Es wurde kein dttrip (match) gefunden
        if(!results.length) {
            console.log('No Matches found | 404');
            res.status(404);
            res.send({
                error_message: 'No Matches found'
            });
            return;
        }
        
        //Array in dem die matches gespeichert werden
        var matches = []
        
        //Alle ermittelten dt_trips müssen mit Daten der Anbieter angereichert werden
        async.each(results, function(result, callback) {
            
            //Die Abfragen können parallel erfolgen
            async.parallel([
                
                //picture, first_name und last_name des Anbieters ermitteln
                function(callback) {
                    Users.findById(result.owner_user_id, '_id picture first_name last_name', function (err, user) {
                        callback(err, user);
                    });
                
                //Rating des Anbieters ermitteln
                }, function(callback) {
                    Ratings.find({user_id : result.owner_user_id}, 'stars', function(err, results) {
                        if(err) {
                            //Fehler bei der Datenbankabfrage
                            callback(err)
                        }
                        //Die durchschnittliche Bewertung des users ermitteln
                        //average_rating soll 0 sein wenn der user noch nicht bewertet wurde
                        var average_rating = 0;
                        results.forEach(function(result) {
                            //jedes Rating zum average_rating hinzuadieren
                            average_rating += result.stars;
                        });

                        //Überprüfung damit nicht durch 0 geteilt wird
                        if(results.length) {
                            //average_rating durch die Anzahl an Bewertungen teilen
                            average_rating/=results.length;
                        }
                        
                        //Das average_rating wurde ermittelt
                        callback(null, average_rating);
                    });
                }
            ],
                           
            //wird aufgerufen nachdem die Abfragen ausgeführt wurden
            function(err, results){
                
                if(err) {
                    //Fehler bei der Datenbankabfrage
                    callback(err)
                }
                
                //Ein Object für jeden match erstellen
                var match = {
                    match : result, 
                    owner : {
                        _id : results[0]._id,
                        first_name : results[0].first_name,
                        last_name : results[0].last_name,
                        picture : results[0].picture,
                        average_rating : results[1]
                    }
                }
                
                //Das Object ins Array matches speichern
                matches.push(match);
                callback(null); 
            });
            
        //wird aufgerufen nachdem alle matches durchlaufen wurden
        }, function(err) {
            
            if(err) {
                //Es gabe einen Fehler bei der Datenbankabfrage
                res.status(500);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Die matches werden als response übertragen
            res.json(matches);
        });
    });
}