angular.module('dplm.settings',[])
    .config(function($routeProvider){
        $routeProvider.when('/settings',{
            controller:function($scope,ConfigurationService,CliService){
                $scope.configuration = ConfigurationService.configuration;
                $scope.save = function(){
                    ConfigurationService.save();
                    CliService.getWorkspaces();
                };
            },
            templateUrl:'js/settings/settings.html'
        })
    });