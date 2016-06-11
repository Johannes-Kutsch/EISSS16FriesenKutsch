var mongoose = require('mongoose');

/*Datenbank Schema der Collection "messages" */
module.exports = mongoose.model('messages', {
	chat_id: Number,
    message_id: Number,
    author_id: Number,
    message_text: String,
    time: Number,
    date: Number
});