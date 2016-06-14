var Users = require('../models/users'),
    mongoose = require('mongoose');
    
module.exports.register = function (req, res) {
    if(req.body.birthYear == null) {
        res.status(400);
        res.send({
            errorCode: '0001',
            errorMessage: 'birthYear is missing'
        });
        return;
    } else if(typeof req.body.birthYear != 'number') {
        res.status(400);
        res.send({
            errorCode: '0002',
            errorMessage: 'birthYear is not a Number'
        });
        return;
    } else if(req.body.birthYear < 1000 || req.body.birthYear > 9999 || req.body.birthYear % 1 != 0) {
        res.status(400);
        res.send({
            errorCode: '0003',
            errorMessage: 'birthYear has to be a whole Number with length 4'
        });
        return;
    }
    
    if(req.body.firstName == null) {
        res.status(400);
        res.send({
            errorCode: '0011',
            errorMessage: 'firstName is missing' 
        });
        return;
    } else if(typeof req.body.firstName != 'string') {
        res.status(400);
        res.send({
            errorCode: '0012',
            errorMessage: 'firstName is not a String'
        });
        return;
    }
        
    if(req.body.name == null) {
        res.status(400);
        res.send({
            errorCode: '0013',
            errorMessage: 'name is missing' 
        });
        return;
    } else if(typeof req.body.name != 'string') {
        res.status(400);
        res.send({
            errorCode: '0014',
            errorMessage: 'name is not a String'});
        return;
    }
    
    if(req.body.gender == null) {
        res.status(400);
        res.send({
            errorCode: '0021',
            errorMessage: 'gender is missing' 
        });
        return;
    } else if(typeof req.body.name != 'string') {
        res.status(400);
        res.send({
            errorCode: '0022',
            errorMessage: 'gender is not a String'
        });
        return;
    } else if(req.body.gender != 'm' && req.body.gender != 'f') {
        console.log([typeof req.body.gender != 'm' || typeof req.body.gender != 'f']);
        res.status(400);
        res.send({
            errorCode: '0023',
            errorMessage: 'gender has to be m or f'
        });
        return;
    }
    
    if(typeof req.body.interests != 'string') {
        res.status(400);
        res.send({
            errorCode: '0031',
            errorMessage: 'interests has to be a string' 
        });
        return;
    }

    
    if(typeof req.body.more != 'string') {
        res.status(400);
        res.send({
            errorCode: '0041',
            errorMessage: 'more has to be a string' 
        });
        return;
    }
    
    if(req.body.email == null) {
        res.status(400);
        res.send({
            errorCode: '0051',
            errorMessage: 'email is missing' 
        });
        return;
    } else if(typeof req.body.email != 'string') {
        res.status(400);
        res.send({
            errorCode: '0052',
            errorMessage: 'email is not a String'
        });
        return;
    }
    
    if(req.body.pass == null) {
        res.status(400);
        res.send({
            errorCode: '0061',
            errorMessage: 'pass is missing' 
        });
        return;
    } else if(typeof req.body.pass != 'string') {
        res.status(400);
        res.send({
            errorCode: '0071',
            errorMessage: 'pass is not a String'
        });
        return;
    }
    
    Users.findOne({email : req.body.email}, function(err, result) {
        if(result) {
            res.status(409);
            res.send({
                errorCode: '0081',
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
                console.error(err);
                console.log('Benutzer angelegt: ' + result._id);
                res.json(result);
            });
        }
    });

}

module.exports.findUser = function (req, res) {
    //ToDo Errorhandling wenn kein User gefunden
    Users.findById(req.params.userID, '-__v', function (err, result) {
        if(!result) {
            res.status(404);
            res.send({
                errorCode: '0091',
                errorMessage: 'User not found'
            });
            return;
        }
        res.json(result);
    });
}