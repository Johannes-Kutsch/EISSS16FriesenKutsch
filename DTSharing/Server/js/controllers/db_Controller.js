var Offer = require('../models/offer');
var Search = require('../models/search');
var Station = require('../models/station');

module.exports.createEntry = function (req, res) {
	console.log(req.body);

	switch(req.params.type){

		case "offers":
			var offer = new Offer(req.body);
			offer.save(function (err, result) {
    			res.json(result);
			});
	    	break;

		case "searches":
			var search = new Search(req.body);
			search.save(function (err, result) {
    			res.json(result);
			});
	    	break;
	    	
		default:
	}
}

module.exports.getAllByType = function (req, res) {

	switch(req.params.type){

		case "offers":
			Offer.find({}, function (err, results) {
	        	res.json(results);
	    	});
	    	break;

		case "searches":
			Search.find({}, function (err, results) {
	        	res.json(results);
	    	});
	    	break;
	    	
		default:
	}
}

module.exports.getMatchesByType = function (req, res) {
	var qStart = req.body.start,
		qDestination = req.body.destination,
		qTicket = req.body.ticket,
		qDate = req.body.date,
		qTime = req.body.time;

	switch(req.params.type){

		case "offers":
			Offer.find({
				start: qStart,
				destination: qDestination,
				date: qDate
			}, function (err, results) {
	        	res.json(results);
	    	});
			break;

		case "searches":
			Search.find({
				start: qStart,
				destination: qDestination,
				date: qDate
			}, function (err, results) {
	        	res.json(results);
	    	});
			break;

	}
}

module.exports.getStations = function (req, res) {
	Station.find({/*'stop_name': {'$regex': 'Deutz'}*/}, 'stop_name -_id', function (err, results) {
	        	/*var newResults = [];
	        	results.forEach(function(results){
	        		newResults.push(results.stop_name);
	        	});*/
	        	res.json(results);
	    	});
}
