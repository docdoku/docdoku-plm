(function(){
    'use strict';

    angular.module('dplm.settings', [])

        .config(function ($routeProvider) {
            $routeProvider.when('/settings', {
                controller: 'SettingsController',
                templateUrl: 'js/settings/settings.html'
            });
        })

        .controller('SettingsController', function ($scope, $location, $translate, ConfigurationService, WorkspaceService) {
            $scope.configuration = ConfigurationService.configuration;
            $scope.output = WorkspaceService.output;

            $scope.save = function () {
                ConfigurationService.save();
                ConfigurationService.reset();
                WorkspaceService.reset();
                $location.path('home');
            };

            $scope.onSslChange = function() {
                $scope.configuration.port = $scope.configuration.ssl === true ? 443 : $scope.configuration.port;
            };


        });
})();
