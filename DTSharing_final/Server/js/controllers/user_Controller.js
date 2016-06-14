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
        if(!result) {
            res.status(409);
            res.send({
                errorMessage: 'A User for that Mail already exists'
            });
            return;
        } else {
            var user = new Users({
            picture_id: null,
            birth_year: req.body.birthYear,
            first_name: req.body.firstName,
            name: req.body.name,
            gender: req.body.gender,
            interests: req.body.interests,
            more: req.body.more,
            email: req.body.email,
            pass: req.body.pass
            });
            user.save(function (err, result) {
                res.json(result);
            });
        }
    });

}

module.exports.findUser = function (req, res) {
    //ToDo Errorhandling wenn kein User gefunden
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
        res.json(result);
    });
}