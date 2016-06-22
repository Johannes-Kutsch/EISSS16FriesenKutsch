var Users = require('../models/users'),
    Chats = require('../models/chats'),
    Dt_trips = require('../models/dt_trips'),
    async = require('async'),
    gcm = require('node-gcm'),
    mongoose = require('mongoose');
 
module.exports.offer = function (req, res) {
    var dt_trip = new Dt_trips({
        unique_trip_id: req.body.unique_trip_id,
        trip_id: req.body.trip_id,
        date: req.body.date,
        route_name: req.body.route_name,
        owner_departure_time: req.body.departure_time,
        owner_arrival_time: req.body.arrival_time,
        owner_user_id: req.params.user_id,
        owner_sequence_id_target_station: req.body.sequence_id_target_station,
        owner_sequence_id_departure_station: req.body.sequence_id_departure_station,
        owner_departure_station_name: req.body.departure_station_name,
        owner_target_station_name: req.body.target_station_name,
        has_season_ticket: req.body.has_season_ticket,
        partner_user_id: null,
        partner_sequence_id_target_station: null,
        partner_sequence_id_departure_station: null,
        partner_departure_station_name: null,
        partner_target_station_name: null,
        partner_departure_time: null,
        partner_arrival_time: null
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
        res.status(201);
        res.send({
            success_message: 'Offer sucessfull'
        });
        var query;
        if(req.body.has_season_ticket == 'true') {
            query = {
                owner_user_id : {$ne: req.params.user_id},
                unique_trip_id : req.body.unique_trip_id, 
                has_season_ticket : false, 
                owner_sequence_id_departure_station : {$gte: req.body.sequence_id_departure_station}, 
                owner_sequence_id_target_station : {$lte: req.body.sequence_id_target_station},
                partner_user_id : null
            }
        } else {
            query = {
                owner_user_id : {$ne: req.params.user_id},
                unique_trip_id : req.body.unique_trip_id, 
                has_season_ticket : true, 
                owner_sequence_id_departure_station : {$lte: req.body.sequence_id_departure_station}, 
                owner_sequence_id_target_station : {$gte: req.body.sequence_id_target_station},
                partner_user_id : null
            }
        }
        Dt_trips.find(query, '_id owner_user_id has_season_ticket owner_sequence_id_departure_station owner_sequence_id_target_station owner_departure_time owner_arrival_time owner_departure_station_name owner_target_station_name', function (err, results) {
            async.each(results, function(result, callback) {
                Users.findById(result.owner_user_id, 'token', function(err, result) {
                    if (err) {
                        console.error(err);
                        return;
                    }
                    console.log(result);
                    if(result.token) {
                        var body;
                        if(result.has_season_ticket == 'true') {
                            body = 'Es gibt einen neuen potentiellen Mitfahrer für einen deiner Trips'
                        } else {
                            body = 'Es gibt eine neue potentielle Mitfahrgelegenheit für einen deiner Trips'
                        }
                        var message = new gcm.Message({
                            data: {
                                type: 'search_agent',
                                unique_trip_id: req.body.unique_trip_id,
                                sequence_id_departure_station: result.owner_sequence_id_departure_station,
                                sequence_id_target_station: result.owner_sequence_id_target_station,
                                user_id: result.owner_user_id,
                                has_season_ticket: result.has_season_ticket,
                                departure_time: result.owner_departure_time,
                                arrival_time: result.owner_arrival_time,
                                departure_station_name: result.owner_departure_station_name,
                                target_station_name: result.owner_target_station_name
                            },
                            notification: {
                                title: 'DTSharing - Suchagent',
                                body: body
                            }
                        });
                        var sender = new gcm.Sender('AIzaSyCutkpnGoS-TAk5wWDzxRPR9ARBR6lm38E');
                        sender.send(message, { registrationTokens: [result.token] }, function (err, response) {
                            if(err) {
                                console.error(err);
                            }
                        });
                    }
                });
            });
        });
    });
}


module.exports.match = function (req,res) {
    // dupes löschen
    // message an Partner
    //type: new_partner
//payload: dt_trip_id
    Dt_trips.findByIdAndUpdate(req.params.dt_trip_id, { 
        partner_user_id: req.body.user_id,
        partner_departure_time: req.body.departure_time,
        partner_arrival_time: req.body.arrival_time,
        partner_sequence_id_target_station: req.body.sequence_id_target_station,
        partner_sequence_id_departure_station: req.body.sequence_id_departure_station,
        partner_departure_station_name: req.body.departure_station_name,
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
        var chat = new Chats({
            owner_user_id: result.owner_user_id,
            partner_user_id: req.body.user_id,
            dt_trip_id: result._id,
            key: req.body.key
        });
        chat.save(function (err, result) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.send({
                success_message: 'successfully matched',
                chat_id: result._id
            });
        });
    });
}

module.exports.findDtTrips = function (req, res) {
    Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partner_user_id : req.params.user_id}]}, '-__v', function (err, results) {
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
        var dt_trips = []
        results.forEach( function(result) {
            if(result.owner_user_id == req.params.user_id) {
                var number_partners = 0;
                if(result.partner_user_id) {
                    number_partners = 1;
                }
                dt_trips.push({
                    _id : result._id,
                    date : result.date,
                    route_name : result.route_name,
                    departure_station_name : result.owner_departure_station_name,
                    target_station_name : result.owner_target_station_name,
                    arrival_time : result.owner_arrival_time,
                    departure_time : result.owner_departure_time,
                    number_partners : number_partners
                });
            } else {
                dt_trips.push({
                    _id : result._id,
                    date : result.date,
                    route_name : result.route_name,
                    departure_station_name : result.partner_departure_station_name,
                    target_station_name : result.partner_target_station_name,
                    arrival_time : result.partner_arrival_time,
                    departure_time : result.partner_departure_time,
                    number_partners : 1
                });
            }
        });
        res.json(dt_trips);
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
        async.parallel([
            function(callback) {
                if(result.owner_user_id) {
                    Users.findById(result.owner_user_id, '-_id first_name',function(err, result) {
                        callback(err, result.first_name);
                    });
                } else {
                    callback(null);
                }
            }, function(callback) {
                if(result.partner_user_id) {
                    Users.findById(result.partner_user_id, '-_id first_name',function(err, result) {
                        callback(err, result.first_name);
                    });
                } else {
                    callback(null);
                }
            }
        ], function(err, results){
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            if(result.owner_user_id == req.params.user_id) {
                res.json({
                    trip: {
                        date : result.date,
                        route_name : result.route_name
                    },
                    user: {
                        first_name : results[0],
                        sequence_id_target_station : result.owner_sequence_id_target_station,
                        sequence_id_departure_station : result.owner_sequence_id_departure_station,
                        departure_station_name : result.owner_departure_station_name,
                        target_station_name : result.owner_target_station_name,
                        arrival_time : result.owner_arrival_time,
                        departure_time : result.owner_departure_time
                    },
                    partner: {
                        first_name : results[1],
                        sequence_id_target_station : result.partner_sequence_id_target_station,
                        sequence_id_departure_station : result.partner_sequence_id_departure_station,
                        departure_station_name : result.partner_departure_station_name,
                        target_station_name : result.partner_target_station_name,
                        arrival_time : result.partner_arrival_time,
                        departure_time : result.partner_departure_time
                    }
                });
            } else {
                res.json({
                    trip: {
                        _id : result._id,
                        unique_trip_id : result.unique_trip_id,
                        date : result.date,
                    },
                    user: {
                        first_name : results[1],
                        sequence_id_target_station : result.partner_sequence_id_target_station,
                        sequence_id_departure_station : result.partner_sequence_id_departure_station,
                        departure_station_name : result.partner_departure_station_name,
                        target_station_name : result.partner_target_station_name,
                        arrival_time : result.partner_arrival_time,
                        departure_time : result.partner_departure_time
                    },
                    partner: {
                        first_name : results[0],
                        sequence_id_target_station : result.owner_sequence_id_target_station,
                        sequence_id_departure_station : result.owner_sequence_id_departure_station,
                        departure_station_name : result.owner_departure_station_name,
                        target_station_name : result.owner_target_station_name,
                        arrival_time : result.owner_arrival_time,
                        departure_time : result.owner_departure_time
                    }
                });
            }
        });
    });
}

module.exports.removeDtTrip = function (req, res) {
    // chat löschen
    // message an Partner
    //type: delete
//payload: unique_trip_id, sequence_ids, has_season_ticket
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
                partner_departure_station_name: null,
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
                        success_message: 'successfully unmatched'
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