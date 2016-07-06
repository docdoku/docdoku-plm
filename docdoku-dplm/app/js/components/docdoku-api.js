(function() {

    'use strict';

    angular.module('dplm.services.api', [])
        .service('DocdokuAPIService',function($window){
            var DocdokuPLMClient = $window.require('docdoku-api-js');
            this.client = new DocdokuPLMClient();
        });

})();