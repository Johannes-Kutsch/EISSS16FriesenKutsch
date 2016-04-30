var Offer = require('../models/offer'),
	Search = require('../models/search'),
	Station = require('../models/station'),
	geolib = require('geolib');

/*Erstelle Eintrag in die Datenbank mit erhaltenen Reisedaten*/
module.exports.createEntry = function (req, res) {
	console.log(req.body);

	/*Unterscheide nach Typ*/
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

/*Rufe alle Reisedaten aus der Datenbank ab.*/
module.exports.getAllByType = function (req, res) {

	/*Unterscheide nach Typ*/
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

/*Rufe alle Reisedaten aus der Datenbank ab, welche gewisse Kriterien erfüllen (Matching)*/
module.exports.getMatchesByType = function (req, res) {
	var qStart = req.body.start,
		qDestination = req.body.destination,
		qTicket = req.body.ticket,
		qDate = req.body.date,
		qTime = req.body.time;

	/*Unterscheide nach Typ*/
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

/*Rufe alle Stationen aus der Datenbank ab und gib nur den Namen zurück*/
module.exports.getStations = function (req, res) {
	Station.find({}, 'stop_name -_id', function (err, results) {
	        	res.json(results);
	    	});
}

/*Ermittle alle Stationen in einem Radius von 2km um die erhaltenen GPS Informationen und gib
* nur die Namen zurück */
module.exports.getStationsNearby = function (req, res) {
	var user_lat = req.params.lat,
		user_lon = req.params.lon;

	Station.find({}, 'stop_name stop_lat stop_lon -_id', function (err, results) {
		var newResults = [];
		var obj = {};
		results.forEach(function(result){
			var inRange = geolib.isPointInCircle(
				{latitude: result.stop_lat, longitude: result.stop_lon},
				{latitude: user_lat, longitude: user_lon},
				2000
				);
			if(inRange)
				newResults.push({'stop_name': result.stop_name});
		});
		res.json(newResults);
	});
}
