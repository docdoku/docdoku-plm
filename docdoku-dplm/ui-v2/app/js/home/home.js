angular.module('dplm.home', [])
    .config(function ($routeProvider) {
        $routeProvider.when('/', {
            controller: function ($scope, ConfigurationService) {
                $scope.configuration = ConfigurationService.configuration;
                $scope.save = ConfigurationService.save;
            },
            resolve:{
                conf:function(ConfigurationService,WorkspaceService){
                    return ConfigurationService.checkAtStartup().then(WorkspaceService.getWorkspaces);
                }
            },
            templateUrl: 'js/home/home.html'
        })
    });