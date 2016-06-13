var Users = require('../models/users'),
    mongoose = require('mongoose');
    
module.exports.register = function (req, res) {
    //ToDo Errorhandling wenn User schon vorhanden
    var user = new Users({
        picture_id: null,
        birth_year: req.body.birthYear,
        first_name: req.body.firstName,
        name: req.body.name,
        gender: req.body.gender,
        intersts: req.body.intersts,
        email: req.body.email,
        pass: req.body.pass
    });
    user.save(function (err, result) {
        console.log('Benutzer angelegt: ' + result._id);
        res.json(result);
    });
}

module.exports.findUser = function (req, res) {
    //ToDo Errorhandling wenn kein User gefunden
    Users.findById(req.params.userID, '-__v', function (err, result) {
        res.json(result);
    });
}