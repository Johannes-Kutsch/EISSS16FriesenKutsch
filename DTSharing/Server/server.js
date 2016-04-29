var express 	= require('express'),
	app			= express(),
	bodyParser 	= require('body-parser'),
	mongoose	= require('mongoose'),
	db_Controller = require('./js/controllers/db_Controller'),
	geocoder = require('geocoder');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.get('/:type/get/all', db_Controller.getAllByType);
app.get('/get/stations', db_Controller.getStations);
app.get('/get/stations/nearby/:lat/:lon', db_Controller.getStationsNearby);

app.post('/:type/post/entry', db_Controller.createEntry);
app.post('/:type/get/matches', db_Controller.getMatchesByType);

app.get('/vrs/:from/:to', function (req, res) {
	var from = req.params.from,
		to = req.params.to;		
});

app.get('/location/:lat/:lon', function (req, res) {
	var lat = req.params.lat,
		lon = req.params.lon;
	
	//console.log("Lat: "+lat);
	//console.log("Lon: "+lon);

	// Setting language to German
	geocoder.reverseGeocode( lat, lon, function ( err, data ) {
		var results = data.results,
			myAddress = {};
		for (i = 0; i < results.length; i++) {
  			myAddress[i] = results[i].formatted_address;
		}
  		res.json(myAddress);
	}, { language: 'de' });
});

/*app.get('/getAll/:type', function (req, res) {
	if(req.params.type == "offers"){
		db_postController.getOffers;
	}else if(req.params.type == "searches"){
		db_postController.getSearches;
	}
});*/

//app.use('/js', express.static(__dirname + '/client/js'));
//app.use('/assets/js', express.static(__dirname + '/client/assets/js'));
//app.use('/css', express.static(__dirname + '/client/assets/css'));

//REST API
//app.get('/api/meetups', meetupsController.list);
//app.post('/api/meetups', meetupsController.create);

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});