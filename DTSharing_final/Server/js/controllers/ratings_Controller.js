var Ratings = require('../models/ratings'),
    mongoose = require('mongoose');
    
module.exports.rate = function (req, res) {
    //ToDo Errorhandling wenn User schon vorhanden
    var userObjectID = new mongoose.mongo.ObjectID(req.params.userID);
    var authorObjectID = new mongoose.mongo.ObjectID(req.body.authorID);
        console.log(authorObjectID);
    var rating = new Ratings({
        user_id: userObjectID,
        author_id: authorObjectID,
        stars: req.body.stars,
        comment: req.body.comment
    });
    console.log(rating);
    rating.save(function (err, result) {
        //console.log('Rating angelegt: ' + result._id);
        res.json(result);
    });
}

module.exports.findRating = function (req, res) {
    //ToDo Errorhandling wenn kein User gefunden
    Ratings.findById(req.params.userID, '-__v', function (err, result) {
        res.json(result);
    });
}