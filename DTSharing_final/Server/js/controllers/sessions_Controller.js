var Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
 
module.exports.login = function (req, res) {
    Users.findOne({email : req.body.email, pass : req.body.pass}, '_id picture first_name last_name interests more', function (err, user) {
        if(err) {
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        } else if(user) {
            Users.findOneAndUpdate({token : req.body.token}, {token : null}, function(err, result) {
                if(err) {
                    res.status(500);
                    res.send({
                        error_message: 'Database Error'
                    });
                    console.error(err);
                    return;
                }
                Users.findByIdAndUpdate(user._id, {token : req.body.token}, function(err, result) {
                    if(err) {
                        res.status(500);
                        res.send({
                            error_message: 'Database Error'
                        });
                        console.error(err);
                        return;
                    }
                    res.json(user);
                });
            });       
        } else {
            console.log('wrong pass or email | 403');
            res.status(403);
            res.send({
                error_message: 'wrong pass or email'
            });
            return
        }
    });
}