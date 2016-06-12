var Stops = require('../models/stops');
    
module.exports.findStops = function (req, res) {
    //var user = new User(req.body);
    Stops.find({}, 'stop_id stop_name stop_lat stop_lon -_id', function (err, results) {
        res.json(results);
    });
}