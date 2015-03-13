/*global $,define,App*/
define(['datatables','moment'], function (DT,moment) {
	'use strict';

    // sorting eu dates with current format and timezone
    // allows string comparison

    var getComparableDate = function(date){
        return moment(date,App.config.i18n._DATE_FORMAT).toDate().getTime();
    };

    var stripTags = function(input, allowed) {
        allowed = (((allowed || '') + '').toLowerCase().match(/<[a-z][a-z0-9]*>/g) || []).join('');
        var tags = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi, commentsAndPhpTags = /<!--[\s\S]*?-->|<\?(?:php)?[\s\S]*?\?>/gi;
        return input.replace(commentsAndPhpTags, '').replace(tags, function ($0, $1) {
            return allowed.indexOf('<' + $1.toLowerCase() + '>') > -1 ? $0 : '';
        });
    };

    $.fn.dataTableExt.oSort['date_sort-asc'] = function (x, y) {
        return getComparableDate(x) - getComparableDate(y);
    };

    $.fn.dataTableExt.oSort['date_sort-desc'] = function (x, y) {
        return getComparableDate(y) - getComparableDate(x);
    };

    $.fn.dataTableExt.oSort['strip_html-asc'] = function (x, y) {

        return stripTags(x).trim() > stripTags(y).trim() ? 1:-1;
    };

    $.fn.dataTableExt.oSort['strip_html-desc'] = function (x, y) {
        return stripTags(x).trim() <= stripTags(y).trim() ? 1:-1;
    };

});
