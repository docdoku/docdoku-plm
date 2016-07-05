angular.module('dplm.menu', [])
    .directive('menuButton',function(){
        return {
            restrict:'E',
            templateUrl:'js/menu/menu-button.html',
            scope:false
        };
    })
    .controller('MenuController', function ($scope,FolderService,ConfigurationService) {
        $scope.configuration = ConfigurationService.configuration;
        $scope.toggleFolders = function(){
            $scope.foldersExpanded=!$scope.foldersExpanded;
        };
        $scope.toggleWorkspaces = function(){
            $scope.workspacesExpanded=!$scope.workspacesExpanded;
        };
        $scope.onFileDropped = function(path){
            if(path){
                FolderService.add(path);
            }
        };

    })

    .controller('FolderMenuController', function ($scope) {

        $scope.onDrop = function () {
        };

    })
    .controller('WorkspaceMenuController', function ($scope, WorkspaceService) {

        $scope.onDrop = function () {
        };

        $scope.refreshWorkspaces = function(){
            WorkspaceService.reset();
            WorkspaceService.getWorkspaces();
        };

    });