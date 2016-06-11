var Users = require('../models/users');
    
module.exports.register = function (req, res) {
    var user = new User(req.body);
    user.save(function (err, result) {
        res.json(result);
    });
}

module.exports.findUserProfile = function (req, res) {
    var user_id = req.params.user_id;
    Users.find( { user_id : user_id }, function (err, results) {
        //ToDo Errorhandling wenn kein User gefunden
        res.json(results);
    });
}