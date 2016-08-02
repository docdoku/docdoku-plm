(function () {

    'use strict';

    angular.module('dplm.services')
        .service('DocdokuAPIService', function ($window, ConfigurationService) {
            var DocdokuPLMClient = $window.require('docdoku-api-js');

            var cookie = null;

            this.setCookie = function (pCookie) {
                cookie = pCookie;
            };

            this.getClient = function () {
                var client = new DocdokuPLMClient();
                client.setOptions({
                    url: ConfigurationService.getHostApiURL(),
                    cookie: cookie
                });
                return client;
            };

        });

})();