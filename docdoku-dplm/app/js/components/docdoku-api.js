(function () {

    'use strict';

    angular.module('dplm.services')
        .service('DocdokuAPIService', function ($window, ConfigurationService) {

            var DocdokuPlmApi = $window.require('docdoku-plm-api');

            var client = new DocdokuPlmApi.ApiClient();
            client.basePath = ConfigurationService.getHostApiURL();

            var token = null;

            this.getApi = function () {
                return DocdokuPlmApi;
            };

            this.getClient = function () {
                return client;
            };

            this.setToken = function (pToken) {
                token = pToken;
                if (token) {
                    client.defaultHeaders.Authorization = 'Bearer ' + token;
                } else {
                    delete client.defaultHeaders.Authorization;
                }
            };

        });

})();