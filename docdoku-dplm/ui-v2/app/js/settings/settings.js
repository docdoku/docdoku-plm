angular.module('dplm.settings',[])
    .config(function($routeProvider){
        $routeProvider.when('/settings',{
            controller:function($scope,ConfigurationService){
                $scope.configuration = ConfigurationService.configuration;
                $scope.save = ConfigurationService.save;
            },
            templateUrl:'js/settings/settings.html'
        })
    });