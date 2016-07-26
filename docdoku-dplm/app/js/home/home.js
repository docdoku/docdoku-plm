(function(){

    'use strict';

    angular.module('dplm.home', [])

        .config(function ($routeProvider) {
            $routeProvider.when('/', {
                controller: 'HomeCtrl',
                templateUrl: 'js/home/home.html'
            });
        })

        .controller('HomeCtrl',function ($scope, $mdMedia, ConfigurationService, FolderService, WorkspaceService,RepositoryService) {
            $scope.$mdMedia = $mdMedia;
            $scope.configuration = ConfigurationService.configuration;
            $scope.folders = FolderService.folders;
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.modifiedFiles = {};
            angular.forEach($scope.folders,function(folder){
                $scope.modifiedFiles[folder.path] = RepositoryService.getFilesWithModifications(folder.path);
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
