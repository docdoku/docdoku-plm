(function(){
    'use strict';
    angular.module('dplm.directives.scrollend', []).directive('onScrollEnd', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var raw = element[0];
                element.bind('scroll', function () {
                    if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight) {
                        scope.$apply(attrs.onScrollEnd);
                    }
                });
            }
        };
    });
})();
