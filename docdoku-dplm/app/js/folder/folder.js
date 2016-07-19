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
            var translate = $filter('translate');

            var hasFilter = function(code){
                return $scope.filters.filter(function(filter){
                        return filter.code === code && filter.value;
                    }).length>0;
            };

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

            $scope.selected = [];

            $scope.filters = [
                { name: translate('CHECKED_OUT') , code:'CHECKED_OUT', value:true},
                { name: translate('CHECKED_IN'), code:'CHECKED_IN', value:true},
                { name: translate('DOCUMENTS') , code:'DOCUMENTS', value:true},
                { name: translate('PARTS') , code:'PARTS', value:true},
                { name: translate('OUT_OF_INDEX'), code:'OUT_OF_INDEX', value:true }
            ];

            $scope.sync = {
                running:false,
                progress:0,
                total:0
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

                    if($scope.pattern && !path.match($scope.pattern)){
                        return false;
                    }

                    var index = RepositoryService.getFileIndex(repositoryIndex, path);

                    if(!hasFilter('OUT_OF_INDEX') && !index){
                        return false;
                    }

                    if(!hasFilter('DOCUMENTS') && index && index.id){
                        return false;
                    }

                    if(!hasFilter('PARTS') && index && index.number){
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

            $scope.prevent = function($event){
                $event.preventDefault(); $event.stopPropagation(); return false;
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
                        $scope.search();
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

            $scope.updateIndex = function(){
                $scope.sync.running = true;
                RepositoryService.syncIndex(folderPath)
                    .then($scope.fetchFolder, function(){
                        // todo handle error
                    },function(sync){
                        $scope.sync.total = sync.total;
                        $scope.sync.progress = sync.progress;
                    }).catch(function(){
                        // todo handle error
                    }).finally(function(){
                        $scope.sync.running = false;
                        $scope.sync.total = 0;
                        $scope.sync.progress = 0;
                    });
            };

            $scope.toggleFilters = function(state){
                angular.forEach($scope.filters,function(filter){
                    filter.value = state;
                });
                $scope.search();
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
                if(typeof error === 'object' && error.statusText){
                    $scope.error =error.statusText;
                }else if(typeof error === 'string'){
                    $scope.error = error;
                }
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
