var express 	= require('express'),
	app			= express(),
	bodyParser 	= require('body-parser'),
	mongoose	= require('mongoose'),
	db_Controller = require('./js/controllers/db_Controller'),
	geocoder = require('geocoder'),
	https = require('https'),
	fs = require('fs');

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
app.get('/stations/nearby/:lat/:lon', db_Controller.stationsNearby);

app.get('/matches/:type/:unique_trip_id/:departure_sequence_id/:target_sequence_id', db_Controller.advancedMatching);

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

/*Testweise: Zugriff auf die API des VRS.
* XML-Request an die API. XML-Response als Antwort, welche weiterverarbeitet werden kann*/
app.get('/test/vrsapi', function (req, res) {
	
	/*Query auslesen und XML-Body mit zu suchendem String bauen*/
	var query = req.query.stop,
		body = '<?xml version="1.0" encoding="ISO-8859-15"?><Request><ObjectInfo><ObjectSearch><String>'+query+'</String><Classes><Stop/></Classes></ObjectSearch><Options><Output><SRSName>urn:adv:crs:ETRS89_UTM32</SRSName></Output></Options></ObjectInfo></Request>';

	/*HTTP-Request Optionen zuzüglich einbinden des VRS SSL-Zertifikats*/
	var options = {
    host: "apitest.vrsinfo.de",
    path: "/vrs/cgi/service/ass",
    port: 4443,
    method: "POST",
    ca: [fs.readFileSync('ssl/VRS-CA.cer')],
    headers: {
        'Content-Type': 'text/xml',
        'Content-Length': Buffer.byteLength(body)
    }
	};

	var vrs_request = https.request( options, function( vrs_response ){

   		console.log( vrs_response.statusCode );
		var buffer = "";
   		
   		/*Response kommt in Häppchen, deswegen wird sie hier nach und nach zusammengesetzt*/
   		vrs_response.on( "data", function( data ) {
	   		buffer = buffer + data; 
	   	});
	   	
	   	/*Response endet, Buffer somit vollständig und kann ausgegeben/weiterverarbeitet werden*/
	   	vrs_response.on( "end", function( data ) {
	   		res.writeHead(200, {'Content-Type': 'text/xml'});
	   		res.write(buffer);
	   		res.end();
	   	});

	});

	/*Fehler bei der Request*/
	vrs_request.on('error', function(e) {
	    console.log('Problem mit der Request: ' + e.message);
	});

	/*XML-Body an die API abschließend Ende*/
	vrs_request.write( body );
	vrs_request.end();
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});