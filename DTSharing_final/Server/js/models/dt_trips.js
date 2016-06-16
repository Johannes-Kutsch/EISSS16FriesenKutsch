var mongoose = require('mongoose');

/*Datenbank Schema der Collection "dt_trips" */
module.exports = mongoose.model('dt_trips', {
    unique_trip_id: String,
    trip_id: String,
    date: String,
    owner_departure_time: String,
    owner_arrival_time: String,
    owner_user_id: String,
    owner_sequence_id_target_station: Number,
    owner_sequence_id_departure_station: Number,
    owner_departure_station_name: String,
    owner_target_station_name: String,
    has_season_ticket: Boolean,
    partner_user_id: String,
    partner_departure_time: String,
    partner_arrival_time: String,
    partner_sequence_id_target_station: Number,
    partner_sequence_id_departure_station: Number,
    partner_departure_station_name: String,
    partner_target_station_name: String
});