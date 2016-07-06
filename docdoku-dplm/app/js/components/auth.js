(function() {

    'use strict';

    angular.module('dplm.services.auth', [])
        .service('AuthService',function($q, ConfigurationService, DocdokuAPIService){

            var self = this;
            var client = DocdokuAPIService.client;

            this.user = {};

            var data ={
                cookie:null
            };

            this.data = data;

            this.login = function() {

                var deferred = $q.defer();

                client.setOptions({url:ConfigurationService.resolveUrl()});

                client.getApi()
                    .then(function(api){
                        return api.auth.login({
                            body:{
                                login:ConfigurationService.configuration.login,
                                password:ConfigurationService.configuration.password
                            }
                        })
                    }, deferred.reject).then(function(response){

                        var headers = response.headers;
                        data.cookie = headers['set-cookie'][0];
                        angular.copy(response.obj,self.user);
                        deferred.resolve(self.user);

                    }, deferred.reject);

                return deferred.promise;
            };

            this.logout = function(){
                var deferred = $q.defer();
                ConfigurationService.deleteAuth();
                client.getApi().then(function(api){
                    angular.copy({},self.user);
                    api.auth.logout().then(deferred.resolve,deferred.reject);
                });
                return deferred.promise;
            };

        });

})();