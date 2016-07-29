(function(){
    'use strict';

    angular.module('dplm.filters.strings', [])

        .filter('nospace', function () {
            return function (value) {
                return (!value) ? '' : value.replace(/ /g, '');
            };
        });

})();
