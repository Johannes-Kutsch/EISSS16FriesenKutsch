var mongoose = require('mongoose');

/*Datenbank Schema der Collection "stops" */
module.exports = mongoose.model('stops', {
	stop_id: Number,
    stop_code: String,
    stop_name: String,
    stop_desc: String,
    zone_id: String,
    stop_url: String,
    location_type: String,
    parent_station: String,
    stop_timezone: String,
    geo: {
        lat: Number,
        lon: Number,
        
    }
});