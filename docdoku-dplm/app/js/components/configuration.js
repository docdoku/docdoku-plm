(function () {

    'use strict';

    angular.module('dplm.services')

        .service('ConfigurationService', function ($window) {

            var _this = this;

            this.error = 'CONFIG_SERVICE_ERROR';

            this.configuration = JSON.parse($window.localStorage.configuration || '{"port":443,"host":"docdokuplm.net","ssl":true}');

            this.save = function () {
                _this.configuration.protocol = _this.configuration.ssl ?
                    'https' : 'http';
                _this.configuration.port = _this.configuration.port ?
                    _this.configuration.port : _this.configuration.ssl ?
                    '443' : '80';
                $window.localStorage.configuration = JSON.stringify(_this.configuration);
            };

            this.hasAuth = function () {
                return _this.configuration.login && _this.configuration.password && _this.configuration.host;
            };

            this.deleteAuth = function () {
                delete _this.configuration.login;
                delete _this.configuration.password;
                _this.save();
            };

            this.getHostUrl = function () {
                return _this.configuration.protocol + '://' + _this.configuration.host + ':' + _this.configuration.port;
            };


            this.getHostApiURL = function () {
                return _this.getHostUrl() + '/api';
            };

            this.getFileApiURL = function () {
                return _this.getHostApiURL() + '/files';
            };

            this.getHttpFormRequestOpts = function () {
                return {
                    hostname: _this.configuration.host,
                    port: _this.configuration.port,
                    protocol: _this.configuration.protocol + ':',
                    auth: _this.configuration.login + ':' + _this.configuration.password
                };
            };
        });
})();
