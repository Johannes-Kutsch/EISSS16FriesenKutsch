var express 	= require('express'),
	app			= express(),
	mongoose	= require('mongoose'),
	user_Controller = require('./js/controllers/users_Controller'),
    trip_Controller = require('./js/controllers/trips_Controller'),
    stop_Controller = require('./js/controllers/stops_Controller'),
    ratings_Controller = require('./js/controllers/ratings_Controller'),
    bodyParser	= require('body-parser'),
	https = require('https'),
	fs = require('fs');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));



//Platzhalter
//Alle Stops Abrufen 
app.get('/stops', stops_Controller.findStops);

//Benutzer
app.post('/users', users_Controller.register);
app.get('/users/:userID', users_Controller.findUser);

//Ratings
app.post('/users/:userID/ratings', ratings_Controller.rate);
app.get('/users/:userID/ratings', ratings_Controller.findRating);

//trips ermitteln
app.get('/trips', trips_Controller.findTrips);

//Not Found
app.use(function(req, res, next) {
    res.status(404);
    res.send({ errorMessage: 'URI not found' });
    //return;
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});