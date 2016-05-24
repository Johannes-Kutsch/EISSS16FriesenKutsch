var express 	= require('express'),
	app			= express(),
	bodyParser 	= require('body-parser'),
	mongoose	= require('mongoose'),
	db_Controller = require('./js/controllers/db_Controller'),
	geocoder = require('geocoder'),
	http = require('http');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

/*Rufe alle Daten nach Typ ab*/
app.get('/:type/get/all', db_Controller.getAllByType);

/*Rufe alle Stationen ab und gib die Namen zurück*/
app.get('/get/stations', db_Controller.getStations);

/*Ermittle Stationen im Umkreis von 2km um die erhaltene Position*/
app.get('/get/stations/nearby/:lat/:lon', db_Controller.getStationsNearby);
app.get('/stations/nearby/:lat/:lon', db_Controller.stationsNearby);

//app.get('/matches', db_Controller.advancedMatching);
app.get('/matches/:type/:unique_trip_id/:departure_sequence_id/:target_sequence_id', db_Controller.advancedMatching);
app.get('/evolveSchema', db_Controller.evolveSchema);


/*Trage erhaltene Reisedaten in die Datenbank ein*/
app.post('/:type/post/entry', db_Controller.createEntry);

/*Ermittle Matches anhand der erhaltenen Daten und gib diese zurück*/
app.post('/:type/get/matches', db_Controller.getMatchesByType);

/*Reverse Geocoding um einen Ort aus der Latitude und Longitude zu ermitteln
* Vorerst nur ein Versuch, welcher nicht über die Applikation angesprochen wird */
app.get('/location/:lat/:lon', function (req, res) {
	var lat = req.params.lat,
		lon = req.params.lon;
	
	geocoder.reverseGeocode( lat, lon, function ( err, data ) {
		var results = data.results,
			myAddress = {};
		for (i = 0; i < results.length; i++) {
  			myAddress[i] = results[i].formatted_address;
		}
  		res.json(myAddress);
	}, { language: 'de' });
});

app.get('/test/vrsapi/:stop', function (req, res) {
	var stop = req.params.stop;
	var body = '<?xml version="1.0" encoding="ISO-8859-15"?>'+
				'<Request>'+
  					'<ObjectInfo>'+
    					'<ObjectSearch>'+
        					'<String>'+stop+'</String>'+
      						'<Classes>'+
        						'<Stop/>'+
      						'</Classes>'+
    					'</ObjectSearch>'+
    					'<Options>'+
      						'<Output>'+
        						'<SRSName>urn:adv:crs:ETRS89_UTM32</SRSName>'+
        					'</Output>'+
    					'</Options>'+
					'</ObjectInfo>'+
				'</Request>';

	var postRequest = {
    host: "https://apitest.vrsinfo.de",
    path: "/vrs/cgi/service/ass",
    port: 4443,
    method: "POST",
    headers: {
        'Content-Type': 'text/xml',
        'Content-Length': Buffer.byteLength(body)
    }
	};

	var buffer = "";

	var req = http.request( postRequest, function( res )    {

	   console.log( res.statusCode );
	   var buffer = "";
	   res.on( "data", function( data ) { buffer = buffer + data; } );
	   res.on( "end", function( data ) { console.log( buffer ); } );

	});

	req.on('error', function(e) {
	    console.log('problem with request: ' + e.message);
	});

	req.write( body );
	req.end();
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});