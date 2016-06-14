var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    async = require('async'),
    ObjectId = require('mongoose').Types.ObjectId; 
    mongoose = require('mongoose');
    
module.exports.rate = function (req, res) {
    var error;
    async.parallel([
        function(callback) {
            Users.findById(req.params.userID, '-__v', function (err, result) {
                if(!result) {
                    if(!error) {
                        res.status(404);
                        error = 'User not found'
                    }
                }
                callback(err);
            });
        }, function(callback) {
            Users.findById(req.body.authorID, '-__v', function (err, result) {
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
            console.error(err);
        }
        if(error) {
            res.send({errorMessage: error});
            return;
        }
        var userObjectID = new mongoose.mongo.ObjectID(req.params.userID);
        var authorObjectID = new mongoose.mongo.ObjectID(req.body.authorID);
        var rating = new Ratings({
            user_id: userObjectID,
            author_id: authorObjectID,
            stars: req.body.stars,
            comment: req.body.comment
        });
        rating.save(function (err, result) {
            console.error(err);
            res.json(result);
        });
    });
}

module.exports.findRating = function (req, res) {
    Users.find({user_id : new ObjectId(req.params.userID)}, '-__v', function (err, results) {
        if(err) {
            console.error(err);
        }
        console.log(results);
        if(!results.length > 0) {
            res.status(404);
            res.send({
                errorMessage: 'No Ratings for this User'
            });
            return;
        }
        res.json(results);
    });
}