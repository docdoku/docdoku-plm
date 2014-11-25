angular.module('dplm.home', [])
    .config(function ($routeProvider) {
        $routeProvider.when('/', {
            controller: function ($scope, $filter, ConfigurationService,FolderService) {
                $scope.configuration = ConfigurationService.configuration;
                $scope.save = ConfigurationService.save;
                $scope.folders = $filter('filter')(FolderService.folders,{favorite:true});
            },
            resolve:{
                conf:function(ConfigurationService,WorkspaceService){
                    return ConfigurationService.checkAtStartup().then(WorkspaceService.getWorkspaces);
                }
            },
            templateUrl: 'js/home/home.html'
        })
    })
    .controller('FolderDetailsController',function($scope,FolderService){

        $scope.details = {
            count: 0
        };

        FolderService.getFilesCount($scope.folder.path).then(function(count){
            $scope.details.count = count;
        });

    });
;