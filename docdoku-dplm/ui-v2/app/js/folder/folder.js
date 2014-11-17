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

    FolderService.recursiveReadDir($scope.folder.path).then(function(files){
         angular.forEach(files,function(file){
            $scope.files.push(file);
        });     
    });         

    $scope.toggleOpenedFile = function(file){
        $scope.openedFile = file == $scope.openedFile ? null : file;
    };

    $scope.reveal = function(){
        FolderService.reveal($scope.folder.path);
    };

}) 

.controller('FileController', function($scope,FolderService){
    $scope.fetchStatus = function(){
        $scope.loading = true;
        FolderService.fetchFileStatus($scope.file).then(function(){
            $scope.loading = false;
        },function(){
            $scope.loading = false;
            $scope.file.notSync = true;
        });
    };
    $scope.fetchStatus();
})

.directive('fileActions', function(){

    return {

        templateUrl: 'js/folder/file-actions.html',

        controller: function($scope, $element, $attrs, $transclude, $timeout, CliService) {
            
            $scope.options = {force:true};

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

        }

    };
});