var express 	= require('express'),
	app			= express(),
	mongoose	= require('mongoose'),
	users_Controller = require('./js/controllers/users_Controller'),
    trips_Controller = require('./js/controllers/trips_Controller'),
    stops_Controller = require('./js/controllers/stops_Controller'),
    ratings_Controller = require('./js/controllers/ratings_Controller'),
    dt_trips_Controller = require('./js/controllers/dt_trips_Controller'),
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
app.get('/users/:user_id', users_Controller.findUser);

//Ratings
app.post('/users/:user_id/ratings', ratings_Controller.rate);
app.get('/users/:user_id/ratings', ratings_Controller.findRating);

//trips ermitteln
app.get('/trips', trips_Controller.findTrips);

//dt_trips
app.post('/users/:user_id/dt_trips', dt_trips_Controller.offer);
app.get('/users/:user_id/dt_trips', dt_trips_Controller.findDtTrips);
app.get('/users/:user_id/dt_trips/:dt_trip_id', dt_trips_Controller.findDtTrip);

//Not Found
app.use(function(req, res, next) {
    res.status(404);
    res.send({ errorMessage: 'URI not found' });
    //return;
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});