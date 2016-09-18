(function () {
    'use strict';
    angular.module('dplm.dialogs')
        .controller('LoginCtrl', function ($scope, $mdDialog, ConfigurationService, AuthService, WorkspaceService, xhrFrom) {

            var configuration = ConfigurationService.configuration;

            $scope.configuration = ConfigurationService.configuration;
            $scope.xhrFrom = xhrFrom;

            $scope.connect = function () {
                $scope.xhrFrom = null;
                if (ConfigurationService.hasAuth()) {
                    $scope.loggingIn = true;
                    ConfigurationService.save();
                    AuthService.login(configuration.login, configuration.password)
                        .then(WorkspaceService.getWorkspaces)
                        .then($mdDialog.hide)
                        .catch(function (xhr) {
                            $scope.xhrFrom = xhr;
                        })
                        .finally(function () {
                            $scope.loggingIn = false;
                        });
                }
            };

        });
})();