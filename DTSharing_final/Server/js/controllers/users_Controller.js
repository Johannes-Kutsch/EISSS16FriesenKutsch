var Users = require('../models/users'),
    mongoose = require('mongoose'),
    default_picture = 'iVBORw0KGgoAAAANSUhEUgAAAMgAAAEsBAMAAAB01OGNAAAAIVBMVEX19fXd3d3n5+fi4uLl5eXp6enz8/Px8fHv7+/t7e3r6+vwMGzcAAAD0UlEQVR42u3bz08TQRQH8BFxC5x43aWlnFqUiycG0SAnqkHwVgSi8VR/ofFUoqDealAOnBYTkHhq/RHDfymHwktL29nuzHel5H3+AF6YN/PezNtUCSGEEEIIIYQQQgghhBCif3h7NzSduLX4XaFsFenMzKFCSK1Sk4fKvaEitZiHxEBH8YrUxl3lVJ3aWlIOvaQOQocJ0dRBGrhY7I5y5Bp15leUGwXqIqucGKSuKrh/hGWBGWFl3NZi0w7OCJkEytoVMqopW0ViqNQPk5mvLD2jCEq41WIZy8ZOUQSOSgqytGxSJAv2KQFvYo+iSQOLI1MWrlJEoV3e8ZlfoYhGcZuLjeM2FxuzbFjowjJCDFbtByiyMq48siruLLIwiSA1XO9lJVxVYXkJcomC9LK7LnYQi8MICVJNokBWcDcihnucMN+ux+OvkJoiytnOCdDPoH2KaBr6vuaziD8oFctxBP7RqHGbi9XBeefMo6crI8CiwjQuJewzLiVsEHdKmAd8ZrF93GqxAeADiBVxL1/2AncSmQdMO9u0vQXbjwjTyo13uEkq8zR1NKEYrLZUE/io9cB6lT79WQ/581y37bu1frwbK8RPTUR+2O3qHTTK73s6Mfuk97t2sfXvnOeHze3zdo8l/5VuXZEv52PUWtcyqMZt7XOn696SlyDkQWWcKEO63XEbvk7EFk/XZifm+S80L0tVNXy9Tw33DlXDc2oyF7e4p7krvf6xPjn59GibN4iON/hK6egf91NFU+WP3qWWOx2mFUPFNJZDcxRv1VAye5wQzJfbbMICtRPGvzjMnDsCbzW1lbW4YvvNtcn7TR34Vh195m/5LMSBtun6mrqYnTra3tj4sLemrS5iI2TPt5qjuZqwFciBjPVLwX6cM0hOlHEpYXlcSlgGlxI2hjslzDdUYDdC86vd3oIh7/DMa3IkZzVDs5+yXSNngJuLVZFFxVxY9smZadwOZhncDmY5YHk0l8hhcicAHhMG7L2sgjuLLMSdRVZCdhNTR6mTA6Yjv0IOZczPH3tZXFVhuSSCTCBLl6F4pcilNK4+sqCcQBC/ArwIcxBkpe9+XxnowyAhrmex2qUJUmrffSXIfwySlyAXLkj7xA/2YZBaov0k+c44RC4FCd278Hfh8SRu9aPmbwH28km8GasJvOP9ZMce+CkRfqiGfqEEqrM6YLVw1Z6rI66yjCfxkSYEf9PiQQF2/mz+6fQOWSspo19kaUmZeZZRllUk37RFPh6riN6sxQwze7Oiovt48GiyZ1PHu0oIIYQQQgghhBBCCCGEEEKIvvQPzSbKEl1V5+cAAAAASUVORK5CYII=';
    
module.exports.register = function (req, res) {
    Users.findOne({email : req.body.email}, function(err, result) {
        if(err) {
            res.status(500);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(result) {
            res.status(409);
            res.send({
                errorMessage: 'A User for that Mail already exists'
            });
            return;
        } else {
            var user = new Users({
            user_version: 0,
            birth_year: req.body.birth_year,
            first_name: req.body.first_name,
            last_name: req.body.last_name,
            gender: req.body.gender,
            interests: req.body.interests,
            more: req.body.more,
            email: req.body.email,
            pass: req.body.pass,
            picture: default_picture,
            picture_version: 0
            });
            user.save(function (err, result) {
                res.json(result);
            });
        }
    });

}

module.exports.findUser = function (req, res) {
    Users.findById(req.params.user_id, '-__v', function (err, result) {
         if(err) {
            res.status(500);
            res.send({
                errorMessage: 'Database Error'
            });
            console.error(err);
            return;
        }
        if(!result) {
            res.status(404);
            res.send({
                errorMessage: 'User not found'
            });
            return;
        }
        var responseObject = {};
        if(req.query.user_version == undefined || result.user_version != req.query.user_version) {
            responseObject.user_version = result.user_version;
            responseObject.birth_year = result.birth_year;
            responseObject.first_name = result.first_name;
            responseObject.last_name = result.last_name;
            responseObject.gender = result.gender;
            responseObject.interests = result.interests;
            responseObject.more = result.more;
        }
        if(req.query.picture_version == undefined || result.picture_version != req.query.picture_version) {
            responseObject.picture = result.picture;
            responseObject.picture_version = result.picture_version;
        }
        res.json(responseObject);
    });
}