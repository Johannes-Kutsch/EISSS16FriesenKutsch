var Users = require('../models/users'),
    Dt_trips = require('../models/dt_trips'),
    mongoose = require('mongoose');    

//Einen neuen user anlegen
module.exports.register = function (req, res) {
    
    //Überprüfen ob bereits ein user mit der angegeben Email existiert
    Users.findOne({email : req.body.email}, function(err, result) {
        
        if(err) {
            //Fehler bei der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(result) {
            //Es existiert bereits ein user mit der angegeben Email existiert
            console.log('A User for that Mail already exists | 409');
            res.status(409);
            res.send({
                error_message: 'A User for that Mail already exists'
            });
            return;
            
        } else {
            //Es existiert noch kein user mit der angegeben Email existiert
            //ein neues user Object erstellen und mit Daten füllen
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
                token: null,
                picture_version: 0
                });
            
            //das Object in die Users collection speichern
            user.save(function (err, result) {
                
                if(err) {
                    //Fehler bei der Datenbankabfrage
                    res.status(500);
                    res.send({
                        error_message: 'Database Error'
                    });
                    console.error(err);
                    return;
                }
                
                //Der user wurde erstellt
                res.status(201);
                res.send({
                    success_message: 'Registration sucessfull'
                });
            });
        }
    });
}

//Die Daten eines Users ermitteln
module.exports.findUser = function (req, res) {
    
    //Daten aus der Users Collection abrufen
    Users.findById(req.params.user_id, '-__v', function (err, result) {
        
         if(err) {
            //Fehler bei der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(!result) {
            //Es wurde kein User mit dieser urser_id gefunden
            console.log('User not found | 404');
            res.status(404);
            res.send({
                error_message: 'User not found'
            });
            return;
        }
        
        //Es soll ermittelt werden wieviele dt_trips der User angeboten hat und bei wievielen er mitgefahren ist
        Dt_trips.find({$or:[{owner_user_id : req.params.user_id},{partner_user_id : req.params.user_id}]}, 'owner_user_id partner_user_id', function (err, results) {
            
            if(err) {
                //Fehler bei der Datenbankabfrage
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Object das als response übermittelt wird
            var response_object = {};
            
            //Anzahl angebotdener dt_trips
            var count_offerer = 0;
            //Anzahl mitgefahrener dt_trips
            var count_passenger = 0;
            
            //Die Anzahl angebotdener dt_trips und die Anzahl mitgefahrener dt_trips ermitteln
            results.forEach( function(result) {
                if(result.owner_user_id == req.params.user_id) {
                    count_offerer++;
                } else if (result.partner_user_id == req.params.user_id) {
                    count_passenger++;
                }
            });
            
            //Für die geplante Clientseitige Speierung von Chats sollen auch die Userinformationen der Chatpartner gespeichert werden
            //Damit die Userdaten nur übermittelt werden wenn sie gebraucht werden wird die user_version und die picture_version mit der übermittelten user_version und picture_version abgeglichen
            //und es werden nur Daten übermittelt die aktualisiert wurden.
            //Die Angabe der Versions soll optional sein
            if(req.query.user_version == undefined || result.user_version != req.query.user_version) {
                //Es wurde keine user_version mitgegeben oder die user_versions sind nicht identisch
                response_object.user_version = result.user_version;
                response_object.birth_year = result.birth_year;
                response_object.first_name = result.first_name;
                response_object.last_name = result.last_name;
                response_object.gender = result.gender;
                response_object.interests = result.interests;
                response_object.more = result.more;
                response_object.count_offerer = count_offerer;
                response_object.count_passenger = count_passenger;
            }
            if(req.query.picture_version == undefined || result.picture_version != req.query.picture_version) {
                //Es wurde keine picture_version mitgegeben oder die picture_version sind nicht identisch
                response_object.picture = result.picture;
                response_object.picture_version = result.picture_version;
            }
            
            //Das response_object wird als response übermittelt
            res.json(response_object);
        });
    });
}

//Einige Daten eines Users werden geupdated
//Die entsprechende user_version wird erhöht
module.exports.updateUser = function (req, res) {
    
    //query zur Datenbankabfrage
    var query = {$inc : {}};

    if(req.body.interests) {
        //Die interests sollen verändert werden, die user_version soll um 1 erhöht werden
        query.interests = req.body.interests;
        query.$inc.user_version = 1;
    }
    if(req.body.more) {
        //Der Inhalt des Feldes more soll verändert werden, die user_version soll um 1 erhöht werden
        query.more = req.body.more;
        query.$inc.user_version = 1;
    }
    if(req.body.picture) {
        //Das picture soll verändert werden, die user_version soll um 1 erhöht werden
        query.picture = req.body.picture;
        query.$inc.picture_version = 1;
    }
    
    //Das entsprechende Users document wird ermittelt und geupdated
    Users.findByIdAndUpdate(req.params.user_id, query, function (err, result) {
        if(err) {
            //Fehler bei der Datenbankabfrage
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //Der Benutzer wurde nicht gefunden
        if(!result) {
            console.log('User not found | 404');
            res.status(404);
            res.send({
                error_message: 'User not found'
            });
            return;
        }
        
        //Es wird eine success_message als response übermittelt
        res.send({
                success_message: 'successfully updated'
            });
    });
}