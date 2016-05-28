var mongoose = require('mongoose');

/*Datenbank Schema der Collection "stop_times" */
module.exports = mongoose.model('stop_times', {
	trip_id: Number,
    arrival_time: String,
    departure_time: String,
    stop_id: Number,
    stop_sequence: Number,
    stop_headsign: String,
    pickup_type: Number,
    drop_off_type: Number,
    shape_dist_traveled: String
});