(function(){
    'use strict';

    angular.module('dplm.filters.timeago', [])
        .filter('timeago', function () {
            return function (date) {
                var moment = require('moment');
                moment.locale(localStorage.lang);
                return moment(date).fromNow();
            };
        });
})();
