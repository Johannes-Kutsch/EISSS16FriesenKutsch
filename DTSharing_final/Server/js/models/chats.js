var mongoose = require('mongoose');

/*Datenbank Schema der Collection "chats" */
module.exports = mongoose.model('chats', {
    owner_user_id: String,
    partner_user_id: String,
    dt_trip_id: String,
    key: String
});