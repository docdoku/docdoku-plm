(function(){
    'use strict';

    angular.module('dplm.filters.timeago', [])
        .filter('timeago', function ($window) {
            return function (date) {
                var moment = $window.require('moment');
                moment.locale($window.localStorage.lang);
                return moment(date).fromNow();
            };
        });
})();
