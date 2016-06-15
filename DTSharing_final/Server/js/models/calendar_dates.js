var mongoose = require('mongoose');

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
