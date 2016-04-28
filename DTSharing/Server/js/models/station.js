var mongoose = require('mongoose');

module.exports = mongoose.model('stops', {
	stop_id: Number,
    stop_code: String,
    stop_name: String,
    stop_desc: String,
    stop_lat: String,
    stop_lon: String,
    zone_id: String,
    stop_url: String,
    location_type: String,
    parent_station: String,
    stop_timezone: String
});