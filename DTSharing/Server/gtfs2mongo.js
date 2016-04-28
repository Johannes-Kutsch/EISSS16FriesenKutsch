var gtfs = require('gtfs2mongo');
gtfs.convert('./gtfs_data','mongodb://localhost:27017/dtsharing',function(err){
	if(err){
		console.log(err);
	}
	process.exit();
});