var mongoose = require('mongoose');

/*Datenbank Schema der Collection "Search" */
module.exports = mongoose.model('Offer', {
	user_id: Number,
    trip_id: Number,
    unique_trip_id: Number,
    departure_sequence_id: Number,
    target_sequence_id: Number
});