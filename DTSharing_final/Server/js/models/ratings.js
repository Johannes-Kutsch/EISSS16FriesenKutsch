var mongoose = require('mongoose');

/*Datenbank Schema der Collection "ratings" */
module.exports = mongoose.model('ratings', {
    user_id: String,
    author_id: String,
    stars: Number,
    comment: String
});