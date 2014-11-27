(function(){

    'use strict';

    angular.module('dplm.filters.last', [])
        .filter('last', function () {
            return function (arr) {
                return arr.length ? arr[arr.length - 1] : null;
            };
        });

})();