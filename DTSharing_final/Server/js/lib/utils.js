//Aus dem gtfs Modul übernommen und abgeändert

var _ = require('lodash');

module.exports = {
  isInt: function(n) {
    return typeof n === 'number' && n % 1 === 0;
  },

  getDayName: function(date) {
    var days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    return days[date.getDay()];
  },
    
  //selber geschrieben    
  formatDate: function(date) {
      var dateParts;
      dateParts = date.split('.');
      if (dateParts.length != 3) {
        return null;
      }
      return new Date(dateParts[2], dateParts[1]-1, dateParts[0]);
  },

  formatDay: function(date) {
    var day = (date.getDate() < 10) ? '' + '0' + date.getDate() : date.getDate(),
      month = ((date.getMonth() + 1) < 10) ? '' + '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
      year = date.getFullYear();
    return '' + day + "." + month + "." + year;
  },
    
  //selber geschrieben
  formatDayWithoutDots: function(date) {
    var day = (date.getDate() < 10) ? '' + '0' + date.getDate() : date.getDate(),
      month = ((date.getMonth() + 1) < 10) ? '' + '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
      year = date.getFullYear();
    return '' + year + month + day;
  },

  //etwas abgeändert
  timeToSeconds: function(time) {
    var timeParts;
    if (time instanceof Date) {
      timeParts = [time.getHours(), time.getMinutes(), time.getSeconds()];
    } else {
      timeParts = time.split(':');
      if (timeParts.length == 2) {
        timeParts[2] = 0;
      }
    }
    return parseInt(timeParts[0], 10) * 60 * 60 + parseInt(timeParts[1], 10) * 60 + parseInt(timeParts[2], 10);
  },

  secondsToTime: function(seconds) {
    if (seconds === undefined || seconds === '') {
      return seconds;
    } 
      
    var hour = Math.floor(seconds / (60 * 60)),
      minute = Math.floor((seconds - hour * (60 * 60)) / 60),
      second = seconds - hour * (60 * 60) - minute * 60;

    return((hour < 10) ? '' + '0' + hour : hour) + ':' + ((minute < 10) ? '' + '0' + minute : minute) + ':' + ((second < 10) ? '' + '0' + second : second);
  },

  milesToDegrees: function(miles) {
    var milesPerDegree = 69.17101972;
    return miles / milesPerDegree;
  }
};
