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