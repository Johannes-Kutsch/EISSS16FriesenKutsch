var Offer = require('../models/offer'),
	Search = require('../models/search'),
	Stops = require('../models/stops'),
	stop_times = require('../models/stop_times'),
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

module.exports.advancedMatching = function (req, res) {
	var unique_trip_id = req.params.unique_trip_id,
		departure_sequence_id = req.params.departure_sequence_id,
		target_sequence_id = req.params.target_sequence_id;

		switch(req.params.type){
			
			case "offers":

				Search.find( { unique_trip_id : unique_trip_id }, function (err, results) {

					var matches = [];
					
					results.forEach( function (result) {

						if ((departure_sequence_id >= result.departure_sequence_id) && (target_sequence_id <= result.target_sequence_id)){
							matches.push(result);
						}

					});
					
					res.json(matches);

				});

				break;

			case "searches":
				break;
		}
}

/*Rufe alle Stationen aus der Datenbank ab und gib nur den Namen zurück*/
module.exports.getStations = function (req, res) {
	Stops.find({}, 'stop_name -_id', function (err, results) {
	        	res.json(results); 
	    	});
}

/*Ermittle alle Stationen in einem Radius von 2km um die erhaltenen GPS Informationen und gib
* nur die Namen zurück */
module.exports.stationsNearby = function (req, res) {
	var user_lat = req.params.lat,
		user_lon = req.params.lon;

	Stops.find({}, 'stop_name stop_lat stop_lon -_id', function (err, results) {
		var inRangeObj = {};
		results.forEach(function(result){
			var inRange = geolib.isPointInCircle(
				{latitude: result.stop_lat, longitude: result.stop_lon},
				{latitude: user_lat, longitude: user_lon},
				2000
				);
			if(inRange){
				var name = result.stop_name,
					lat = result.stop_lat,
					lon = result.stop_lon;

				inRangeObj[name] = {latitude: lat, longitude: lon};
			}
		});
		var sortedByDistance = geolib.orderByDistance({latitude: user_lat, longitude: user_lon}, inRangeObj);
		res.json(sortedByDistance);
	});
}
