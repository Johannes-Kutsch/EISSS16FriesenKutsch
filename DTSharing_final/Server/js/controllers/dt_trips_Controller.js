var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    async = require('async'),
    mongoose = require('mongoose');
 
module.exports.offer = function (req, res) {
    Users.findById(req.params.user_id, function (err, result) {
        if(err) {
            res.status(500);
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
                res.status(500);
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

//Evtl noch eine Überpürung der Eingabe (ob die beiden wirklich zueinander passen!)
module.exports.match = function (req,res) {
    var error;
    async.parallel([
        function(callback) {
            Users.findById(req.params.user_id, function (err, result) {
                if(!result) {
                    if(!error) {
                        res.status(404);
                        error = 'Partner not found'
                    }
                }
                callback(err);
            });
        }, function(callback) {
            Users.findById(req.body.user_id, function (err, result) {
                if(!result) {
                    if(!error) {
                        res.status(404);
                        error = 'User not found'
                    }
                }
                callback(err);
            }); 
        }
    ],
    function(err, results){
        if(err) {
            res.status(500);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(error) {
            res.send({errorMessage: error});
            return;
        }
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
            res.send({
                    successMessage: 'successfully matched'
                });
        });
    });
}

module.exports.findDtTrips = function (req, res) {
    Users.findById(req.params.user_id, function (err, result) {
        if(err) {
            res.status(500);
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
        Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partne_user_id : req.params.user_id}]}, '-__v', function (err, results) {
            if(err) {
                res.status(500);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            if(!results) {
                res.status(404);
                res.send({
                    errorMessage: 'No Trips found'
                });
                return;
            }
            res.json(results);
        });
    });
}

module.exports.findDtTrip = function (req, res) {
    Users.findById(req.params.user_id, function (err, result) {
        if(err) {
            res.status(500);
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
                res.status(500);
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
            res.json(result);
        });
    });
}

module.exports.removeDtTrip = function (req, res) {
    Users.findById(req.params.user_id, '-__v', function (err, result) {
        if(err) {
            res.status(500);
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
                res.status(500);
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
            if(req.params.user_id == result.owner_user_id) {
                Dt_trips.findByIdAndRemove(req.params.dt_trip_id, function (err, result) {
                if(err) {
                    res.status(500);
                    res.send({
                        errorMessage: 'Database Error'
                    });
                    console.error(err);
                    return;
                }
                res.send({
                        successMessage: 'successfully removed'
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
                res.status(403);
                res.send({
                    errorMessage: 'User is not part of that Trip'
                });
                return;
            }
        });
    });
}