var express 	= require('express'),
	app			= express(),
	mongoose	= require('mongoose'),
	users_Controller = require('./js/controllers/users_Controller'),
    trips_Controller = require('./js/controllers/trips_Controller'),
    stops_Controller = require('./js/controllers/stops_Controller'),
    ratings_Controller = require('./js/controllers/ratings_Controller'),
    dt_trips_Controller = require('./js/controllers/dt_trips_Controller'),
    matches_Controller = require('./js/controllers/matches_Controller'),
    sessions_Controller = require('./js/controllers/sessions_Controller'),
    chats_Controller = require('./js/controllers/chats_Controller'),
    bodyParser	= require('body-parser'),
	https = require('https'),
	fs = require('fs');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));



//Alle Stops Abrufen 
//Querry: stops_version
app.get('/stops', stops_Controller.findStops);


//Login
//Querry: email, pass, token
app.post('/sessions', sessions_Controller.login);


//Benutzer
//Boddy: birth_year,first_name,last_name,gender,interests,more,email,pass,
app.post('/users', users_Controller.register);

//URI: User ID des Nutzers
//Querry: user_version, picture_version
app.get('/users/:user_id', users_Controller.findUser);

//Boddy: interests, more, picture (nur die die geändert wurden)
app.put('/users/:user_id', users_Controller.updateUser);


//Ratings
//URI: User ID des Nutzers der bewertet wird
//Boddy: author_id, stars, comment, date
app.post('/users/:user_id/ratings', ratings_Controller.rate);

//URI: User ID des Nutzers
app.get('/users/:user_id/ratings', ratings_Controller.findRatings);


//dt_trips
//URI: user_id
//Boddy: unique_trip_id, trip_id, date, departure_time, arrival_time, sequence_id_target_station, sequence_id_departure_station, destination_station_name, target_station_name, has_season_ticket, route_name
app.post('/users/:user_id/dt_trips', dt_trips_Controller.offer);

//URI: user_id
app.get('/users/:user_id/dt_trips', dt_trips_Controller.findDtTrips);

//URI: user_id, dt_trip_id
app.get('/users/:user_id/dt_trips/:dt_trip_id', dt_trips_Controller.findDtTrip);

//URI: user_id des Benutzers der die Fahrt anbietet, dt_trip_id des Trips
//Boddy: user_id,sequence_id_target_station,sequence_id_departure_station,destination_station_name,target_station_name, key
app.put('/users/:user_id/dt_trips/:dt_trip_id', dt_trips_Controller.match);

//URI: user_id des Benutzers, dt_trip_id des Trips (löscht gesamten Tripp wenn "Eigentümer" ausführt, nur den Partner wenn er ausführt)
app.delete('/users/:user_id/dt_trips/:dt_trip_id', dt_trips_Controller.removeDtTrip);


//Chat
//URI: User ID des Nutzers der den Chatraum erstellt
//Body: partner_user_id, key, dt_trip_id
app.post('/users/:user_id/chats', chats_Controller.createChat);

//Uri: User ID des Nutzers
app.get('/users/:user_id/chats', chats_Controller.findChats);

//Uri: User ID des Nutzers
//Body: message_text,
app.post('/users/:user_id/chats/:chat_id/messages', chats_Controller.createMessage);

//Uri: User ID des Nutzers, Chat ID des Chats
//Query: sequence der letzten bekannten Nachricht (optional)
app.get('/users/:user_id/chats/:chat_id/messages', chats_Controller.findMessages);




//trips ermitteln
app.get('/trips', trips_Controller.findTrips);

//matches
app.get('/matches', matches_Controller.findMatches);



//Not Found
app.use(function(req, res, next) {
    res.status(404);
    res.send({ errorMessage: 'URI not found' });
    //return;
});

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});