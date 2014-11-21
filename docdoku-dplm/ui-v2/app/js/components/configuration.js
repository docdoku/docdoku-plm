'use strict';

angular.module('dplm.services.configuration', [])

    .service('ConfigurationService', function ($q, $log) {

        var _this = this;

        this.error = 'CONFIG_SERVICE_ERROR';

        this.configuration = JSON.parse(localStorage.configuration || '{}');

        this.save = function () {
            localStorage.configuration = JSON.stringify(_this.configuration);
        }
        this.checkAtStartup = function () {
            $log.info('Checking for valid configuration');
            return $q(function (resolve, reject) {
                if (_this.configuration.user && _this.configuration.password) {
                    resolve();
                } else {
                    $log.error('Configuration missing, rejecting')
                    reject(_this.error);
                }
            });
        }
    });