var mongoose = require('mongoose');

module.exports = mongoose.model('Search', {
	radius: String,
    start: String,
    destination: String,
    time: String,
    date: String,
    ticket: String
});