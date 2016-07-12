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
            var crypto = require('crypto');
            var fs = require('fs');


            $scope.selected = [];
            var translate = $filter('translate');
            $scope.filters = [
                { name: translate('CHECKED_OUT') , value:'CHECKED_OUT'},
                { name: translate('CHECKED_IN'), value:'CHECKED_IN'},
                { name: translate('DOCUMENTS') , value:'DOCUMENTS'},
                { name: translate('PARTS') , value:'PARTS'},
                { name: translate('OUT_OF_INDEX'), value:'OUT_OF_INDEX' }
            ];

            var hasFilter = function(value){
                return $scope.selectedFilters.indexOf(value) !== -1;
            };

            $scope.selectedFilters = $scope.filters.map(function(filter){
                return filter.value;
            });

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

            $scope.search = function(){

                filteredFiles = allFiles.filter(function(path){
                    var file = FolderService.createFileObject(path);
                    var index = RepositoryService.getFileIndex(repositoryIndex, path) ;

                    if($scope.pattern && !file.path.match($scope.pattern)){
                       return false;
                    }

                    if(!hasFilter('OUT_OF_INDEX') && !index){
                        return false;
                    }

                    if(!hasFilter('DOCUMENTS') && index.id){
                        return false;
                    }

                    if(!hasFilter('PARTS') && index.partNumber){
                        return false;
                    }

                    if(!hasFilter('DOCUMENTS') && index && index.id) {
                        return false;
                    }

                    if(!hasFilter('PARTS') && index && index.number){
                        return false;
                    }

                    return true;
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
