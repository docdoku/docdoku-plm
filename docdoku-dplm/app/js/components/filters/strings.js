(function () {
    'use strict';

    angular.module('dplm.filters')

        .filter('nospace', function () {
            return function (value) {
                return (!value) ? '' : value.replace(/ /g, '');
            };
        });

})();
