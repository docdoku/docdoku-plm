(function() {

    'use strict';

    angular.module('dplm.filters.join', [])
        .filter('join', function () {
            return function (input, sep) {
                if(angular.isArray(input)){
                    return input.join(sep);
                }
                return null;
            };
        });

})();