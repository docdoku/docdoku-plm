/*global define,App*/
define(['moment', 'momentTimeZone'],function (moment, momentTimeZone) {

    'use strict';

    moment.suppressDeprecationWarnings = true;
    moment.locale(App.config.locale);
    var zone = App.config.timeZone;
    console.log('Using timezone ' + zone);

    return {
        formatTimestamp: function (format, timestamp) {
            try {
                return moment(timestamp).tz(zone).format(format);
            } catch (error) {
                console.error('Date.formatTimestamp(' + format + ', ' + timestamp + ')', error);
                return timestamp;
            }
        },
        fromNow: function (timestamp) {
            try {
                return moment.tz(timestamp,zone).fromNow();
            } catch (error) {
                console.error('Date.formatTimestamp(' + timestamp + ')', error);
                return timestamp;
            }
        }
    };

});
