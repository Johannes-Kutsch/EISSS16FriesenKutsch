var Stops = require('../models/stops');
    
module.exports.findStops = function (req, res) {
    //Eine Variable die Auskunft über die Version der Stops gibt, wird per Hand um 1 erhöht wenn die GTFS Daten neu eingelesen werden.
    var stops_version = 1;
    
    //var user = new User(req.body);
    if(req.query.user_version !== undefined || result.stops_version != req.query.stops_version) {
        Stops.find({}, 'stop_id stop_name stop_lat stop_lon -_id', function (err, results) {
            if(err) {
                res.status(444);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            res.json(results);
        });
    } else {
        res.json({});
    }
}