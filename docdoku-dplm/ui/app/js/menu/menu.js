angular.module('dplm.menu', [])
    .directive('menuButton',function(){
        return {
            restrict:'E',
            templateUrl:'js/menu/menu-button.html',
            scope:false
        };
    })
    .controller('MenuController', function ($scope,FolderService) {
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
    .controller('WorkspaceMenuController', function ($scope) {

        $scope.onDrop = function () {
        };

    });