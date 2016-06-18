var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    mongoose = require('mongoose');    


module.exports.register = function (req, res) {
    Users.findOne({email : req.body.email}, function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(result) {
            console.log('A User for that Mail already exists | 409');
            res.status(409);
            res.send({
                error_message: 'A User for that Mail already exists'
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
                picture: null,
                picture_version: 0
                });
            user.save(function (err, result) {
                res.status(201);
                res.send({
                    success_message: 'Registration sucessfull'
                });
            });
        }
    });
}

module.exports.findUser = function (req, res) {
    Users.findById(req.params.user_id, '-__v', function (err, result) {
         if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            console.log('User not found | 404');
            res.status(404);
            res.send({
                error_message: 'User not found'
            });
            return;
        }
        var response_object = {};
        if(req.query.user_version == undefined || result.user_version != req.query.user_version) {
            response_object.user_version = result.user_version;
            response_object.birth_year = result.birth_year;
            response_object.first_name = result.first_name;
            response_object.last_name = result.last_name;
            response_object.gender = result.gender;
            response_object.interests = result.interests;
            response_object.more = result.more;
        }
        if(req.query.picture_version == undefined || result.picture_version != req.query.picture_version) {
            response_object.picture = result.picture;
            response_object.picture_version = result.picture_version;
        }
        Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partner_user_id : req.params.user_id}]}, 'owner_user_id partner_user_id', function (err, results) {
            if(err) {
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            var count_offerer = 0;
            var count_passenger = 0;
            results.forEach( function(result) {
                if(result.owner_user_id == req.params.user_id) {
                    count_offerer++;
                } else if (result.partner_user_id == req.params.user_id) {
                    count_passenger++;
                }
            });
            response_object.count_offerer = count_offerer;
            response_object.count_passenger = count_passenger;
            res.json(response_object);
        });
    });
}

module.exports.updateUser = function (req, res) {
    var query = {$inc : {}};
    if(req.body.interests) {
        query.interests = req.body.interests;
        query.$inc.user_version = 1;
    }
    if(req.body.more) {
        query.more = req.body.more;
        query.$inc.user_version = 1;
    }
    if(req.body.picture) {
        query.picture = req.body.picture;
        query.$inc.picture_version = 1;
    }
    console.log(query);
    Users.findByIdAndUpdate(req.params.user_id, query, function (err, result) {
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
        res.send({
                success_message: 'successfully updated'
            });
    });
}