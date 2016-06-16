var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    async = require('async'),
    mongoose = require('mongoose');
 
module.exports.offer = function (req, res) {
    var dt_trip = new Dt_trips({
        unique_trip_id: req.body.unique_trip_id,
        trip_id: req.body.trip_id,
        date: req.body.date,
        owner_departure_time: req.departure_time,
        owner_arrival_time: req.arrival_time,
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
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        res.json(result);
    });
}


module.exports.match = function (req,res) {
    Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, { 
        partner_user_id: req.body.user_id,
         
        partner_sequence_id_target_station: req.body.sequence_id_target_station,
        partner_sequence_id_departure_station: req.body.sequence_id_departure_station,
        partner_destionation_station_name: req.body.destination_station_name,
        partner_target_station_name: req.body.target_station_name
    }, function (err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        res.send({
                success_message: 'successfully matched'
            });
    });
}

module.exports.findDtTrips = function (req, res) {
    Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partne_user_id : req.params.user_id}]}, '-__v', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!results) {
            console.log('No Trips found | 404');
            res.status(404);
            res.send({
                error_message: 'No Trips found'
            });
            return;
        }
        res.json(results);
    });
}

module.exports.findDtTrip = function (req, res) {
    Dt_trips.findById(req.params.dt_trip_id, '-__v', function (err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        res.json(result);
    });
}

module.exports.removeDtTrip = function (req, res) {
    Dt_trips.findById(req.params.dt_trip_id, '-__v', function (err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('Trip not found | 404');
            res.status(404);
            res.send({
                error_message: 'Trip not found'
            });
            return;
        }
        if(req.params.user_id == result.owner_user_id) {
            Dt_trips.findByIdAndRemove(req.params.dt_trip_id, function (err, result) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.send({
                    success_message: 'successfully removed'
                });
            });
        } else if(req.params.user_id == result.partner_user_id) {
            Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, { 
                partner_user_id: null,
                partner_sequence_id_target_station: null,
                partner_sequence_id_departure_station: null,
                partner_destionation_station_name: null,
                partner_target_station_name: null
            }, function (err, result) {
                if(err) {
                    res.status(500);
                    res.send({
                        errorMessage: 'Database Error'
                    });
                    console.error(err);
                    return;
                }
                if(!result) {
                    console.log('Trip not found | 404');
                    res.status(404);
                    res.send({
                        errorMessage: 'Trip not found'
                    });
                    return;
                }
                res.send({
                        successMessage: 'successfully unmatched'
                    });
            });
        }
        else {
            console.log('User is not part of that Trip | 403');
            res.status(403);
            res.send({
                errorMessage: 'User is not part of that Trip'
            });
            return;
        }
    });
}