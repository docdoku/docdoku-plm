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

    FolderService.recursiveReadDir($scope.folder.path).then(function(files){
        angular.forEach(files,function(fileName){
            $scope.files.push({
                path:fileName
            });
        });        
    }).then(function(){
        FolderService.fetchFilesStatus($scope.files);
    });

}) 
.controller('FileController', function(){
    
});