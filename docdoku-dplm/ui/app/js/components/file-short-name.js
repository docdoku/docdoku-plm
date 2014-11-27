(function(){
    'use strict';

    angular.module('dplm.filters.fileshortname', [])
        .filter('fileshortname', function () {
            return function (path) {
                return path.replace(/^.*[\\\/]/, '');
            };
        });

})();
