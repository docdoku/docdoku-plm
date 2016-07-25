(function() {

    'use strict';

    angular.module('dplm.services.auth', [])
        .service('AuthService',function($q, $translate,
                                        ConfigurationService, DocdokuAPIService, DBService, WorkspaceService){

            var self = this;

            this.user = {
                login:ConfigurationService.configuration.login
            };

            this.login = function() {

                WorkspaceService.reset();

                var deferred = $q.defer();

                DocdokuAPIService.getClient().getApi()
                    .then(function(api){
                        return api.auth.login({
                            body:{
                                login:ConfigurationService.configuration.login,
                                password:ConfigurationService.configuration.password
                            }
                        });
                    }, deferred.reject).then(function(response){
                        var headers = response.headers;

                        var cookie = headers['set-cookie'][0];
                        DocdokuAPIService.setCookie(cookie);

                        angular.copy(response.obj,self.user);
                        var lang = self.user.language;
                        $translate.use(lang);

                        deferred.resolve(self.user);

                    }, deferred.reject);

                return deferred.promise;
            };

            this.logout = function(){
                var deferred = $q.defer();
                DocdokuAPIService.getClient().getApi().then(function(api){

                    angular.copy({},self.user);
                    ConfigurationService.deleteAuth();
                    DocdokuAPIService.setCookie(null);
                    DBService.removeDb();

                    api.auth.logout().then(deferred.resolve,deferred.reject);
                });

                return deferred.promise;
            };

        });

})();