'use strict';

angular.module('dplm.folder',[])
    .config(function($routeProvider){
        $routeProvider.when('/folder/:uuid',{

            templateUrl:'js/folder/folder.html',

            controller:function($scope,$routeParams,ConfigurationService,FolderService){
                
                $scope.folder = FolderService.getFolder($routeParams);

                $scope.configuration = ConfigurationService.configuration;
                $scope.files = [];

                FolderService.recursiveReadDir($scope.folder.path).then(function(files){
                    $scope.files = files;
                }).then(function(){
                    FolderService.fetchFilesStatus($scope.files);
                });
            }

        });
    })

    .controller('FileController', function(){
        
    });