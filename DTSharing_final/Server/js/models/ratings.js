var mongoose = require('mongoose');

/*Datenbank Schema der Collection "ratings" */
module.exports = mongoose.model('ratings', {
    //ID aus der Users collection
    user_id: [{type: mongoose.Schema.Types.ObjectId, ref: 'users'}],
    //ID aus der Users collection
    author_id: [{type: mongoose.Schema.Types.ObjectId, ref: 'users'}],
    //1-5
    stars: Number,
    //keine Begrenzungen
    comment: String
});