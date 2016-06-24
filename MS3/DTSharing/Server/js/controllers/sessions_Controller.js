var Users = require('../models/users'),
    async = require('async'),
    mongoose = require('mongoose');
 
//einfaches login durch abgleichen von Email und dem gehastem Passwort
//es soll außerdem das fcm Token des Users gespeichert werden
module.exports.login = function (req, res) {
    
    //überprüfen ob die Kombination aus email und pass in der Collection gefunden werden kann
    Users.findOne({email : req.body.email, pass : req.body.pass}, '_id picture first_name last_name interests more', function (err, user) {
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        } else if(user) {
            //Es wurde ein user gefunden
            
            //Es wird überprüft ob ein anderer User vorher auf dem Gerät eingeloggt war, falls ja wird sein token auf null gesetzt
            Users.findOneAndUpdate({token : req.body.token}, {token : null}, function(err, result) {
                if(err) {
                    //Es gab einen Datenbankfehler
                    res.status(500);
                    res.send({
                        error_message: 'Database Error'
                    });
                    console.error(err);
                    return;
                }
                
                //Das token wird bei dem user der sich grade einloggt geupdated
                Users.findByIdAndUpdate(user._id, {token : req.body.token}, function(err, result) {
                    if(err) {
                        //Es gab einen Datenbankfehler
                        res.status(500);
                        res.send({
                            error_message: 'Database Error'
                        });
                        console.error(err);
                        return;
                    }
                    
                    //Die Daten des users werden als response übermittelt
                    res.json(user);
                });
            });       
        } else {
            //Es wurde kein user mit dieser Kombination gefunden
            console.log('wrong pass or email | 403');
            res.status(403);
            res.send({
                error_message: 'wrong pass or email'
            });
            return
        }
    });
}