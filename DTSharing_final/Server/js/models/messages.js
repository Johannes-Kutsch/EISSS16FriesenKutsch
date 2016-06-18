var mongoose = require('mongoose');

/*Datenbank Schema der Collection "messages" */
module.exports = mongoose.model('messages', {
	chat_id: String,
    author_id: String,
    message_text: String,
    time: String,
    date: String
});