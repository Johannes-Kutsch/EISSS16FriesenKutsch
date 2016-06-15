var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
    
module.exports.rate = function (req, res) {
    var error;
    async.parallel([
        function(callback) {
            Users.findById(req.params.user_id, function (err, result) {
                if(!result) {
                    if(!error) {
                        res.status(404);
                        error = 'User not found'
                    }
                }
                callback(err);
            });
        }, function(callback) {
            Users.findById(req.body.author_id, function (err, result) {
                if(!result) {
                    if(!error) {
                        res.status(404);
                        error = 'Author not found'
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
        var rating = new Ratings({
            user_id: req.params.user_id,
            author_id: req.body.author_id,
            stars: req.body.stars,
            comment: req.body.comment
        });
        rating.save(function (err, result) {
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

module.exports.findRating = function (req, res) {
    Ratings.find({user_id : req.params.user_id}, '-__v', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        console.log(results);
        if(results.length == 0) {
            res.status(404);
            res.send({
                errorMessage: 'No Ratings for this User'
            });
            return;
        }
        res.json(results);
    });
}