var Chats = require('../models/chats'),
    Messages = require('../models/messages'),
    async = require('async'),
    utils = require('../lib/utils'),
    gcm = require('node-gcm'),
    mongoose = require('mongoose');
 
module.exports.createChat = function (req, res) {
    var chat = new Chats({
        owner_user_id: req.params.user_id,
        partner_user_id: req.body.partner_user_id,
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
    Chats.find( {$or: [{owner_user_id : req.query.user_id}, {partner_user_id : req.query.user_id}]}, '-__v -key', function (err, results) {
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
        res.json(results);
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
            var message = new gcm.Message({
                data: {
                    key1: 'message1',
                    key2: 'message2'
                },
                notification: {
                    title: "Neuer Mitfahrer gefunden",
                    body: "Es wurde blablubbla ok lol iksdeh"
                }
            });
            var sender = new gcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
            var registrationTokens = [];
            registrationTokens.push('e7Nqcw-M0_Q:APA91bF-4suN1sAqw5s6hT-FJJnoQeBNqf1ekyQ4uN6uEOmgQRxLFjJW6V0TkXB8UniokaAasUrka6TyvGhKAC_9yIoM2_-gKb5ko_2ISTqSY4dsYU8VxJ5qZ7JwKpH_nqw-QSjaTjei');
            sender.send(message, { registrationTokens: registrationTokens }, function (err, response) {
              if(err) console.error(err);
              else    console.log(response);
            });
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