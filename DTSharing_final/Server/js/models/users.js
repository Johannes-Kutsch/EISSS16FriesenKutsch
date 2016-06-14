var mongoose = require('mongoose');

/*Datenbank Schema der Collection "users" */
module.exports = mongoose.model('users', {
    user_version: Number,
    birth_year: Number,
    first_name: String,
    name: String,
    gender: String,
    interests: String,
    more: String,
    email: {type : String , unique : true, required : true, dropDups: true },
    pass: String,
    picture: String,
    picture_version: Number
});