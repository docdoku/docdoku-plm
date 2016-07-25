(function(){
    'use strict';

    angular.module('dplm.settings', [])

        .config(function ($routeProvider) {
            $routeProvider.when('/settings', {
                controller: 'SettingsController',
                templateUrl: 'js/settings/settings.html'
            });
        })

        .controller('SettingsController', function ($scope, $location, $translate, ConfigurationService, WorkspaceService, FolderService) {

            $scope.configuration = ConfigurationService.configuration;
            $scope.output = WorkspaceService.output;

            $scope.save = function () {
                ConfigurationService.save();
                ConfigurationService.reset();
                WorkspaceService.reset();
                $location.path('home');
            };

        });
})();
