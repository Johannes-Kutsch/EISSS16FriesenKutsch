var Stops = require('../models/stops');
    
//eine Liste aller Haltestellen bestehend aus Name und Position ermitteln
module.exports.findStops = function (req, res) {
    
    //Eine Variable die Auskunft über die Version der Stops gibt, wird per Hand um 1 erhöht wenn die GTFS Daten neu eingelesen werden.
    //Die stops_version kann bei der Anfrage mitgegeben werden um zu überprüfen ob bereits die aktuellen Daten vorliegen
    var stops_version = 1;
    
    if(req.query.stops_version == undefined || stops_version != req.query.stops_version) {
        //Die aktuellen Daten liegen noch nicht vor
        Stops.find({}, 'stop_name stop_lat stop_lon -_id', function (err, results) {
            if(err) {
                //Es gab einen Datenbankfehler
                res.status(500);
                res.send({
                    errorMessage: 'Database Error'
                });
                console.error(err);
                return;
            }
            
            //Die aktuelle stops_version und die stops werden als response übermittelt
            res.json({
                stops_version: stops_version,
                stops: results
            });
        });
    } else {
        //Die aktuellen Daten liegen bereits vor
        res.json({});
    }
}