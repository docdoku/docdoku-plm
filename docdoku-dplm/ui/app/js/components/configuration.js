(function(){

    'use strict';

    angular.module('dplm.services.configuration', [])

        .service('ConfigurationService', function ($q, $log, $filter, $location, NotificationService) {

            var _this = this;

            this.error = 'CONFIG_SERVICE_ERROR';

            this.configuration = JSON.parse(localStorage.configuration || '{}');

            this.save = function () {
                localStorage.configuration = JSON.stringify(_this.configuration);
            };

            var checkForJava = function(){

                var deferred = $q.defer();

                try{

                    NotificationService.toast($filter('translate')('CHECKING_FOR_JAVA'));

                    var spawn = require('child_process').spawn(_this.configuration.java || 'java', ['-version']);

                    spawn.on('error', function (err) {
                        $log.error(err);
                        deferred.reject();
                    });

                    spawn.stderr.on('data', function (data) {
                        data = data.toString().split('\n')[0];
                        var javaVersion = new RegExp('java|openjdk version').test(data) ? data.split(' ')[2].replace(/"/g, '') : false;
                        if (javaVersion && javaVersion >= '1.7') {
                            $log.info('Java version found' + javaVersion);
                            deferred.resolve();
                        } else {
                            deferred.reject();
                        }
                    });

                } catch(e){
                    deferred.reject(e);
                }

                return deferred.promise;
            };

            var checkAtStartupPromise;

            this.checkAtStartup = function () {

                if(checkAtStartupPromise){
                    return checkAtStartupPromise;
                }

                var deferred = $q.defer();
                checkAtStartupPromise = deferred.promise;

                if (!_this.configuration.user || !_this.configuration.password) {
                    NotificationService.toast($filter('translate')('CONFIGURATION_MISSING'));
                    $location.path('settings');
                    deferred.reject();
                }else{
                    checkForJava().then(function(){
                        deferred.resolve();
                    },function(){
                        NotificationService.toast($filter('translate')('NO_SUITABLE_JAVA'));
                        $location.path('settings');
                        deferred.reject();
                    });
                }

                return deferred.promise;

            };

            this.reset = function(){
                checkAtStartupPromise = null;
            };

            this.resolveUrl = function () {
                var protocole = this.configuration.ssl ? 'https' : 'http';
                return protocole + '://' + this.configuration.host + ':' + this.configuration.port;
            };

        });
})();
