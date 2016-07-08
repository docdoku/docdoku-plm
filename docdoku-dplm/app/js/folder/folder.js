(function(){

    'use strict';

    angular.module('dplm.folder', [])

        .config(function ($routeProvider) {

            $routeProvider
            .when('/folder/:uuid', {
                template: '',
                controller: function($location,$routeParams){
                    $location.path('folder/'+$routeParams.uuid+'/documents');
                }
            })
            .when('/folder/:uuid/:entity', {
                templateUrl: 'js/folder/folder.html',
                controller: 'FolderController'
            });
        })

        .controller('FolderController', function ($scope, $q, $location, $routeParams, $filter, ConfirmService, ConfigurationService, FolderService, RepositoryService, NotificationService) {

            $scope.reveal = function ($event) {
                FolderService.reveal($scope.folder.path);
                $event.stopPropagation();
            };

            $scope.toggleFavorite = function () {
                $scope.folder.favorite = !$scope.folder.favorite;
                FolderService.save();
            };

            $scope.delete = function(ev){
                ConfirmService.confirm(ev,{
                    content:$filter('translate')('DELETE_FOLDER_CONFIRM_TITLE')
                }).then(function(){
                    FolderService.delete($scope.folder);
                    NotificationService.toast($filter('translate')('DELETE_FOLDER_CONFIRMED'));
                    $location.path('/');
                });
            };
            $scope.display = {type:'flat'};
            $scope.folder = FolderService.getFolder({uuid:$routeParams.uuid});

            var path = $scope.folder.path;

            $scope.outOfIndexFiles = [];
            $scope.inIndexFiles = [];

            RepositoryService.getRepositoryIndex(path)
                .then(function(index){
                    $scope.repositoryIndex = index;
                    return path;
                })
                .then(FolderService.recursiveReadDir)
                .then(function(files){
                    $scope.inIndexFiles.length = 0;
                    $scope.outOfIndexFiles.length = 0;

                    angular.forEach(files, function(file){
                        if(!$scope.repositoryIndex[file+'.digest']){
                            $scope.outOfIndexFiles.push(file);
                        }else{
                            $scope.inIndexFiles.push(file);
                        }
                    });
                });
        })
        .controller('AddFolderCtrl', function ($scope, $mdDialog, FolderService) {

            $scope.search = function($ev,files){
                var file = files[0];
                if(file){
                    FolderService.add(file.path);
                    $mdDialog.hide(file.path);
                }
            };

            $scope.close = function(){
                $mdDialog.hide();
            };
        });

})();
