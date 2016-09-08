(function () {

    'use strict';

    angular.module('dplm.services')
        .service('DocdokuAPIService', function ($window, ConfigurationService) {
            var DocdokuPLMClient = $window.require('docdoku-api-js');

            var cookie = null;

            var getClient = function () {
                var client = new DocdokuPLMClient();
                client.setOptions({
                    url: ConfigurationService.getHostApiURL(),
                    cookie: cookie
                });
                return client;
            };

            this.setCookie = function (pCookie) {
                cookie = pCookie;
            };

            this.getApi = function(){
                return getClient().getApi()
            }

        });

})();