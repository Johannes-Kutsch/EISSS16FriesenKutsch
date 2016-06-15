var mongoose = require('mongoose');

/*Datenbank Schema der Collection "dt_trips" */
module.exports = mongoose.model('dt_trips', {
    unique_trip_id: String,
    trip_id: String,
    date: String,
    owner_user_id: String,
    owner_sequence_id_target_station: Number,
    owner_sequence_id_departure_station: Number,
    owner_destination_station_name: String,
    owner_target_station_name: String,
    has_season_ticket: Boolean,
    partner_user_id: String,
    partner_sequence_id_target_station: Number,
    partner_sequence_id_departure_station: Number,
    partner_destionation_station_name: String,
    partner_target_station_name: String
});