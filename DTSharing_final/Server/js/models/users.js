var mongoose = require('mongoose');

/*Datenbank Schema der Collection "users" */
module.exports = mongoose.model('users', {
	user_id: Number,
    picture_id: Number,
    birth_year: Number,
    first_name: String,
    name: String,
    gender: String,
    intersts: String,
    email: String,
    pass: String
});