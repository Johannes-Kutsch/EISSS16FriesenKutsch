var mongoose = require('mongoose');
var utils = require('../lib/utils');

/*Datenbank Schema der Collection "stop_times" */
module.exports = mongoose.model('stoptimes', {
  agency_key: {
    type: String,
    index: true
  },
  trip_id: {
    type: String,
    ref: 'Trips',
    index: true
  },
  arrival_time: {
    type: String,
    get: utils.timeToSeconds,
    set: utils.secondsToTime
  },
  departure_time: {
    type: String,
    index: true,
    get:  utils.timeToSeconds,
    set:  utils.secondsToTime
  },
  stop_id: String,
  stop_sequence: {
    type: Number,
    index: true
  },
  stop_headsign: String,
  pickup_type: {
    type: Number,
    index: true,
    min: 0,
    max: 3
  },
  drop_off_type: {
    type: Number,
    index: true,
    min: 0,
    max: 3
  },
  shape_dist_traveled: Number,
  timepoint: {
    type: Number,
    min: 0,
    max: 1
  }
});