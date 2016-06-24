var mongoose = require('mongoose');

//Datenbank Schema der Collection calendars
//https://github.com/brendannee/node-gtfs/blob/master/models/Calendar.js
//wurde als Vorlage verwendet
module.exports = mongoose.model('calendars', {
  agency_key: {
    type: String,
    index: true
  },
  service_id: String,
  monday: {
    type: Number,
    min: 0,
    max: 1
  },
  tuesday: {
    type: Number,
    min: 0,
    max: 1
  },
  wednesday: {
    type: Number,
    min: 0,
    max: 1
  },
  thursday: {
    type: Number,
    min: 0,
    max: 1
  },
  friday: {
    type: Number,
    min: 0,
    max: 1
  },
  saturday: {
    type: Number,
    min: 0,
    max: 1
  },
  sunday: {
    type: Number,
    min: 0,
    max: 1
  },
  start_date: Number,
  end_date: Number
});
