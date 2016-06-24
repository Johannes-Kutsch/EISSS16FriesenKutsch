var mongoose = require('mongoose');

//Datenbank Schema der Collection calendar_dates
//https://github.com/brendannee/node-gtfs/blob/master/models/CalendarDate.js
//wurde als Vorlage verwendet
module.exports = mongoose.model('calendarDates', {
  agency_key: {
    type: String,
    index: true
  },
  service_id: String,
  date: Number,
  exception_type: {
    type: Number,
    min: 1,
    max: 2
  }
});
