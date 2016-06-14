var express 	= require('express'),
	app			= express(),
	mongoose	= require('mongoose'),
	user_Controller = require('./js/controllers/user_Controller'),
    dt_Controller = require('./js/controllers/dt_Controller'),
    stop_Controller = require('./js/controllers/stop_Controller'),
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
app.get('/stops', stop_Controller.findStops);

//Benutzer
app.post('/users', user_Controller.register);
app.get('/users/:userID', user_Controller.findUser);

//Ratings
app.post('/users/:userID/ratings', ratings_Controller.rate);
app.get('/users/:userID/ratings', ratings_Controller.findRating);

//trips ermitteln
app.get('/trips', dt_Controller.findTrips);

//Not Found
app.use(function(req, res, next) {
    res.status(404);
    res.send({ errorMessage: 'URI not found' });
    //return;
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});