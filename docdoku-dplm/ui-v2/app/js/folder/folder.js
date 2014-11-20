'use strict';

angular.module('dplm.folder',[])

.config(function($routeProvider){
    $routeProvider.when('/folder/:uuid',{
        templateUrl:'js/folder/folder.html',
        controller:'FolderController'
    });
})

.controller('FolderController',function($scope,$routeParams,ConfigurationService,FolderService){
    
    $scope.folder = FolderService.getFolder($routeParams);

    $scope.configuration = ConfigurationService.configuration;
    $scope.files = [];
    $scope.openedFile = null;

    $scope.filters = {
        sync:true,
        notSync:true,
        modified:true
    };

    $scope.refresh = function(){
        $scope.files.length = 0;
         FolderService.recursiveReadDir($scope.folder.path).then(function(files){
             angular.forEach(files,function(file){
                $scope.files.push(file);
            });     
        });    
    };


    $scope.toggleOpenedFile = function(file){
        $scope.openedFile = file == $scope.openedFile ? null : file;
    };

    $scope.reveal = function(){
        FolderService.reveal($scope.folder.path);
    };

    $scope.refresh();

}) 

.filter('filterFiles',function(ConfigurationService){
    return function(arr,filters) {
     
        if(!arr){
            return [];
        }

        return arr.filter(function(file){


            if(!filters.sync && file.sync){
                return false;
            }

            if(!filters.notSync && file.notSync){
                return false;
            }

            if(!filters.modified && file.modified){
                return false;
            }

            return true;

      });

    };
})

.controller('FileController', function($scope,FolderService){    
    $scope.fetchStatus = function(){
        $scope.loading = true;
        FolderService.fetchFileStatus($scope.file).then(function(){
            $scope.loading = false;
        },function(){
            $scope.loading = false;
        });
    };
    $scope.fetchStatus();
})

.directive('fileActions', function(){

    return {

        templateUrl: 'js/folder/file-actions.html',

        controller: function($scope, $element, $attrs, $transclude, $timeout, CliService, WorkspaceService) {
            
            $scope.options = {force:true};
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.newPart = {workspace:$scope.workspaces[0]};           

            var onFinish = function(){
                $scope.file.busy = false;
            };

            var onProgress = function(progress){
                $scope.file.progress = progress;
            };

            $scope.checkout = function(){
                $scope.file.busy = true;
                CliService.checkout($scope.file.part,$scope.folder.path,$scope.options).then(function(){
                    return $scope.fetchStatus();                 
                },null,onProgress).then(onFinish);
            };

            $scope.checkin = function(){
                $scope.file.busy = true;
                CliService.checkin($scope.file.part).then(function(){
                    return $scope.fetchStatus();       
                },null,onProgress).then(onFinish);
            };
            
            $scope.put = function(){
                $scope.file.busy = true;
                CliService.put($scope.file.part.workspace,$scope.file.path).then(function(){
                    return $scope.fetchStatus();       
                },null,onProgress).then(onFinish);
            };

            $scope.undoCheckout = function(){
                $scope.file.busy = true;
                CliService.undoCheckout($scope.file.part).then(function(){
                    return $scope.fetchStatus();       
                }).then(onFinish);
            };

            $scope.createPart = function(){            
                $scope.file.busy = true;
                CliService.createPart($scope.newPart,$scope.file.path).then(function(){
                    $scope.newPart = {workspace:$scope.workspaces[0]};           
                    return $scope.fetchStatus();      
                }).then(onFinish);

            };

        }

    };
});