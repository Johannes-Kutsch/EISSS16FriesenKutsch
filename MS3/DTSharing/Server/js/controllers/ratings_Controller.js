var Ratings = require('../models/ratings'),
    Users = require('../models/users'),
    async = require('async'),
    utils = require('../lib/utils'),
    mongoose = require('mongoose');
    
//erstellt ein Rating zu einem user
module.exports.rate = function (req, res) {
    
    //date ist ein Dateobject mit aktuellem Datum und Uhrzeit
    var date = new Date();
    //Die Zeitzone muss angepasst werden, der einfachste weg ist es zwei Stunden in ms Sekunden hinzuzufügen
    date.setTime(date.getTime()+7200000);
    
    //ein neues rating wird angelegt und mit den Informationen gefüllt
    var rating = new Ratings({
        user_id: req.params.user_id,
        author_id: req.body.author_id,
        stars: req.body.stars,
        comment: req.body.comment,
        date: utils.formatDay(date),
        chat_id: req.body.chat_id
    });
    
    //das Rating wird gespeichert
    rating.save(function (err, result) {
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        //eine success_message wird als response gesendet
        res.status(201);
        res.send({
            success_message: 'successfully rated'
        });
    });
}

//finde alle ratings die zu einen user bewerten
module.exports.findRatings = function (req, res) {
    //die ratings aus der Ratings Collection auslesen
    Ratings.find({user_id : req.params.user_id}, '-__v', function (err, results) {
        if(err) {
            //Es gab einen Datenbankfehler
            res.status(500);
            res.send({
                error_message: 'Database Error'
            });
            console.error(err);
            return;
        }
        
        if(results.length == 0) {
            //Der user wurde noch nicht bewertet
            console.log('No Ratings for this User | 404');
            res.status(404);
            res.send({
                error_message: 'No Ratings for this User'
            });
            return;
        }
        
        //Aray in dem die ratings gespeichert werden sollen
        var ratings = [];
        
        //Jedes rating durchlaufen um es mit weitern Informationen anzureichern
        async.each(results, function(result, callback) {
            //Den Namen des Authors ermitteln
            Users.findById(result.author_id, 'first_name last_name picture' , function (err, author) {
                if(err) {
                    //Es gab einen Datenbankfehler
                    callback(err);
                }
                
                //ein rating Object erstellen und mit Daten anreichern
                var rating = {
                    author : {
                        first_name : author.first_name,
                        last_name : author.last_name,
                        picture :  author.picture
                    },
                    rating : {
                        date : result.date,
                        stars : result.stars,
                        comment : result.comment                        
                    }
                }
                
                //Das Object in das ratings array schreiben
                ratings.push(rating);
                callback(null);
            });
            
        //wird aufgerufen nachdem alle Ratings durchlaufen wurden
        }, function (err) {
            if(err) {
                //Es gab einen Datenbankfehler
                res.status(500);
                res.send({
                    error_message: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Die durchschnittliche Bewertung des users ermitteln
            //average_rating soll 0 sein wenn der user noch nicht bewertet wurde
            var average_rating = 0;
            results.forEach(function(result) {
                //jedes Rating zum average_rating hinzuadieren
                average_rating += result.stars;
            });
            
            //Überprüfung damit nicht durch 0 geteilt wird
            if(results.length) {
                //average_rating durch die Anzahl an Bewertungen teilen
                average_rating/=results.length;
            }
            
            //response Object erstellen und mit Daten anreichern
            var response = {
                user_data: {
                    user_id : req.params.user_id,
                    average_rating : average_rating
                },
                ratings: ratings
            };
            
            //das response Object als response senden
            res.json(response);
        });
        
    });
}