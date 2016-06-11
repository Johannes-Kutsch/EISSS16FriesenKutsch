var mongoose = require('mongoose');

/*Datenbank Schema der Collection "chats" */
module.exports = mongoose.model('chats', {
	chat_id: Number,
    owner_user_id: Number,
    partner_user_id: Number,
    key: String
});