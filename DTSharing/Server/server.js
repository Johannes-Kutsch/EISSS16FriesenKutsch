var express 	= require('express'),
	app			= express(),
	bodyParser 	= require('body-parser'),
	mongoose	= require('mongoose');
	db_postController = require('./js/controllers/db_postController');

mongoose.connect('mongodb://localhost:27017/dtsharing');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.get('/:type/get/all', db_postController.getAllByType);

app.post('/:type/post/entry', db_postController.createEntry);
app.post('/:type/get/matches', db_postController.getMatchesByType);

app.get('/vrs/:from/:to', function (req, res) {
	var from = req.params.from,
		to = req.params.to;

		
});

/*app.get('/getAll/:type', function (req, res) {
	if(req.params.type == "offers"){
		db_postController.getOffers;
	}else if(req.params.type == "searches"){
		db_postController.getSearches;
	}
});*/

//app.use('/js', express.static(__dirname + '/client/js'));
//app.use('/assets/js', express.static(__dirname + '/client/assets/js'));
//app.use('/css', express.static(__dirname + '/client/assets/css'));

//REST API
//app.get('/api/meetups', meetupsController.list);
//app.post('/api/meetups', meetupsController.create);

app.listen(3000, function() {
	console.log('Listening on port 3000...');
});