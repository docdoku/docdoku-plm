/*global App, $*/
define(['bootstrapDatepicker'], function () {

    'use strict';

    return {
        init : function() {

            $.fn.datepicker.dates.fr =  {
                days: App.config.i18n.DAYS,
                daysShort: App.config.i18n.DAYS_SHORT,
                daysMin: App.config.i18n.DAYS_MIN,
                months: App.config.i18n.MONTHS,
                monthsShort: App.config.i18n.MONTHS_SHORT,
                today: App.config.i18n.TODAY,
                clear: App.config.i18n.CLEAR,
                weekStart: App.config.i18n.WEEKSTART,
                format: App.config.i18n._DATE_FORMAT
            };
        }
    };

});
