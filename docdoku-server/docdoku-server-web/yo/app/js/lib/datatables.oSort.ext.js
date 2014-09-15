/*global define*/
define(['datatables'], function (DT) {

    // sorting french dates dd-mm-YYYY hh:ii:ss
    function parseTimeStampFromFrenchDate(str) {
        if (!str) return 0;
        var _split = str.split(" ");
        if (_split.length != 2) return 0;
        var left = _split[0].split("-");
        var right = _split[1].split(":");
        if (left.length != 3 && right.length != 3) return 0;
        // return new Date(Year, Month, Day, Hours, Minutes, Seconds);
        return new Date(left[2], left[1], left[0], right[0], right[1], right[2]).getTime();
    }

    $.fn.dataTableExt.oSort['date_sort_fr-asc'] = function (x, y) {
        return parseTimeStampFromFrenchDate(x) - parseTimeStampFromFrenchDate(y);
    }

    $.fn.dataTableExt.oSort['date_sort_fr-desc'] = function (x, y) {
        return parseTimeStampFromFrenchDate(y) - parseTimeStampFromFrenchDate(x);
    }

    // sorting eu dates dd-mm-YYYY hh:ii:ss
    // allows string comparison

    $.fn.dataTableExt.oSort['date_sort_en-asc'] = function (x, y) {
        return x > y;
    }

    $.fn.dataTableExt.oSort['date_sort_en-desc'] = function (x, y) {
        return y > x;
    }

});