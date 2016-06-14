var mongoose = require('mongoose');

/*Datenbank Schema der Collection "users" */
module.exports = mongoose.model('users', {
    picture_id: [{type: mongoose.Schema.Types.ObjectId, ref: 'users'}],
    birth_year: Number,
    first_name: String,
    name: String,
    gender: String,
    interests: String,
    more: String,
    email: {type : String , unique : true, required : true, dropDups: true },
    pass: String,
});