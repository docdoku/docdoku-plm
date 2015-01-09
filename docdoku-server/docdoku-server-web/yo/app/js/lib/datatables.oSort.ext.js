/*global $,define*/
define(['datatables','moment','momentTimeZone'], function (DT,moment,momentTimeZone) {
	'use strict';

    // sorting eu dates with current format and timezone
    // allows string comparison

    var getComparableDate = function(date){
        return moment(date,App.config.i18n._DATE_FORMAT).tz(App.config.timeZone).toDate().getTime();
    };


    $.fn.dataTableExt.oSort['date_sort-asc'] = function (x, y) {
        return getComparableDate(x) > getComparableDate(y);
    };

    $.fn.dataTableExt.oSort['date_sort-desc'] = function (x, y) {
        return getComparableDate(y) > getComparableDate(x);
    };
});
