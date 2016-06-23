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
                    Messages.findOne({chat_id : result._id}, 'sequence message_text', {sort:{sequence:-1}},function(err, result) {
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

module.exports.findChat = function (req, res) {
    Chats.findById(req.params.chat_id, 'owner_user_id partner_user_id', function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        var query;
        if(result.owner_user_id == req.params.user_id) {
            query = result.partner_user_id
        } else {
            query = result.owner_user_id
        }
        Users.findById(query, 'first_name last_name picture', function(err, result) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json({
                partner: result
            });
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
        var partner = results[1];
        var messages = results[0];
        res.json({
            partner: partner,
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