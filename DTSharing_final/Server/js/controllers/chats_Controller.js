var Chats = require('../models/chats'),
    Messages = require('../models/messages'),
    async = require('async'),
    utils = require('../lib/utils'),
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
        res.json(results);
    });
}

module.exports.createMessage = function (req, res) {
    var date = new Date();
    //Zeitzone anpassen, 2 Stunden in Ms Hinzufügen
    date.setTime(date.getTime()+7200000);
    var message = new Messages({
        author_user_id: req.params.user_id,
        chat_id: req.params.chat_id,
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
    });
}

module.exports.findMessages = function (req, res) {
    Messages.find({chat_id : req.params.chat_id}, '-__v', function(err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        res.json(results);
    });
}