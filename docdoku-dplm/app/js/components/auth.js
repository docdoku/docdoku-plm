(function() {

    'use strict';

    angular.module('dplm.services.auth', [])
        .service('AuthService',function($q, $translate,
                                        ConfigurationService, DocdokuAPIService, DBService, WorkspaceService){

            var user = {};
            this.user = user;

            this.login = function(login,password) {

                WorkspaceService.reset();

                var deferred = $q.defer();

                DocdokuAPIService.getClient().getApi()
                    .then(function(api){
                        return api.auth.login({
                            body:{
                                login:login,
                                password:password
                            }
                        });
                    }, deferred.reject).then(function(response){
                        var headers = response.headers;

                        var cookie = headers['set-cookie'][0];
                        DocdokuAPIService.setCookie(cookie);

                        angular.copy(response.obj,user);
                        var lang = user.language;
                        $translate.use(lang);

                        deferred.resolve(user);

                    }, deferred.reject);

                return deferred.promise;
            };

            this.logout = function(){
                var deferred = $q.defer();
                DocdokuAPIService.getClient().getApi().then(function(api){

                    angular.copy({},user);
                    ConfigurationService.deleteAuth();
                    DocdokuAPIService.setCookie(null);
                    DBService.removeDb();

                    api.auth.logout().then(deferred.resolve,deferred.reject);
                });

                return deferred.promise;
            };

        });

})();