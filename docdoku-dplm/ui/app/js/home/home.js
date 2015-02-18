(function(){

    'use strict';

    angular.module('dplm.home', [])
        .config(function ($routeProvider) {
            $routeProvider.when('/', {
                controller: function ($scope, $filter, ConfigurationService,FolderService,WorkspaceService) {
                    $scope.configuration = ConfigurationService.configuration;
                    $scope.save = ConfigurationService.save;
                    $scope.folders = $filter('filter')(FolderService.folders,{favorite:true});
                    $scope.workspaces = WorkspaceService.getLastVisitedWorkspaces();
                },
                resolve:{
                    conf:function(ConfigurationService,WorkspaceService,CliService,$translate){
                        return ConfigurationService.checkAtStartup()
                            .then(WorkspaceService.getWorkspaces)
                            .then(CliService.fetchAccount)
                            .then(function(account){
                                localStorage.lang = account.language || 'en';
                                $translate.use(localStorage.lang);
                            });
                    }
                },
                templateUrl: 'js/home/home.html'
            });
        })
        .controller('FolderDetailsController',function($scope,FolderService){

            $scope.details = {
                count: 0
            };

            FolderService.getFilesCount($scope.folder.path).then(function(count){
                $scope.details.count = count;
            });

        });

})();
