(function () {
    'use strict';

    angular.module('dplm.filters')

        .filter('join', function () {
            return function (input, sep) {
                if (angular.isArray(input)) {
                    return input.join(sep);
                }
                return null;
            };
        })

        .filter('last', function () {
            return function (arr) {
                return arr.length ? arr[arr.length - 1] : null;
            };
        });

})();
