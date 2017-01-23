(function () {

    'use strict';

    angular.module('dplm.services')
        .service('AuthService', function ($q, $translate,
                                          ConfigurationService, DocdokuAPIService, DBService, WorkspaceService, FolderService) {

            var user = {};
            this.user = user;

            var api = DocdokuAPIService.getApi();
            var authApi = new api.AuthApi(DocdokuAPIService.getClient());

            this.login = function (login, password) {

                WorkspaceService.reset();

                var deferred = $q.defer();

                authApi.login(api.LoginRequestDTO.constructFromObject({
                    login: login,
                    password: password
                }), function (err, account, response) {
                    if (err) {
                        console.log('Error while login');
                        console.log(err);
                        return deferred.reject(err);
                    }
                    DocdokuAPIService.setToken(response.headers.jwt);
                    angular.copy(account, user);
                    $translate.use(user.language);
                    deferred.resolve(user);
                });

                return deferred.promise;
            };

            this.logout = function () {
                var deferred = $q.defer();

                angular.copy({}, user);

                ConfigurationService.deleteAuth();
                DocdokuAPIService.setToken(null);
                DBService.removeDb();
                FolderService.removeFolders();
                WorkspaceService.resetWorkspaceSyncs();

                authApi.logout(function (err, data, response) {
                    if (err) {
                        return deferred.reject(err);
                    }
                    return deferred.resolve(data);
                });

                return deferred.promise;
            };

        });

})();