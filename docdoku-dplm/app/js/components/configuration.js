(function(){

    'use strict';

    angular.module('dplm.services.configuration', [])

        .service('ConfigurationService', function () {

            var _this = this;

            this.error = 'CONFIG_SERVICE_ERROR';

            this.configuration = JSON.parse(localStorage.configuration || '{"port":443,"host":"docdokuplm.net","ssl":true}');

            this.save = function () {
                localStorage.configuration = JSON.stringify(_this.configuration);
            };

            this.hasAuth = function(){
                return this.configuration.login && this.configuration.password && this.configuration.host;
            };

            this.deleteAuth = function(){
                delete this.configuration.login;
                delete this.configuration.password;
                this.save();
            };

            this.resolveUrl = function () {
                var protocol = this.configuration.ssl ? 'https' : 'http';
                return protocol + '://' + this.configuration.host + ':' + this.configuration.port + '/api';
            };

        });
})();
