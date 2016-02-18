(function(){

    'use strict';

    angular.module('dplm.services.output',[])
        .directive('output',function(){
            return {
                restrict: 'E',
                scope:{
                    'entry':'='
                },
                templateUrl:'js/components/output/output.html'
            };
        });
})();
