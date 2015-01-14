/*global _,$,define,App*/
define([
    'moment',
    'momentTimeZone'
],function (moment, momentTimeZone) {
    'use strict';

    console.log('Using timezone ' +  App.config.timeZone + ' and locale ' + App.config.locale);

    moment.suppressDeprecationWarnings = true;
    moment.locale(App.config.locale);

    var offset = moment().tz(App.config.timeZone).zone();
    var moffset = offset * 60 * 1000;

    return {
        formatTimestamp: function (format, timestamp) {
            try {
                return moment(timestamp).tz(App.config.timeZone).format(format);
            } catch (error) {
                console.error('Date.formatTimestamp(' + format + ', ' + timestamp + ')', error);
                return timestamp;
            }
        },

        toUTCWithTimeZoneOffset:function(dateString){
            var dateUTCWithOffset = moment.utc(dateString).toDate().getTime() + moffset;
            return moment(dateUTCWithOffset).utc().format("YYYY-MM-DD\THH:mm:ss");
        },

        getMainZonesDates : function(timestamp){
            var mainZones = ['America/Los_Angeles','America/New_York','Europe/London','Europe/Paris','Europe/Moscow','Asia/Tokyo'];
            var mainZonesDates = [];
            _(mainZones).each(function(zone){
                mainZonesDates.push({
                    name:zone,
                    date:moment.utc(timestamp).tz(zone).format(App.config.i18n._DATE_FORMAT)
                });
            });

            mainZonesDates.push({
                name:App.config.timeZone + ' (yours)', date : moment.utc(timestamp).tz(App.config.timeZone).format(App.config.i18n._DATE_FORMAT)
            });

            mainZonesDates.push({
                name:'locale', date : moment(timestamp).format(App.config.i18n._DATE_FORMAT)
            });
            mainZonesDates.push({
                name:'utc', date : moment.utc(timestamp).format(App.config.i18n._DATE_FORMAT)
            });
            return mainZonesDates;
        },

        dateHelper:function($querySelector){

            $querySelector.each(function(){
                var _date = $(this).text();
                var dateUTCWithOffset = moment.utc(_date,App.config.i18n._DATE_FORMAT).toDate().getTime() + moffset;
                moment(dateUTCWithOffset).utc();

                var fromNow = moment(dateUTCWithOffset).utc().fromNow();
                $(this).popover({
                    title: '<b>' + App.config.timeZone + '</b><br /><i class="fa fa-clock-o"></i> ' + _date + '<br />' + fromNow,
                    html: true,
                    content: '<b>UTC</b><br /><i class="fa fa-clock-o"></i>  ' + moment.utc(_date,App.config.i18n._DATE_FORMAT).zone(-offset).format(App.config.i18n._DATE_FORMAT),
                    trigger: 'click',
                    placement: "top"
                });
            });
        }
    };
});
