var express 	= require('express'),
	app			= express(),
	bodyParser 	= require('body-parser'),
	mongoose	= require('mongoose');
	//meetupsController = require('./controllers/meetups-controller');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.post('/:type/newEntry', function (req, res) {
	console.log(req.params.type);
	var test = {
		"success": 1,
		"data": "Daten hier"
	}
	res.writeHead(200, {
				'Content-Type': 'application/json'
			});
  	res.end(JSON.stringify(test));
});

app.get('getAll', function (req, res) {

});

//app.use('/js', express.static(__dirname + '/client/js'));
//app.use('/assets/js', express.static(__dirname + '/client/assets/js'));
//app.use('/css', express.static(__dirname + '/client/assets/css'));

//REST API
//app.get('/api/meetups', meetupsController.list);
//app.post('/api/meetups', meetupsController.create);

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});