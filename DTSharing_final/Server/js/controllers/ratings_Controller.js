var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
    
module.exports.rate = function (req, res) {
    var rating = new Ratings({
        user_id: req.params.user_id,
        author_id: req.body.author_id,
        stars: req.body.stars,
        comment: req.body.comment,
        date: req.body.date
    });
    rating.save(function (err, result) {
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
            success_message: 'successfully rated'
        });
    });
}

module.exports.findRatings = function (req, res) {
    Ratings.find({user_id : req.params.user_id}, '-__v', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(results.length == 0) {
            console.log('No Ratings for this User | 404');
            res.status(404);
            res.send({
                error_message: 'No Ratings for this User'
            });
            return;
        }
        var ratings = [];
        async.each(results, function(result, callback) {
            Users.findById(result.author_id, 'first_name last_name picture' , function (err, author) {
                if(err) {
                    callback(err);
                }
                var rating = {
                    author : {
                        first_name : author.first_name,
                        last_name : author.last_name,
                        picture :  author.picture
                    },
                    rating : {
                        date : result.date,
                        stars : result.stars,
                        comment : result.comment                        
                    }
                }
                ratings.push(rating);
                callback(null);
            });
        }, function (err) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            var average_rating = 0;
            results.forEach(function(result) {
                average_rating += result.stars;
            });
            if(results.length) {
                average_rating/=results.length;
            }
            var response = {
                user_data: {
                    user_id : req.params.user_id,
                    average_rating : average_rating
                },
                ratings: ratings
            };
            res.json(response);
        });
        
    });
}