var mongoose = require('mongoose');

//Datenbank Schema der Collection routes
//https://github.com/brendannee/node-gtfs/blob/master/models/Route.js
//wurde als Vorlage verwendet
module.exports = mongoose.model('routes', {
  agency_key: {
    type: String,
    index: true
  },
  route_id: String,
  agency_id: String,
  route_short_name: String,
  route_long_name: String,
  route_desc: String,
  route_type: {
    type: Number,
    min: 0,
    max: 7
  },
  route_url: String,
  route_color: String,
  route_text_color: String
});
