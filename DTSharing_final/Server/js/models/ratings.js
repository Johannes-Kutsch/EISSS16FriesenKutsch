var mongoose = require('mongoose');

/*Datenbank Schema der Collection "ratings" */
module.exports = mongoose.model('ratings', {
    user_id: {type: mongoose.Schema.Types.ObjectId, ref: 'users'},
    author_id: {type: mongoose.Schema.Types.ObjectId, ref: 'users'},
    stars: Number,
    comment: String
});