(function(){

    'use strict';

    angular.module('dplm.workspace.documents', [])

        .controller('WorkspaceDocumentsController', function ($scope, $q, $filter, $window, $timeout, $routeParams, CliService, ConfigurationService, FolderService, NotificationService) {

            $scope.path = $routeParams.path;
            $scope.decodedPath = $filter('decodePath')($routeParams.path);
            $scope.remoteFolders = [];
            $scope.pathParts = [];

            if($scope.path){
                var parts = $scope.path.split(':');
                var fullPath='';
                angular.forEach(parts,function(part){
                    if(fullPath){
                        fullPath+=':';
                    }
                    fullPath+=part;
                    $scope.pathParts.push(fullPath);
                });
            }

            $scope.action = {
                selected:
                    $routeParams.action==='folders'?0:
                    $routeParams.action==='checkedOut'?1:
                    0,
                folders:0,
                checkedOut:1
            };

            $scope.folders = FolderService.folders;
            $scope.options = {force: true};
            $scope.folder = angular.copy($scope.folders[0])||{};
            $scope.documents = [];
            $scope.loadingDocuments = true;
            $scope.loadingMore = false;
            $scope.openedDocument = null;

            var onListResults = function (documents) {
                $scope.loadingDocuments = false;
                $scope.loadingMore = false;
                angular.forEach(documents, function (document) {
                    $scope.documents.push(document);
                });
            };

            var getDocumentsRevisions = function () {
                return CliService.getDocumentsRevisionsInFolder($scope.workspace, $filter('decodePath')($scope.path))
                    .then(onListResults);
            };

            var getCheckedOutDocumentsRevisions = function(){
                return CliService.getCheckedOutDocumentsRevisions($scope.workspace, $filter('decodePath')($scope.path))
                    .then(onListResults);
            };
           
            $scope.toggleOpenedDocument = function (document) {
                $scope.openedDocument = document == $scope.openedDocument ? null : document;
            };

            $scope.showInBrowser = function () {
                var host = ConfigurationService.configuration.host;
                var port = ConfigurationService.configuration.port;
                $window.open('http://' + host + ':' + port + '/product-management/#' + $scope.workspace);
            };            

            $scope.refreshCurrent = function(){
                angular.forEach($scope.documents, function (document) {
                    document.busy=true;
                    CliService.getStatusForDocument(document).then(function(){
                        document.busy = false;
                    });
                });
            };

            $scope.downloadAll = function(){
                var downloadPromises = [];
                angular.forEach($scope.documents,function(document){
                    downloadPromises.push($q(function(resolve,reject){
                        CliService.downloadDocumentFiles(document, $scope.folder.path, $scope.options).then(resolve,resolve);
                    }));
                });
                $q.all(downloadPromises).then(function(){
                   NotificationService.toast($filter('translate')('DOWNLOADS_FINISHED'));
                    FolderService.getFolder({path:$scope.folder.path}).newStuff = true;
                });
            };

            // if action = folders
            if($routeParams.action === 'folders'){

                CliService.getFolders($scope.workspace,$filter('decodePath')($scope.path)).then(function(folders){

                    angular.forEach(folders,function(folder){
                        var fullPath = '';
                        if($scope.path){
                            fullPath = $scope.path + ':';
                        }
                        fullPath += folder;
                        $scope.remoteFolders.push(fullPath);
                    });

                }).then(getDocumentsRevisions);

            } else if($routeParams.action === 'checkedOut'){
                getCheckedOutDocumentsRevisions();
            }
        })

        .filter('folderShortName',function () {
            return function (path) {
                return path ? path.substr(path.lastIndexOf(':')+1,path.length):'';
            };
        })

        .filter('decodePath',function () {
            return function (path) {
                return path ? path.replace(/:/g,'/') : '';
            };
        })

        .filter('encodePath',function () {
            return function (path) {
                return path ? path.replace(/\//g,':') : '';
            };
        })

        .controller('DocumentController', function ($scope, ConfigurationService) {
            $scope.configuration = ConfigurationService.configuration;
            $scope.actions = false;
        })

        .directive('folders', function () {
            return {
                templateUrl: 'js/workspace/workspace-folders.html',
                restrict:'E',
                controller: function ($scope,CliService) {

                }
            };
        })

        .directive('documentActions', function () {

            return {

                templateUrl: 'js/workspace/document-actions.html',

                controller: function ($scope, $filter,$element, $attrs, $transclude, $timeout, CliService, FolderService, PromptService) {

                    $scope.folders = FolderService.folders;
                    $scope.options = {force: true};
                    $scope.folder = {};
                    $scope.folder.path = FolderService.folders.length ? FolderService.folders[0].path : '';

                    var onFinish = function () {
                        $scope.document.busy = false;
                        $scope.document.progress = 0;
                    };

                    var onProgress = function (progress) {
                        $scope.document.progress = progress;
                    };

                    var newStuff = function () {
                        FolderService.getFolder({path:$scope.folder.path}).newStuff = true;
                    };

                    var checkForWorkspaceReload = function(){
                        if($scope.options.recursive){
                            $scope.refreshCurrent();
                        }
                    };

                    $scope.download = function () {
                        $scope.document.busy = true;
                        CliService.downloadDocumentFiles($scope.document, $scope.folder.path, $scope.options).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }, null, onProgress).then(onFinish).then(newStuff);
                    };

                    $scope.checkout = function () {
                        $scope.document.busy = true;
                        CliService.checkoutDocument($scope.document, $scope.folder.path, $scope.options).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }, null, onProgress).then(onFinish).then(newStuff).then(checkForWorkspaceReload);
                    };

                    $scope.checkin = function (e) {
                        PromptService.prompt(e, {title:$filter('translate')('CHECKIN_MESSAGE')}).then(function(message) {
                            $scope.document.busy = true;
                            CliService.checkinDocument($scope.document,{message:message}).then(function () {
                                return CliService.getStatusForDocument($scope.document);
                            }, null, onProgress).then(onFinish);
                        });
                    };

                    $scope.undoCheckout = function () {
                        $scope.document.busy = true;
                        CliService.undoCheckoutDocument($scope.document).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }).then(onFinish);
                    };

                }

            };
        });
})();
