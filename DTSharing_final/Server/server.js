var express 	= require('express'),
	app			= express(),
	mongoose	= require('mongoose'),
	user_Controller = require('./js/controllers/user_Controller'),
    dt_Controller = require('./js/controllers/dt_Controller'),
    stop_Controller = require('./js/controllers/stop_Controller'),
    bodyParser	= require('body-parser'),
	https = require('https'),
	fs = require('fs');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

/*Ein neues Benutzerprofil erstellen*/
app.post('/users', user_Controller.register);

/*Ein Benutzerprofil abrufen*/
app.get('/users/:user_id', user_Controller.findUserProfile);

//Alle Stops Abrufen
app.get('/stops', stop_Controller.findStops);


app.get('/trips', dt_Controller.findTrips);


app.listen(3000, function() {
	console.log('Listening on port 3000...');
});