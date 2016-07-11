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

        .controller('FolderController', function ($scope, $q, $location, $routeParams, $filter,
                                                  ConfirmService, ConfigurationService, FolderService,
                                                  RepositoryService, NotificationService) {

            $scope.folder = FolderService.getFolder({uuid:$routeParams.uuid});
            var path = $scope.folder.path;
            var inIndexFiles = [];
            var outOfIndexFiles = [];
            var repositoryIndex;


            $scope.display = {type:'flat'};

            $scope.selected = [];

            $scope.query = {
                limit: 5,
                limits: [5,10,15,50,100],
                page: 1
            };

            $scope.reveal = function ($event) {
                FolderService.reveal(path);
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

            $scope.onPaginateOutOfIndexFiles = function(page, count){
                var start = (page - 1)*count;
                var end  = start + count;
                $scope.outOfIndexFiles = outOfIndexFiles.slice(start, end);
            };

            $scope.onPaginateInIndexFiles = function(page, count){
                var start = (page - 1)*count;
                var end  = start + count;
                $scope.outOfIndexFiles = outOfIndexFiles.slice(start, end);
                $scope.outOfIndexFilesSize = outOfIndexFiles.length;
            };

            RepositoryService.getRepositoryIndex(path)
            .then(function(index){
                repositoryIndex = index;
                return path;
            })
            .then(FolderService.recursiveReadDir)
            .then(function(files){
                inIndexFiles.length = 0;
                outOfIndexFiles.length = 0;
                angular.forEach(files, function(file){
                    if(!repositoryIndex[file+'.digest']){
                        outOfIndexFiles.push(FolderService.createFileObject(file));
                    }else{
                        inIndexFiles.push(FolderService.createFileObject(file));
                    }
                });
            })
            .then($scope.onPaginateOutOfIndexFiles);

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
