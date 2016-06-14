var Users = require('../models/users'),
    mongoose = require('mongoose');
    
module.exports.register = function (req, res) {
    Users.findOne({email : req.body.email}, function(err, result) {
        if(err) {
            res.status(444);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(result) {
            res.status(409);
            res.send({
                errorMessage: 'A User for that Mail already exists'
            });
            return;
        } else {
            var user = new Users({
            user_version: 0,
            birth_year: req.body.birth_year,
            first_name: req.body.first_name,
            last_name: req.body.last_name,
            gender: req.body.gender,
            interests: req.body.interests,
            more: req.body.more,
            email: req.body.email,
            pass: req.body.pass,
            picture: req.body.picture,
            picture_version: 0
            });
            user.save(function (err, result) {
                res.json(result);
            });
        }
    });

}

module.exports.findUser = function (req, res) {
    Users.findById(req.params.userID, '-__v', function (err, result) {
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
                success: 0,
                errorMessage: 'User not found'
            });
            return;
        }
        var responseObject = {};
        if(req.query.user_version !== undefined && result.user_version != req.query.user_version) {
            responseObject.user_version = result.user_version;
            responseObject.birth_year = result.birth_year;
            responseObject.first_name = result.first_name;
            responseObject.last_name = result.last_name;
            responseObject.gender = result.gender;
            responseObject.interests = result.interests;
            responseObject.more = result.more;
        }
        if(req.query.picture_version !== undefined && result.picture_version != req.query.picture_version) {
            responseObject.picture = result.picture;
            responseObject.picture_version = result.picture_version;
        }
        res.json(responseObject);
    });
}