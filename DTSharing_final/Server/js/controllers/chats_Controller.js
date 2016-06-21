var Chats = require('../models/chats'),
    Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    Messages = require('../models/messages'),
    async = require('async'),
    utils = require('../lib/utils'),
    gcm = require('node-gcm'),
    mongoose = require('mongoose');
 
module.exports.createChat = function (req, res) {
    var chat = new Chats({
        owner_user_id: req.params.user_id,
        partner_user_id: req.body.partner_user_id,
        dt_trip_id: req.body.dt_trip_id,
        key: req.body.key
    });
    chat.save(function (err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        res.status(201);
        res.send({
            success_message: 'chat created'
        });
    });
}

module.exports.findChats = function (req, res) {
    Chats.find( {$or: [{owner_user_id : req.params.user_id}, {partner_user_id : req.params.user_id}]}, '-__v -key', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!results.length) {
            console.log('No Chats for this User | 404');
            res.status(404);
            res.send({
                error_message: 'No Chats for this User'
            });
            return;
        }
        var chats = [];
        async.each(results, function(result, callback) {
            async.parallel([
                function(callback) {
                    Dt_trips.findById(result.dt_trip_id, 'date partner_user_id partner_departure_station_name partner_target_station_name owner_user_id owner_departure_station_name owner_target_station_name', function(err, result) {
                        callback(err, result);
                    });
                }, function(callback) {
                    if(req.params.user_id == result.owner_user_id) {
                        Users.findById(result.partner_user_id, 'first_name last_name picture', function(err, result) {
                            callback(err, result);
                        });
                    } else if (req.params.user_id == result.partner_user_id) {
                        Users.findById(result.owner_user_id, function(err, result) {
                            callback(err, result);
                        });
                    }
                }, function(callback) {
                    Messages.findOne({chat_id : req.params.chat_id}, 'sequence message_text', {sort:{sequence:-1}},function(err, result) {
                        if(err) {
                            callback(err);
                        } else if (result) {
                            callback(null, result.message_text);
                        } else {
                            callback(null, null);
                        }
                    });
                }
            ],function(err, results) {
                if(err) {
                    return callback(err);
                }
                var chat = {
                    _id : result._id,
                    date : results[0].date,
                    first_name : results[1].first_name,
                    last_name : results[1].last_name,
                    picture : results[1].picture,
                    last_message : results[2]
                }
                if(req.params.user_id == results[0].partner_user_id) {
                    chat.departure_station_name = results[0].partner_departure_station_name;
                    chat.target_station_name = results[0].partner_target_station_name;
                } else if(req.params.user_id == results[0].owner_user_id) {
                    chat.departure_station_name = results[0].owner_departure_station_name;
                    chat.target_station_name = results[0].owner_target_station_name;
                }
                chats.push(chat);
                callback(null);
            });
        },function(err) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json(chats);
        });
        
    });
}

module.exports.createMessage = function (req, res) {
    var sequence = 0;
    Messages.findOne({chat_id : req.params.chat_id}, 'sequence', {sort:{sequence:-1}},function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        } else if(result) {
            sequence = result.sequence+1;
        }
        var date = new Date();
        //Zeitzone anpassen, 2 Stunden in Ms Hinzufügen
        date.setTime(date.getTime()+7200000);
        var message = new Messages({
            author_id: req.params.user_id,
            chat_id: req.params.chat_id,
            sequence : sequence,
            message_text: req.body.message_text,
            time: date.toJSON().slice(11,16),
            date: utils.formatDay(new Date())
        });
        message.save(function (err, result) {
            if(err) {
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
            
            
            /*var message = new gcm.Message({
                data: {
                    chat_id: req.params.chat_id
                },
                notification: {
                    title: "Neue Nachricht",
                    body: req.params.chat_id
                }
            });
            var sender = new gcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
            sender.send(message, { registrationTokens: ['fdMB5Z8bc8I:APA91bH1SqecGsPuIvu__qhJyNNYZPO7m7Dp6hL2S6Md32Yf8yWltopW897FQBJVttdgK_kQjvyNvxSBZxJUD05rd_8tYclXmHUy8RQ_UYbEn6XGj1u6807OIPrpTncKsc8RXOBcEXGt'] }, function (err, response) {
              if(err) console.error(err);
              else    console.log(response);
            });*/
        });
    });
}

module.exports.findMessages = function (req, res) {
    var query = {chat_id : req.params.chat_id};
    if(req.query.sequence) {
        query.sequence = {$gt : req.query.sequence};
    }
    Messages.find(query, '-__v -_id -chat_id', {sort:{sequence:1}},function(err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!results.length) {
            console.log('No (new) Messages | 404');
            res.status(404);
            res.send({
                error_message: 'No (new) Messages'
            });
            return;
        }
        res.json(results);
    });
}