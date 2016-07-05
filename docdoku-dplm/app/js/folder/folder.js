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
                controller: 'FolderController',
                resolve:{
                    conf:function(ConfigurationService,WorkspaceService){
                        return ConfigurationService.checkAtStartup().then(WorkspaceService.getWorkspaces);
                    }
                }
            });
        })

        .controller('FolderController', function ($scope, $q, $location, $routeParams, $filter, ConfirmService, ConfigurationService, FolderService, NotificationService) {

            $scope.tabs={
                selected:
                    $routeParams.entity==='documents' ? 0 :
                        $routeParams.entity==='parts' ? 1 :
                        $routeParams.entity==='unknown' ? 2 :
                            0,
                documents:0,
                parts:1,
                unknown:2
            };

            $scope.folder = FolderService.getFolder({uuid:$routeParams.uuid});

            $scope.configuration = ConfigurationService.configuration;
            $scope.files = [];

            $scope.filters = {
                sync: true,
                modified: true,
                entity:$routeParams.entity
            };

            $scope.counts = {
                documents:0,
                parts:0,
                unknown:0,
                totalFiles:0,
                inProgress:0
            };

            var resetFiles = function(files){
                $scope.files.length = 0;
                $scope.counts.totalFiles = files.length;
                $scope.counts.documents = 0;
                $scope.counts.parts = 0;
                $scope.counts.unknown = 0;
                $scope.counts.inProgress=0;
            };

            var onFile = function(file){
                $scope.counts.inProgress++;
                $scope.counts.documents+=file.document?1:0;
                $scope.counts.parts+=file.part?1:0;
                $scope.counts.unknown+=!file.part && !file.document?1:0;
                if(file.document && $scope.filters.entity == 'documents'){
                    $scope.files.push(file);
                }else if(file.part && $scope.filters.entity == 'parts'){
                    $scope.files.push(file);
                }else if(!file.part && !file.document && $scope.filters.entity == 'unknown'){
                    $scope.files.push(file);
                }
            };

            $scope.refresh = function (forceRefresh) {

                resetFiles([]);

                if(!forceRefresh && FolderService.cache[$scope.folder.uuid]){
                    var files = FolderService.cache[$scope.folder.uuid];
                    resetFiles(files);
                    angular.forEach(files, onFile);
                }else{

                    $scope.loadingFiles = true;
                    FolderService.recursiveReadDir($scope.folder.path).then(function (files) {

                        resetFiles(files);

                        var toCache = [];
                        var statusRequestChain = $q.when();
                        angular.forEach(files,function(file){
                            statusRequestChain = statusRequestChain.then(function(){
                                return FolderService.fetchFileStatus(file).then(function () {
                                    onFile(file);
                                    toCache.push(file);
                                });
                            });
                        });

                        statusRequestChain.then(function(){
                            $scope.loadingFiles = false;
                            FolderService.cache[$scope.folder.uuid] = toCache;
                        });

                    }, function () {
                        $scope.loadingFiles = false;
                    }).then(function(){
                        $scope.folder.newStuff = false;
                    });
                }

            };

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

            var getUploadableFiles = function(){
                return $filter('filter')($scope.files,{modified:true, sync:false});
            };

            $scope.hasUploadableFiles = function(){
                return ($scope.tabs.selected === $scope.tabs.documents || $scope.tabs.selected === $scope.tabs.parts ) && getUploadableFiles().length;
            };

            $scope.syncAll = function(){
                $scope.$broadcast('sync:all');
            };

            $scope.refresh();

        })

        .filter('filterFiles', function () {
            return function (arr, filters) {
                if (!arr) {
                    return [];
                }
                return arr.filter(function (file) {
                    if (!filters.sync && file.sync) {
                        return false;
                    }
                    if (!filters.modified && file.modified) {
                        return false;
                    }
                    return true;

                });
            };
        })

        .controller('FileController', function ($scope, FolderService, AvailableLoaders) {

            $scope.openedFile = false;

            $scope.toggleOpenedFile = function (scope) {
                $scope.openedFile = !$scope.openedFile;
            };

            $scope.fetchStatus = function () {
                $scope.loading = true;
                FolderService.fetchFileStatus($scope.file).then(function () {
                    $scope.loading = false;
                }, function () {
                    $scope.loading = false;
                });
            };

            $scope.onFinish = function () {
                $scope.file.busy = false;
                $scope.file.progress = 0;
            };

            $scope.onProgress = function (progress) {
                $scope.file.progress = progress;
            };

            $scope.isViewable = AvailableLoaders.indexOf($scope.file.path.split('.').pop()) !== -1;



        })

        .directive('filePartActions', function () {

            return {

                templateUrl: 'js/folder/file-part-actions.html',
                scope:false,
                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, NotificationService, PromptService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;
                    $scope.show3D = false;

                    var onError = function(error){
                        $scope.error = error;
                    };

                    $scope.output = [];

                    var onOutput = function(o){
                        $scope.output.push(o);
                    };

                    $scope.$on('sync:all',function(){
                        if(!$scope.file.sync && $scope.file.modified && !$scope.file.busy){
                            $scope.put();
                        }
                    });

                    $scope.put = function () {
                        $scope.file.busy = true;
                        CliService.putCADFile($scope.file.part.workspace, $scope.file.path, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);
                    };

                    $scope.checkout = function () {
                        $scope.file.busy = true;
                        CliService.checkoutPart($scope.file.part, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);
                    };

                    $scope.checkin = function (e) {
                        PromptService.prompt(e, {title:$filter('translate')('CHECKIN_MESSAGE')}).then(function(message){
                            $scope.file.busy = true;
                            CliService.checkinPart($scope.file.part,{path:$scope.folder.path,message: message}, onOutput).then(function () {
                                return $scope.fetchStatus();
                            }, onError, $scope.onProgress).then($scope.onFinish);
                        });
                    };                    

                    $scope.undoCheckout = function () {
                        $scope.file.busy = true;
                        CliService.undoCheckoutPart($scope.file.part, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError).then($scope.onFinish);
                    };

                    $scope.conversionStatus = function () {
                        CliService.getConversionStatus($scope.file.part, onOutput).then(function (conversion) {
                            var message = conversion.pending ? $filter('translate')('PENDING') :
                                conversion.succeed ? $filter('translate')('SUCCESS') :
                                    $filter('translate')('FAIL');
                                onOutput({info:message});
                        },function(){
                            onOutput({info:$filter('translate')('NO_CONVERSION')});
                        });
                    };

                    $scope.toggle3D = function(){
                        $scope.show3D = !$scope.show3D;
                    };

                }

            };
        })



        .directive('fileDocumentActions', function () {

            return {

                templateUrl: 'js/folder/file-document-actions.html',
                scope:false,
                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, PromptService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;

                    var onError = function(error){
                        $scope.error = error;
                    };

                    $scope.output = [];

                    var onOutput = function(o){
                        $scope.output.push(o);
                    };

                    $scope.$on('sync:all',function(){
                        if(!$scope.file.sync && $scope.file.modified && !$scope.file.busy){
                            $scope.put();
                        }
                    });

                    $scope.put = function () {
                        $scope.file.busy = true;
                        CliService.putDocumentFile($scope.file.document.workspace, $scope.file.path, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);

                    };

                    $scope.checkout = function () {
                        $scope.file.busy = true;
                        CliService.checkoutDocument($scope.file.document, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);
                    };

                    $scope.checkin = function (e) {
                        PromptService.prompt(e, {title:$filter('translate')('CHECKIN_MESSAGE')}).then(function(message){
                            $scope.file.busy = true;
                            CliService.checkinDocument($scope.file.document,{path:$scope.folder.path,message:message}, onOutput).then(function () {
                                return $scope.fetchStatus();
                            }, onError, $scope.onProgress).then($scope.onFinish);
                        });

                    };


                    $scope.undoCheckout = function () {
                        $scope.file.busy = true;
                        CliService.undoCheckoutDocument($scope.file.document, onOutput).then(function () {
                            return $scope.fetchStatus();
                        }, onError).then($scope.onFinish);
                    };

                }

            };
        })
        .directive('fileUnknownActions', function () {

            return {

                templateUrl: 'js/folder/file-unknown-actions.html',
                scope:false,
                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, NotificationService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;
                    $scope.newPart = {workspace: $scope.workspaces[0]};
                    $scope.newDocument = {workspace: $scope.workspaces[0]};

                    $scope.form={
                        selected:0,
                        part:0,
                        document:1
                    };

                    var onError = function(error){
                        $scope.error = error;
                    };

                    $scope.output = [];

                    var onOutput = function(o){
                        $scope.output.push(o);
                    };

                    $scope.createPart = function () {
                        $scope.file.busy = true;
                        CliService.createPart($scope.newPart, $scope.file.path, onOutput).then(function () {
                            $scope.newPart = {workspace: $scope.workspaces[0]};
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);
                    };

                    $scope.createDocument = function () {
                        $scope.file.busy = true;
                        CliService.createDocument($scope.newDocument, $scope.file.path, onOutput).then(function () {
                            $scope.newDocument = {workspace: $scope.workspaces[0]};
                            return $scope.fetchStatus();
                        }, onError, $scope.onProgress).then($scope.onFinish);
                    };

                }

            };
        });

})();
