var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    Ratings = require('../models/ratings'),
    async = require('async'),
    mongoose = require('mongoose');
 
module.exports.findMatches = function (req, res) {
    var query;
    if(req.query.has_season_ticket == 'true') {
        query = {
            owner_user_id : {$ne: req.query.user_id},
            unique_trip_id : req.query.unique_trip_id, 
            has_season_ticket : false, 
            owner_sequence_id_departure_station : {$gte: req.query.sequence_id_departure_station}, 
            owner_sequence_id_target_station : {$lte: req.query.sequence_id_target_station},
            partner_user_id : null
        }
    } else {
        query = {
            owner_user_id : {$ne: req.query.user_id},
            unique_trip_id : req.query.unique_trip_id, 
            has_season_ticket : true, 
            owner_sequence_id_departure_station : {$lte: req.query.sequence_id_departure_station}, 
            owner_sequence_id_target_station : {$gte: req.query.sequence_id_target_station},
            partner_user_id : null
        }
    }
    Dt_trips.find(query, '_id trip_id date owner_user_id owner_sequence_id_target_station owner_sequence_id_departure_station owner_destination_station_name owner_target_station_name has_season_ticket', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!results.length) {
            res.status(404);
            res.send({
                errorMessage: 'No Matches found'
            });
            return;
        }
        var matches = []
        async.each(results, function(result, callback) {
            Users.findById(result.owner_user_id, '_id picture first_name last_name', function (err, user) {
                if(err) {
                    callback(err)
                }
                Ratings.find({user_id : result.owner_user_id}, 'stars', function(err, ratings) {
                    if(err) {
                        callback(err)
                    }
                    var average_rating = 0;
                    ratings.forEach(function(rating) {
                        average_rating += rating;
                    });
                    if(average_rating.length) {
                        average_rating/=average_rating.length;
                    }
                    var match = {
                        match : result, 
                        owner : {
                            _id : user._id,
                            first_name : user.first_name,
                            last_name : user.last_name,
                            picture : user.picture,
                            average_rating : average_rating
                        }
                    }
                    matches.push(match);
                    callback(null);
                });
            });
        }, function(err) {
            if(err) {
                res.status(500);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json(matches);
        });
    });
}