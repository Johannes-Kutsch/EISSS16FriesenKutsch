var mongoose = require('mongoose');

/*Datenbank Schema der Collection "users" */
module.exports = mongoose.model('users', {
    user_version: Number,
    birth_year: Number,
    token: String,
    first_name: String,
    last_name: String,
    gender: String,
    interests: String,
    more: String,
    email: {type : String , unique : true, required : true, dropDups: true },
    pass: String,
    picture: String,
    picture_version: Number
});