var Chats = require('../models/chats'),
    Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    Messages = require('../models/messages'),
    async = require('async'),
    utils = require('../lib/utils'),
    gcm = require('node-gcm'),
    mongoose = require('mongoose');


//erstelle einen Chatraum
module.exports.createChat = function (req, res) {
    
    //Variablen des Chatraumes aus dem Request-Body und der Uri ermitteln
    var chat = new Chats({
        owner_user_id: req.params.user_id,
        partner_user_id: req.body.partner_user_id,
        dt_trip_id: req.body.dt_trip_id,
        key: req.body.key
    });
    
    //Den neuen Chatraum in der chats-collection speichern
    chat.save(function (err, result) {
        
        if(err) {
            //Es gab einen Fehler während des speicherns
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //Der Chatraum wurde erfolgreich angelegt
        res.status(201);
        res.send({
            success_message: 'chat created'
        });
    });
}


//Details zu allen Chaträumen die zu einem Benutzer gehöhren ermitteln
module.exports.findChats = function (req, res) {
    
    //In der chats-collection alle Chaträume finden in denen der User als owner oder partner eingetragen ist
    Chats.find( {$or: [{owner_user_id : req.params.user_id}, {partner_user_id : req.params.user_id}]}, '-__v -key', function (err, results) {
        
        if(err) {
            //Es gab einen Fehler während der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!results.length) {
            //Es wurden keine Chaträume für den User gefunden
            console.log('No Chats for this User | 404');
            res.status(404);
            res.send({
                error_message: 'No Chats for this User'
            });
            return;
        }
        
        //Array in das die Chaträume gespeichert werden sollen
        var chats = [];
        
        //Alle gefundenen Chaträume werden durchgegangen und mit Daten welche zum Darstellen dieser Räume gebraucht werden angereichert
        async.each(results, function(result, callback) {
            
            //Die Datenbankabfragen können parallel ausgeführt werden
            async.parallel([
                
                //Es werden Informationen über den dt_trip der zu dem Chatraum gehört ermittelt
                function(callback) {
                    Dt_trips.findById(result.dt_trip_id, 'date partner_user_id partner_departure_station_name partner_target_station_name owner_user_id owner_departure_station_name owner_target_station_name', function(err, result) {
                        //Das Errorobject und/oder das result werden weitergegeben, err ist null wenn kein Fehler aufgetreten ist
                        callback(err, result);
                    });
                    
                //Es werden Informationen über den Chatpartner ermittelt
                }, function(callback) {
                    if(req.params.user_id == result.owner_user_id) {
                        //Chatpartner ist der partner des Chatraumes
                        Users.findById(result.partner_user_id, 'first_name last_name picture', function(err, result) {
                            //Das Errorobject und/oder das result werden weitergegeben, err ist null wenn kein Fehler aufgetreten ist
                            callback(err, result);
                        });
                    } else if (req.params.user_id == result.partner_user_id) {
                        //Chatpartner ist der owner der Chatraumes
                        Users.findById(result.owner_user_id, function(err, result) {
                            //Das Errorobject und/oder das result werden weitergegeben, err ist null wenn kein Fehler aufgetreten ist
                            callback(err, result);
                        });
                    }
                    
                //Es wird die zuletzt geschriebene Nachricht innerhalb des Chatraumes ermittelt
                }, function(callback) {
                    Messages.findOne({chat_id : result._id}, 'sequence message_text', {sort:{sequence:-1}},function(err, result) {
                        if(err) {
                            //Es gab einen Fehler während der Datenbankabfrage
                            //Das Errorobject wird weitergegeben
                            callback(err);
                        } else if (result) {
                            //Es wurde eine Nachricht gefunden
                            //Der Inhalt der Nachricht wird weitergegeben
                            callback(null, result.message_text);
                        } else {
                            //Es wurde keine Nachricht gefunden
                            //Es wird nix weitergegeben
                            callback(null, null);
                        }
                    });
                }
                
            //Diese function wird ausgeführt nachdem alle Datenbankabfragen für einen Chatraum durchgeführt wurden
            //err sind Fehler falls welche aufgetreten sind, ansonsten ist es null
            //results ist ein Array in welchem die weitergegebenen Ergebnisse der Parallelen Datenbankabfragen gespeichert sind
            ],function(err, results) {
                
                if(err) {
                    //Bei einer der Datenbankabfragen trat ein Fehler auf, dieser wird weitergegeben
                    return callback(err);
                }
                
                //Es wird ein Chatobject erstellt und mit Daten die diesen Chat betreffen angereichert
                var chat = {
                    _id : result._id,
                    date : results[0].date,
                    first_name : results[1].first_name,
                    last_name : results[1].last_name,
                    picture : results[1].picture,
                    last_message : results[2]
                }
                
                //Es wird geschaut ob der User partner oder owner des dt_trips war
                //Die Haltestellennamen der Haltestellen des Users sollen in dem Chatobject gespeichert werden
                if(req.params.user_id == results[0].partner_user_id) {
                    //user ist partner des dt_trips
                    chat.departure_station_name = results[0].partner_departure_station_name;
                    chat.target_station_name = results[0].partner_target_station_name;
                } else if(req.params.user_id == results[0].owner_user_id) {
                    //user ist owner des dt_trips
                    chat.departure_station_name = results[0].owner_departure_station_name;
                    chat.target_station_name = results[0].owner_target_station_name;
                }
                
                //Das Chatobject wird in das array chats gepushed
                chats.push(chat);
                
                //Es traten keine Fehler auf
                callback(null);
            });
            
        //Diese function wird aufgerufen sobald alle Chaträume durchlaufen wurden
        },function(err) {
            
            if(err) {
                //Es trat ein Fehler bei einer Datenbankabfrage auf
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Das chats Array wird als response übermittelt
            res.json(chats);
        });
        
    });
}

//Informationen über den Chat-Partner eines Chatraumes ermitteln
module.exports.findPartner = function (req, res) {
    
    //Den richtigen Chatraum finden
    Chats.findById(req.params.chat_id, 'owner_user_id partner_user_id', function(err, result) {
        
        if(err) {
            //Fehler während der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //Query das für die Ermittlung der Daten des Chat-Partners benutzt wird
        var query;
        if(result.owner_user_id == req.params.user_id) {
            //Der owner des Chatraums stellt die Anfrage
            //Es müssen also die Daten des partners ermittelt werden
            query = result.partner_user_id
        } else {
            //Der partner des Chatraumes stellt die Anfrage
            //Es müssen also die Daten des owners ermittelt werden
            query = result.owner_user_id
        }
        
        //Informationen in der Users collection ermitteln
        Users.findById(query, 'first_name last_name picture', function(err, result) {
            
            if(err) {
                //Fehler während der Datenbankabfrage
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Informationen als response übermitteln
            res.json({
                partner: result
            });
        });
    });
}

//Eine Nachricht in einem Chat erstellen
module.exports.createMessage = function (req, res) {
    
    //sequence gibt an als wievielte Nachricht diese Nachricht im Chatverlauf geschrieben wurde
    //sequence ist 0 da davon ausgegangen wird, dass es sich um die erste Nachricht in diesem Chatraum handelt
    var sequence = 0;
    
    //Es wird eine Nachricht aus dem selbem Chat mit der höchsten sequence ermittelt
    Messages.findOne({chat_id : req.params.chat_id}, 'sequence', {sort:{sequence:-1}},function(err, result) {
        
        if(err) {
            //Fehler während der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
            
        } else if(result) {
            //Es wurde bereits eine Nachricht in diesem Chatraum geschrieben, die sequence der neuen Nachricht wird entsprechend angepasst
            sequence = result.sequence+1;
        }
        
        //Es wird ein Dateobject erzeugt welches das aktuelle Datum wiederspiegelt.
        var date = new Date();
        //Die Zeitzone von date muss angepasst werden, es werden 2 Stunden in Millisekunden hinzugefügt
        date.setTime(date.getTime()+7200000);
        
        //Die neue Nachricht wird mit Daten gefüllt
        var message = new Messages({
            author_id: req.params.user_id,
            chat_id: req.params.chat_id,
            sequence : sequence,
            message_text: req.body.message_text,
            time: date.toJSON().slice(11,16),
            date: utils.formatDay(date)
        });
        //Die neue Nachricht wird gespeichert
        message.save(function (err, result) {
            
            if(err) {
                //Fehler während der Datenbankabfrage
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.status(201);
            res.send({
                success_message: 'message created'
            });
            var message_id = result._id;
            Chats.findById(req.params.chat_id, 'owner_user_id partner_user_id', function(err, result) {
                if(err) {
                    console.error(err);
                    return;
                }
                var receiver_user_id;
                if(result.owner_user_id == req.params.user_id) {
                    receiver_user_id = result.partner_user_id;
                } else if(result.partner_user_id == req.params.user_id) {
                    receiver_user_id = result.owner_user_id;
                }
                Users.findById(receiver_user_id, 'token', function(err, result) {
                    if(err) {
                        console.error(err);
                        return;
                    }
                    if(result.token) {
                        console.log(message_id);
                        var message = new gcm.Message({
                            data: {
                                type: 'chat_message',
                                chat_id: req.params.chat_id,
                                message_id : message_id
                            },
                            notification: {
                                title: 'DTSharing - Chat',
                                body: 'Einer deiner Mitfahrer hat dir etwas geschrieben.'
                            }
                        });
                        var sender = new gcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                        sender.send(message, { registrationTokens: [result.token] }, function (err, response) {
                            if(err) {
                                console.error(err);
                            }
                        });
                    }
                });
            });
        });
    });
}

module.exports.findMessages = function (req, res) {
    async.parallel([
        function(callback) {
            var query = {chat_id : req.params.chat_id};
            if(req.query.sequence) {
                query.sequence = {$gte : req.query.sequence};
            }
            Messages.find(query, '-__v -_id -chat_id', {sort:{sequence:1}},function(err, results) {
                if (err) {
                    return callback(err);
                }
                callback(null, results);
            });
        }, function(callback) {
            Chats.findById(req.params.chat_id, 'owner_user_id partner_user_id', function(err, result) {
                if(err) {
                    return callback(err);
                }
                var query;
                if(result.owner_user_id == req.params.user_id) {
                    query = result.partner_user_id
                } else {
                    query = result.owner_user_id
                }
                Users.findById(query, 'first_name last_name picture', function(err, result) {
                    if(err) {
                        return callback(err);
                    }
                    callback(null, result);
                });
            });
        }, function(callback) {
            Chats.findById(req.params.chat_id, 'dt_trip_id', function(err, result) {
                if(err) {
                    return callback(err);
                }
                Dt_trips.findById(result.dt_trip_id, 'date owner_arrival_time partner_arrival_time', function(err, result) {
                    if(err) {
                        return callback(err);
                    }
                    var arrival_time;
                    if(utils.timeToSeconds(result.owner_arrival_time) >= utils.timeToSeconds(result.partner_arrival_time)) {
                        arrival_time = utils.timeToSeconds(result.owner_arrival_time);
                    } else {
                        arrival_time = utils.timeToSeconds(result.partner_arrival_time)
                    }
                    var date = utils.formatDate(result.date);
                    date.setSeconds(arrival_time);
                    if(date < new Date()) {
                        Ratings.findOne({chat_id: req.params.chat_id, author_id: req.params.user_id}, function(err, result) {
                            if(err) {
                                return callback(err);
                            }
                            if(result) {
                                callback(null, true);
                            } else {
                                callback(null, false);
                            }
                        });
                    } else {
                        callback(null);
                    }
                });
            });
        }
    ], function(err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!results[0].length) {
            console.log('No (new) Messages | 404');
            res.status(404);
            res.send({
                error_message: 'No (new) Messages'
            });
            return;
        }
        var has_voted = results[2]
        var partner = results[1];
        var messages = results[0];
        res.json({
            partner: partner,
            has_voted: has_voted,
            messages: messages
        });
    });
}

module.exports.findMessage = function (req, res) {
    Messages.findById(req.params.message_id, '-_id -__v', function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('Message not found | 404');
            res.status(404);
            res.send({
                error_message: 'Message not found'
            });
            return;
        }
        res.send(result);
    });
}

module.exports.findKey = function (req, res) {
    Chats.findById(req.params.chat_id, '-_id key', function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('Key not found | 404');
            res.status(404);
            res.send({
                error_message: 'Key not found'
            });
            return;
        }
        res.send(result);
    });
}