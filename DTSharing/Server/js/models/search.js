var mongoose = require('mongoose');

/*Datenbank Schema der Collection "Search" */
module.exports = mongoose.model('Search', {
	radius: String,
    start: String,
    destination: String,
    time: String,
    date: String,
    ticket: String
});