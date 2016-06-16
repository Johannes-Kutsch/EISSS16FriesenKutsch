var Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
 
module.exports.login = function (req, res) {
    Users.findOne({email : req.query.email, pass : req.query.pass}, '_id', function (err, result) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        } else if(!result) {
            res.status(404);
            res.send({
                error_message: 'wrong pass or email'
            });
            return;
        }
        res.json(result);
    });
}