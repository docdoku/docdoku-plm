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
            var allFiles = [];
            var filteredFiles = [];
            var repositoryIndex;

            $scope.selected = [];

            $scope.query = {
                limit: 10,
                limits: [10,20,50,100, {
                    label: 'All',
                    value: function () {
                        return filteredFiles.length;
                    }
                }],
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

            $scope.paginate = function(page, count){
                var start = (page - 1)*count;
                var end  = start + count;
                $scope.displayedFiles = filteredFiles.slice(start, end);
            };

            $scope.search = function(pattern){
                filteredFiles = allFiles.filter(function(file){
                    return file.match(pattern);
                })
                .map(function(path){
                    var file = FolderService.createFileObject(path);
                    file.index = RepositoryService.getFileIndex(repositoryIndex, path);
                    return file;
                });

                $scope.filteredFilesCount = filteredFiles.length;
                $scope.paginate(1,10);
            };

            $scope.fetchFolder = function(){
                RepositoryService.getRepositoryIndex(path)
                    .then(function(index){
                        repositoryIndex = index;
                        return path;
                    })
                    .then(FolderService.recursiveReadDir)
                    .then(function(files){
                        $scope.totalFilesInFolder = files.length;
                        allFiles = files;
                        $scope.search('');
                    });
            };

            $scope.fetchFolder();

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
