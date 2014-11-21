angular.module('dplm.home', [])
    .config(function ($routeProvider) {
        $routeProvider.when('/', {
            controller: function ($scope, ConfigurationService) {
                $scope.configuration = ConfigurationService.configuration;
                $scope.save = ConfigurationService.save;
            },
            templateUrl: 'js/home/home.html'
        })
    });