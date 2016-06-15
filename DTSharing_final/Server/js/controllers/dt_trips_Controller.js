var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    mongoose = require('mongoose');
 
module.exports.offer = function (req, res) {
    Users.findById(req.params.user_id, '-__v', function (err, result) {
        if(err) {
            res.status(444);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            res.status(404);
            res.send({
                errorMessage: 'User not found'
            });
            return;
        }
        var dt_trip = new Dt_trips({
            unique_trip_id: req.body.unique_trip_id,
            trip_id: req.body.trip_id,
            date: req.body.date,
            owner_user_id: req.params.user_id,
            owner_sequence_id_target_station: req.body.sequence_id_target_station,
            owner_sequence_id_departure_station: req.body.sequence_id_departure_station,
            owner_destination_station_name: req.body.destination_station_name,
            owner_target_station_name: req.body.target_station_name,
            has_season_ticket: req.body.has_season_ticket,
            partner_user_id: null,
            partner_sequence_id_target_station: null,
            partner_sequence_id_departure_station: null,
            partner_destionation_station_name: null,
            partner_target_station_name: null
        });
        dt_trip.save(function (err, result) {
            if(err) {
                res.status(444);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json(result);
        });
    });
    
    
}

module.exports.match = function (req,res) {
    Users.findById(req.params.user_id, '-__v', function (err, result) {
        if(err) {
            res.status(444);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            res.status(404);
            res.send({
                errorMessage: 'User not found'
            });
            return;
        }
        Dt_trips.findById(req.params.dt_trip_id, '-__v', function (err, result) {
            if(err) {
                res.status(444);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            if(!result) {
                res.status(404);
                res.send({
                    errorMessage: 'Trip not found'
                });
                return;
            }
            
            
        });
    });
}

module.exports.findDtTrips = function (req, res) {
    
}

module.exports.findDtTrip = function (req, res) {
    
}