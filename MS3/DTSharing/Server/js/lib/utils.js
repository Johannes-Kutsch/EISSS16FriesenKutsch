//Quelle: https://github.com/brendannee/node-gtfs/blob/master/lib/gtfs.js
//Wurde teilweise angepasst!

module.exports = {
    
    //gibt den Wochentag eines Date-Objects zurück
    getDayName: function(date) {
        var days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        return days[date.getDay()];
    },
    
    //selber geschrieben
    //wandelt einen String mit dem Format DD.MM.YYYY in ein Date Object um
    formatDate: function(date) {
        var dateParts;
        //String in Tag, Monat und Jahr unterteilen
        dateParts = date.split('.');
        if (dateParts.length != 3) {
            //String war im falschem Format
            return null;
        }
        //Date Object erstellen
        return new Date(dateParts[2], dateParts[1]-1, dateParts[0]);
    },

    //wandelt ein Date Object in einen String mit dem Format DD.MM.YYYY um
    formatDay: function(date) {
        var day = (date.getDate() < 10) ? '' + '0' + date.getDate() : date.getDate(),
            month = ((date.getMonth() + 1) < 10) ? '' + '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
            year = date.getFullYear();
        return '' + day + "." + month + "." + year;
    },
    
    //selber geschrieben
    //wandelt ein Date Object in einen String mit dem Format DDMMYYYY um
    formatDayWithoutDots: function(date) {
        //Datum, Monat und Jahr ermitteln
        var day = (date.getDate() < 10) ? '' + '0' + date.getDate() : date.getDate(),
            month = ((date.getMonth() + 1) < 10) ? '' + '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
            year = date.getFullYear();
        //Einen String zusammenfügen
        return '' + year + month + day;
    },

    //etwas abgeändert
    //ermittel aus einem String mit dem Format hh.mm.ss oder hh.mm oder einem Date Object die Anzahl an Sekunden
    timeToSeconds: function(time) {
        var timeParts;
        if (time instanceof Date) {
            //time ist ein Date Object
            timeParts = [time.getHours(), time.getMinutes(), time.getSeconds()];
        } else {
            //time ist ein String
            timeParts = time.split(':');
            if (timeParts.length == 2) {
                //time war im Format hh.mm
                timeParts[2] = 0;
            }
        }
        //Anzahl an Sekunden errechnen
        return parseInt(timeParts[0], 10) * 60 * 60 + parseInt(timeParts[1], 10) * 60 + parseInt(timeParts[2], 10);
    },

    //ermittelt aus einer Sekundenanzahl die Uhrzeit
    secondsToTime: function(seconds) {
        if (seconds === undefined || seconds === '') {
            return seconds;
        } 
      
        var hour = Math.floor(seconds / (60 * 60)),
            minute = Math.floor((seconds - hour * (60 * 60)) / 60),
            second = seconds - hour * (60 * 60) - minute * 60;

        return((hour < 10) ? '' + '0' + hour : hour) + ':' + ((minute < 10) ? '' + '0' + minute : minute) + ':' + ((second < 10) ? '' + '0' + second : second);
    }
};
