var Users = require('../models/users'),
    mongoose = require('mongoose'),
    default_picture = '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';    


module.exports.register = function (req, res) {
    Users.findOne({email : req.body.email}, function(err, result) {
        if(err) {
            res.status(500);
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
            picture: default_picture,
            picture_version: 0
            });
            user.save(function (err, result) {
                res.json(result);
            });
        }
    });

}

module.exports.findUser = function (req, res) {
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
        var responseObject = {};
        if(req.query.user_version == undefined || result.user_version != req.query.user_version) {
            responseObject.user_version = result.user_version;
            responseObject.birth_year = result.birth_year;
            responseObject.first_name = result.first_name;
            responseObject.last_name = result.last_name;
            responseObject.gender = result.gender;
            responseObject.interests = result.interests;
            responseObject.more = result.more;
        }
        if(req.query.picture_version == undefined || result.picture_version != req.query.picture_version) {
            responseObject.picture = result.picture;
            responseObject.picture_version = result.picture_version;
        }
        res.json(responseObject);
    });
}