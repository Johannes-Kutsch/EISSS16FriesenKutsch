var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
    
module.exports.rate = function (req, res) {
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
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        res.json(result);
    });
}

module.exports.findRating = function (req, res) {
    Ratings.find({user_id : req.params.user_id}, '-__v', function (err, results) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        console.log(results);
        if(results.length == 0) {
            console.log('No Ratings for this User | 404');
            res.status(404);
            res.send({
                error_message: 'No Ratings for this User'
            });
            return;
        }
        res.json(results);
    });
}