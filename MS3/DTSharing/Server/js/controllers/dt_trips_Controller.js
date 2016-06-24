var Users = require('../models/users'),
    Chats = require('../models/chats'),
    Dt_trips = require('../models/dt_trips'),
    Messages = require('../models/messages'),
    async = require('async'),
    fcm = require('node-gcm'),
    mongoose = require('mongoose');
 
//Einen dt_trip erstellen
module.exports.offer = function (req, res) {
    
    //Das dt_trip Object erstellen und mit Informationen füllen, da noch kein Partner vorhanden ist werden seine Werte auf null gesetzt
    var dt_trip = new Dt_trips({
        unique_trip_id: req.body.unique_trip_id,
        trip_id: req.body.trip_id,
        date: req.body.date,
        route_name: req.body.route_name,
        owner_departure_time: req.body.departure_time,
        owner_arrival_time: req.body.arrival_time,
        owner_user_id: req.params.user_id,
        owner_sequence_id_target_station: req.body.sequence_id_target_station,
        owner_sequence_id_departure_station: req.body.sequence_id_departure_station,
        owner_departure_station_name: req.body.departure_station_name,
        owner_target_station_name: req.body.target_station_name,
        has_season_ticket: req.body.has_season_ticket,
        partner_user_id: null,
        partner_sequence_id_target_station: null,
        partner_sequence_id_departure_station: null,
        partner_departure_station_name: null,
        partner_target_station_name: null,
        partner_departure_time: null,
        partner_arrival_time: null
    });
    
    //Den dt_trip in der Dt_trips collection speichern
    dt_trip.save(function (err, result) {
        
        if(err) {
            //Es gab einen Fehler während des speicherns
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //Der dt_trip wurde erfolgreich angelegt
        res.status(201);
        res.send({
            success_message: 'Offer sucessfull'
        });
        
        
        //Der Suchagent soll potentielle Partner für den grade eingetragenen Trip ermitteln und benachrichtigen
        //Dazu werden alle eingetragenen dt_trips durchsucht und es wird überprüft ob diese mit dem grade eingetragenem dt_trip kompatibel sind
        
        //Query zur Ermittlung der Partner
        var query;
        
        //Überprüfen welchen Ticketstatus der Partner braucht
        if(req.body.has_season_ticket == 'true') {
            query = {
                //Der Partner darf kein Dauerticket haben
                owner_user_id : {$ne: req.params.user_id}, //Die user_id des Partners darf nicht die des Nutzers sein der grade die Fahrt eingetragen hast
                unique_trip_id : req.body.unique_trip_id, //Der dt_trip braucht die selbe unique_trip_id
                has_season_ticket : false, //Der Partner darf kein Dauerticket haben
                owner_sequence_id_departure_station : {$gte: req.body.sequence_id_departure_station}, //Die sequence_id's des Partners müssen mit den des eingetragenen Trips kompatibel sein 
                owner_sequence_id_target_station : {$lte: req.body.sequence_id_target_station},
                partner_user_id : null //Der dt_trip darf noch keinen Partner haben
            }
        } else {
            query = {
                //Der Partner braucht ein Dauerticket
                owner_user_id : {$ne: req.params.user_id}, //Die user_id des Partners darf nicht die des Nutzers sein der grade die Fahrt eingetragen hast
                unique_trip_id : req.body.unique_trip_id, //Der dt_trip braucht die selbe unique_trip_id
                has_season_ticket : true, //Der Partner braucht ein Dauerticket
                owner_sequence_id_departure_station : {$lte: req.body.sequence_id_departure_station}, //Die sequence_id's des Partners müssen mit den des eingetragenen Trips kompatibel sein 
                owner_sequence_id_target_station : {$gte: req.body.sequence_id_target_station},
                partner_user_id : null //Der dt_trip darf noch keinen Partner haben
            }
        }
        
        //Die collection Dt_trips wird mit dem soeben erstelltem query durchsucht
        Dt_trips.find(query, '_id owner_user_id has_season_ticket owner_sequence_id_departure_station owner_sequence_id_target_station owner_departure_time owner_arrival_time owner_departure_station_name owner_target_station_name', function (err, results) {
            
            if (err) {
                //Es gab einen Datenbankfehler
                console.error(err);
                return;
            }
            
            //Alle gefundenen, passenden, dt_trips werden durchlaufen
            async.each(results, function(result, callback) {
                
                //Das fcm token des Users wird ermittelt
                Users.findById(result.owner_user_id, 'token', function(err, user) {
                    
                    if (err) {
                        //Es gab einen Datenbankfehler
                        console.error(err);
                        return;
                    }
                    
                    if(user.token) {
                        //Für den Nutzer ist ein fcm token vorhanden
                        
                        //Der body der fcm message
                        var body;
                        
                        //Überprüfen welchen Ticketstatus der user hat und entsprechend dem body einen String zuweisen
                        if(result.has_season_ticket == 'true') {
                            body = 'Es gibt einen neuen potentiellen Mitfahrer für einen deiner Trips'
                        } else {
                            body = 'Es gibt eine neue potentielle Mitfahrgelegenheit für einen deiner Trips'
                        }
                        
                        //Es wird eine fcm message erstellt
                        //Vorlage: https://github.com/ToothlessGear/node-gcm#usage
                        var message = new fcm.Message({
                            
                            //Die message wird mit Daten die zum aufrufen der Uri /matches benötigt werden gefüllt
                            data: {
                                type: 'search_agent',
                                dt_trip_id: result._id,
                                unique_trip_id: req.body.unique_trip_id,
                                sequence_id_departure_station: result.owner_sequence_id_departure_station,
                                sequence_id_target_station: result.owner_sequence_id_target_station,
                                user_id: result.owner_user_id,
                                has_season_ticket: result.has_season_ticket,
                                departure_time: result.owner_departure_time,
                                arrival_time: result.owner_arrival_time,
                                departure_station_name: result.owner_departure_station_name,
                                target_station_name: result.owner_target_station_name
                            },
                            notification: {
                                title: 'DTSharing - Suchagent',
                                body: body
                            }
                        });
                        
                        //Der sender wird mit dem fcm API key eingerichtet
                        var sender = new fcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                        
                        //Die Nachricht wird abgeschickt
                        sender.send(message, { registrationTokens: [user.token] }, function (err, response) {
                            if(err) {
                                //Fehler bei dem Sendevorgang
                                console.error(err);
                            }
                        });
                    }
                });
            });
        });
    });
}

//sich bei einem dt_trip als Partner eintragen
//einen zum dt_trip gehörigen Chatraum erstellen
module.exports.match = function (req,res) {
    
    //Den dt_trip bei dem sich eingetragen werden soll ermitteln und aktualisieren
    Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, { 
        partner_user_id: req.body.user_id,
        partner_departure_time: req.body.departure_time,
        partner_arrival_time: req.body.arrival_time,
        partner_sequence_id_target_station: req.body.sequence_id_target_station,
        partner_sequence_id_departure_station: req.body.sequence_id_departure_station,
        partner_departure_station_name: req.body.departure_station_name,
        partner_target_station_name: req.body.target_station_name
    }, function (err, result) {
        
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!result) {
            //Der dt_trip der aktualisiert werden soll existiert (nicht) mehr
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        
        //Es wird ein Chatraum für den dt_trip erstellt
        //Die beiden user des dt_trips sollen diesem angehöhren
        
        //Das chat Object erstellen und mit Daten füllen
        var chat = new Chats({
            owner_user_id: result.owner_user_id,
            partner_user_id: req.body.user_id,
            dt_trip_id: result._id,
            key: req.body.key //Der Key mit dem Nachrichten in diesem Chatraum verschlüsselt werden
        });
        
        //Den chat in die Chats Collection speichern
        chat.save(function (err, result) {
            
            if(err) {
                //Es gab einen Datenbankfehler beim speichern
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Der Chatraum wurde erstellt
            //In der response wird die chat_id des Chatraumes übermittelt
            res.send({
                success_message: 'successfully matched',
                chat_id: result._id
            });
            
            //Der Besitzer des Trips soll darüber benachrichtigt werden das ein Partner gefunden wurde
            //Das fcm token des Besitzers wird ermittelt
            Users.findById(result.owner_user_id, 'token', function(err, user) {
                
                if (err) {
                    //Es gab einen Datenbankfehler
                    console.error(err);
                    return;
                }
                
                if(user.token) {
                    //Es wurde ein Token gefunden
                    
                    //Es wird eine fcm message erstellt
                    //Vorlage: https://github.com/ToothlessGear/node-gcm#usage
                    var message = new fcm.Message({
                        data: {
                            type: 'new_partner',
                            chat_id: result._id
                        },
                        notification: {
                            title: 'DTSharing - Neuer Partner gefunden',
                            body: 'Es wurde ein Partner für eine deiner Fahrten gefunden, spreche jetzt die Fahrtdetails mit ihm ab!'
                        }
                    });
                    
                    //Der sender wird mit dem fcm API key eingerichtet
                    var sender = new fcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                    sender.send(message, { registrationTokens: [user.token] }, function (err, response) {
                        if(err) {
                            //Fehler bei dem Sendevorgang
                            console.error(err);
                        }
                    });
                }
            });
        });
    });
}

//alle dt_trips eines users ermitteln
module.exports.findDtTrips = function (req, res) {
    
    //alle dt_trips finden in denen der user owner oder partner ist
    Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partner_user_id : req.params.user_id}]}, '-__v', function (err, results) {
        
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!results.length) {
            //Es wurden keine dt_trips gefunden
            console.log('No Trips found | 404');
            res.status(404);
            res.send({
                error_message: 'No Trips found'
            });
            return;
        }
        
        //Array in dem alle dt_trips gespeichert werden
        var dt_trips = [];
        
        //Alle dt_trips durchlaufen
        results.forEach( function(result) {
            
            //ermitteln ob der user owner oder partner in diesem dt_trip ist 
            if(result.owner_user_id == req.params.user_id) {
                //der user ist owner
                
                //ermitteln ob es einen partner gibt
                var number_partners = 0;
                if(result.partner_user_id) {
                    number_partners = 1;
                }
                
                //den dt_trip in das Array speichern
                dt_trips.push({
                    _id : result._id,
                    date : result.date,
                    route_name : result.route_name,
                    departure_station_name : result.owner_departure_station_name,
                    target_station_name : result.owner_target_station_name,
                    arrival_time : result.owner_arrival_time,
                    departure_time : result.owner_departure_time,
                    number_partners : number_partners
                });
                
            } else {
                //der user is partner
                dt_trips.push({
                    _id : result._id,
                    date : result.date,
                    route_name : result.route_name,
                    departure_station_name : result.partner_departure_station_name,
                    target_station_name : result.partner_target_station_name,
                    arrival_time : result.partner_arrival_time,
                    departure_time : result.partner_departure_time,
                    number_partners : 1 //da der user partner ist hat er immer einen Mitfahrer, den owner des dt_trips
                });
            }
        });
        
        //alle dt_trips in der response übermitteln 
        res.json(dt_trips);
    });
}

//Details zu einem spezifischem dt_trip ermitteln
module.exports.findDtTrip = function (req, res) {
    
    //den dt_trip in der Collection Dt_trips finden
    Dt_trips.findById(req.params.dt_trip_id, '-__v', function (err, result) {
        
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!result) {
            //es gibt keinen dt_trip mit dieser dt_trip_id
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        
        //Zwei parallele aufrufe der Collection Users um die Vornamen der user die dem dt_trip angehöhren zu ermitteln
        async.parallel([
            
            //Vornamen des owners ermitteln
            function(callback) {
                //überprüfen ob es einen owner gibt
                if(result.owner_user_id) {
                    //es gibt einen owner
                    Users.findById(result.owner_user_id, '-_id first_name',function(err, result) {
                        callback(err, result.first_name);
                    });
                } else {
                    //es gibt keinen owner
                    callback(null);
                }
                
            //Vornamen des partners ermitteln
            }, function(callback) {
                //überprüfen ob es einen partner gibt
                if(result.partner_user_id) {
                    //es gibt einen partner
                    Users.findById(result.partner_user_id, '-_id first_name',function(err, result) {
                        callback(err, result.first_name);
                    });
                } else {
                    // er gibt keinen partner
                    callback(null);
                }
            }
        ], function(err, results){
            
            if(err) {
                //Es gab einen Datenbankfehler
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Überprüfen ob der in der URI spezifizierte user der owner oder der partner des dt_trips ist
            if(result.owner_user_id == req.params.user_id) {
                //er ist der owner
                res.json({
                    trip: {
                        date : result.date,
                        route_name : result.route_name
                    },
                    user: {
                        first_name : results[0],
                        sequence_id_target_station : result.owner_sequence_id_target_station,
                        sequence_id_departure_station : result.owner_sequence_id_departure_station,
                        departure_station_name : result.owner_departure_station_name,
                        target_station_name : result.owner_target_station_name,
                        arrival_time : result.owner_arrival_time,
                        departure_time : result.owner_departure_time
                    },
                    partner: {
                        first_name : results[1],
                        sequence_id_target_station : result.partner_sequence_id_target_station,
                        sequence_id_departure_station : result.partner_sequence_id_departure_station,
                        departure_station_name : result.partner_departure_station_name,
                        target_station_name : result.partner_target_station_name,
                        arrival_time : result.partner_arrival_time,
                        departure_time : result.partner_departure_time
                    }
                });
            } else {
                //er ist der partner
                res.json({
                    trip: {
                        date : result.date,
                        route_name : result.route_name
                    },
                    user: {
                        first_name : results[1],
                        sequence_id_target_station : result.partner_sequence_id_target_station,
                        sequence_id_departure_station : result.partner_sequence_id_departure_station,
                        departure_station_name : result.partner_departure_station_name,
                        target_station_name : result.partner_target_station_name,
                        arrival_time : result.partner_arrival_time,
                        departure_time : result.partner_departure_time
                    },
                    partner: {
                        first_name : results[0],
                        sequence_id_target_station : result.owner_sequence_id_target_station,
                        sequence_id_departure_station : result.owner_sequence_id_departure_station,
                        departure_station_name : result.owner_departure_station_name,
                        target_station_name : result.owner_target_station_name,
                        arrival_time : result.owner_arrival_time,
                        departure_time : result.owner_departure_time
                    }
                });
            }
        });
    });
}

//den user aus einem dt_trip entfernen, den dt_trip löschen wenn er nur einem user besitzt
//wenn vorhanden den zum dt_trip gehörigen Chatraum löschen
module.exports.removeDtTrip = function (req, res) {
    
    //den dt_trip in der Dt_trips Collection finden
    Dt_trips.findById(req.params.dt_trip_id, '-__v', function (err, result) {
        
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!result) {
            //Es wurde kein Trip mit der angegebenen dt_trip_id gefunden
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        
        //parallele löschen bzw. updaten des dt_trips und des Chatraumes
        async.parallel([
            
            //hier wird der dt_trip gelöscht/geupdated
            function(callback) {
                
                if(req.params.user_id == result.owner_user_id && !result.partner_user_id) {
                    //user ist der owner und es gibt keinen partner, der dt_trip kann gelöscht werden
                    Dt_trips.findByIdAndRemove(req.params.dt_trip_id, function (err, result) {
                        if(err) {
                            //Es gab einen Datenbankfehler beim löschen
                            return callback(err)
                        }
                        callback(null, 'successfully removed');
                    });
                
                } else if(req.params.user_id == result.owner_user_id && result.partner_user_id != null) {
                    //user ist der owner und es gibt einen partner, der partner wird zum neuem owner
                    Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, {
                        owner_user_id: result.partner_user_id,
                        owner_sequence_id_target_station: result.partner_sequence_id_target_station,
                        owner_sequence_id_departure_station: result.partner_sequence_id_departure_station,
                        owner_departure_station_name: result.partner_departure_station_name,
                        owner_target_station_name: result.partner_target_station_name,
                        owner_departure_time: result.partner_departure_time,
                        owner_arrival_time: result.partner_arrival_time,
                        has_season_ticket: !result.has_season_ticket,
                        partner_user_id: null,
                        partner_sequence_id_target_station: null,
                        partner_sequence_id_departure_station: null,
                        partner_departure_station_name: null,
                        partner_target_station_name: null,
                        partner_departure_time: null,
                        partner_arrival_time: null
                    }, function (err, result) {
                        if(err) {
                            //Es gab einen Datenbankfehler
                            return callback(err)
                        }
                        //Das updaten des dt_trips ist abgeschlossen und somit kann der callback aufgerufen werden
                        callback(null, 'successfully unmatched');
                        
                        //Der alte partner soll benachrichtigt werden das er nun alleine in dem dt_trip ist
                        //fcm token des alten partners ermitteln
                        Users.findById(result.partner_user_id, 'token', function(err, user) {
                            if (err) {
                                //Es gab einen Datenbankfehler
                                console.error(err);
                                return;
                            }
                            
                            if(user.token) {
                                //es wurde ein token gefunden
                                //Es wird eine fcm message erstellt
                                //Vorlage: https://github.com/ToothlessGear/node-gcm#usage
                                var message = new fcm.Message({
                                    //message wird mit den Informationen die zum aufrufen von /matches benötigt werden angereichert
                                    data: {
                                        type: 'delete',
                                        unique_trip_id: req.body.unique_trip_id,
                                        sequence_id_departure_station: result.owner_sequence_id_departure_station,
                                        sequence_id_target_station: result.owner_sequence_id_target_station,
                                        user_id: result.owner_user_id,
                                        has_season_ticket: result.has_season_ticket,
                                        departure_time: result.owner_departure_time,
                                        arrival_time: result.owner_arrival_time,
                                        departure_station_name: result.owner_departure_station_name,
                                        target_station_name: result.owner_target_station_name
                                    },
                                    notification: {
                                        title: 'DTSharing - Ein Partner hat sich ausgetragen',
                                        body: 'Einer deiner Partner hat sich ausgetragen. Suche jetzt nach einem neuen Partner.'
                                    }
                                });
                                
                                //Der sender wird mit dem fcm API key eingerichtet
                                var sender = new fcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                                //die Nachricht senden
                                sender.send(message, { registrationTokens: [user.token] }, function (err, response) {
                                    if(err) {
                                        //Es gab einen Fehler beim senden
                                        console.error(err);
                                    }
                                });
                            }
                        });
                    });
                    
                } else if(req.params.user_id == result.partner_user_id) {
                    //user ist der Partner, die Partnerinformationen sollen aus dem dt_trip gelöscht werden
                    Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, { 
                        partner_user_id: null,
                        partner_sequence_id_target_station: null,
                        partner_sequence_id_departure_station: null,
                        partner_departure_station_name: null,
                        partner_target_station_name: null,
                        partner_departure_time: null,
                        partner_arrival_time: null
                    }, function (err, result) {
                        if(err) {
                            //Es gab einen Datenbankfehler
                            return callback(err)
                        }
                        
                        //Das updaten des dt_trips ist abgeschlossen und somit kann der callback aufgerufen werden
                        callback(null, 'successfully unmatched');
                        
                        //Der alte owner soll benachrichtigt werden das er nun alleine in dem dt_trip ist
                        //fcm token des alten partners ermitteln
                        Users.findById(result.owner_user_id, 'token', function(err, user) {
                            if (err) {
                                //Es gab einen Datenbankfehler
                                console.error(err);
                                return;
                            }
                            
                            if(user.token) {
                                //es wurde ein token gefunden
                                //Es wird eine fcm message erstellt
                                //Vorlage: https://github.com/ToothlessGear/node-gcm#usage
                                var message = new fcm.Message({
                                    //message wird mit den Informationen die zum aufrufen von /matches benötigt werden angereichert
                                    data: {
                                        type: 'delete',
                                        unique_trip_id: result.unique_trip_id,
                                        sequence_id_departure_station: result.partner_sequence_id_departure_station,
                                        sequence_id_target_station: result.partner_sequence_id_target_station,
                                        user_id: result.partner_user_id,
                                        has_season_ticket: !result.has_season_ticket,
                                        departure_time: result.partner_departure_time,
                                        arrival_time: result.partner_arrival_time,
                                        departure_station_name: result.partner_departure_station_name,
                                        target_station_name: result.partner_target_station_name
                                    },
                                    notification: {
                                        title: 'DTSharing - Ein Partner hat sich ausgetragen',
                                        body: 'Einer deiner Partner hat sich ausgetragen. Suche jetzt nach einem neuen Partner.'
                                    }
                                });
                                
                                //Der sender wird mit dem fcm API key eingerichtet
                                var sender = new fcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                                //die Nachricht senden
                                sender.send(message, { registrationTokens: [user.token] }, function (err, response) {
                                    if(err) {
                                        //Es gab einen Fehler beim senden
                                        console.error(err);
                                    }
                                });
                            }
                        });
                    });
                }
                
            //Wenn vorhanden soll der zum dt_trip gehörige Chatraum gelöscht werden
            }, function(callback) {
                
                //Wenn es zwei user in dem dt_trip sind/waren muss der Chatraum gelöscht werden
                if(result.owner_user_id && result.partner_user_id ) {
                    //es gab zwei User, der Chat wird gelöscht
                    Chats.findOneAndRemove({dt_trip_id: req.params.dt_trip_id}, '_id', function (err, result) {
                        if(err) {
                            //Es gab einen Datenbankfehler
                            return callback(err)
                        }
                        
                        if(result) {
                            //Es wurde ein Chatraum gelöscht, die dazugehörigen Nachrichten sollen gelöscht werden
                            Messages.remove({chat_id: result._id}, function(err, results) {
                                if(err) {
                                    //Es gab einen Datenbankfehler
                                    return callback(err);
                                }
                                callback(null);
                            });
                        } else {
                            callback(null);
                        }
                    });
                } else {
                    callback(null);
                }
            }
            
        //diese Funktion wird aufgerufen nachdem dt_trip und wenn vorhanden der Chatraum gelöscht wurden
        ], function(err, results) {
            if(err) {
                //Es gab einen Datenbankfehler
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json({success_message: results[0]});
        })
    });
}