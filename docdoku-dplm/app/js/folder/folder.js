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

        .controller('FolderController', function ($scope, $q, $location, $routeParams, $filter, $mdDialog,
                                                  ConfirmService, ConfigurationService, FolderService,
                                                  RepositoryService, NotificationService) {

            $scope.folder = FolderService.getFolder({uuid:$routeParams.uuid});

            var folderPath = $scope.folder.path;
            var allFiles = [];
            var filteredFiles = [];
            var repositoryIndex;

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
                FolderService.reveal(folderPath);
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
                    var index = RepositoryService.getFileIndex(repositoryIndex, path);

                    if($scope.pattern && !file.path.match($scope.pattern)){
                       return false;
                    }

                    if(!hasFilter('OUT_OF_INDEX') && !index){
                        return false;
                    }

                    if(!hasFilter('DOCUMENTS') && index.id){
                        return false;
                    }

                    if(!hasFilter('PARTS') && index && index.partNumber){
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
                    file.stat = FolderService.getFileSize(path);
                    return file;
                });

                $scope.filteredFilesCount = filteredFiles.length;
                $scope.paginate(1,10);
            };

            $scope.fetchFolder = function(){
                RepositoryService.getRepositoryIndex(folderPath)
                    .then(function(index){
                        repositoryIndex = index;
                        return folderPath;
                    })
                    .then(FolderService.recursiveReadDir)
                    .then(function(files){
                        $scope.totalFilesInFolder = files.length;
                        allFiles = files;
                        $scope.search('');
                    });
            };

            $scope.createPart = function(file){
                $mdDialog.show({
                    templateUrl: 'js/folder/create-part.html',
                    fullscreen: true,
                    controller:'PartCreationCtrl',
                    locals:{
                        file:file,
                        folderPath:folderPath
                    }
                });
            };

            $scope.createDocument = function(file){
                $mdDialog.show({
                    templateUrl: 'js/folder/create-document.html',
                    fullscreen: true,
                    controller:'DocumentCreationCtrl',
                    locals:{
                        file:file,
                        folderPath:folderPath
                    }
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

        })

        .controller('DocumentCreationCtrl', function ($scope,$mdDialog, WorkspaceService, UploadService, RepositoryService, file, folderPath) {

            $scope.file = file;
            $scope.workspaces = WorkspaceService.workspaces;

            var onError = function(error){
                $scope.creating = false;
                $scope.error = error;
            };

            $scope.close = $mdDialog.hide;

            $scope.create = function(){
                $scope.creating = true;
                WorkspaceService.createDocumentInWorkspace($scope.document).then(function(document){
                    $scope.document = document;
                    return UploadService.uploadFileToDocument(folderPath, file.path, document);
                }).then(function(){
                    var newIndex = RepositoryService.saveDocumentToIndex(folderPath,file.path,$scope.document);
                    file.index = RepositoryService.getFileIndex(newIndex,file.path);
                    $mdDialog.hide();
                }).catch(onError);
            };

        })

        .controller('PartCreationCtrl', function ($scope, $mdDialog,
                                                  WorkspaceService, UploadService, RepositoryService, file, folderPath) {

            $scope.file = file;
            $scope.workspaces = WorkspaceService.workspaces;

            var onError = function(error){
                $scope.creating = false;
                $scope.error = error;
            };

            $scope.close = $mdDialog.hide;

            $scope.create = function(){
                $scope.creating = true;
                $scope.error = null;
                WorkspaceService.createPartInWorkspace($scope.part).then(function(part){
                    $scope.part = part;
                    return UploadService.uploadNativeCADFile(folderPath, file.path, part);
                }).then(function(){
                    var newIndex = RepositoryService.savePartToIndex(folderPath,file.path,$scope.part);
                    file.index = RepositoryService.getFileIndex(newIndex,file.path);
                    $mdDialog.hide();
                })
                .catch(onError);
            };
        });

})();
