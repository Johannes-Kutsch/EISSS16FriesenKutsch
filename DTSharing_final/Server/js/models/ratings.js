var mongoose = require('mongoose');

/*Datenbank Schema der Collection "ratings" */
module.exports = mongoose.model('ratings', {
	rating_id: Number,
    user_id: Number,
    author_id: Number,
    stars: Number,
    comment: String
});