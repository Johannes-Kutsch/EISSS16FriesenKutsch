var mongoose = require('mongoose');

/*Datenbank Schema der Collection "users" */
module.exports = mongoose.model('users', {
    //Id aus der Pictures Collection, beim Post 0
    picture_id: [{type: mongoose.Schema.Types.ObjectId, ref: 'users'}],
    //4 Stellen
    birth_year: Number,
    //keine Begrenzungen
    first_name: String,
    //keine Begrenzungen
    name: String,
    // m oder w
    gender: String,
    //keine Begrenzungen
    intersts: String,
    //gültige Mail?
    email: String,
    //noch keine Begrenzungen
    pass: String,
});