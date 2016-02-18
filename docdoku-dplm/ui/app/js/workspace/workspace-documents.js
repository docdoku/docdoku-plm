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

            var onListResults = function (documents) {
                $scope.loadingDocuments = false;
                $scope.loadingMore = false;
                angular.forEach(documents, function (document) {
                    $scope.documents.push(document);
                });
            };

            var onError = function(error){
                $scope.error = error;
                $scope.loadingDocuments = false;
            };

            var getDocumentsRevisions = function () {
                $scope.error = null;
                return CliService.getDocumentsRevisionsInFolder($scope.workspace, $filter('decodePath')($scope.path))
                    .then(onListResults, onError);
            };

            var getCheckedOutDocumentsRevisions = function(){
                $scope.error = null;
                return CliService.getCheckedOutDocumentsRevisions($scope.workspace, $filter('decodePath')($scope.path))
                    .then(onListResults, onError);
            };

            $scope.showInBrowser = function () {
                var host = ConfigurationService.configuration.host;
                var port = ConfigurationService.configuration.port;
                $window.open('http://' + host + ':' + port + '/product-management/#' + $scope.workspace);
            };

            $scope.refreshCurrent = function(){
                var chain = $q.when();

                angular.forEach($scope.documents, function (document) {
                    chain = chain.then(function(){
                        document.busy=true;
                        return CliService.getStatusForDocument(document).then(function(){
                            document.busy = false;
                        });
                    });
                });
            };

            $scope.downloadAllData = {
                downloading:false,
                inProgress:0,
                total:0
            };

            $scope.downloadAll = function(){

                $scope.downloadAllData.inProgress = 0;
                $scope.downloadAllData.total = $scope.documents.length;
                $scope.downloadAllData.downloading = true;

                var chain = $q.when();
                angular.forEach($scope.documents,function(document){
                    chain = chain.then(function(){
                        $scope.downloadAllData.inProgress++;
                        return CliService.downloadDocumentFiles(document, $scope.folder.path, $scope.options);
                    });
                });

                chain.then(function(){
                    $scope.downloadAllData.downloading = false;
                    NotificationService.toast($filter('translate')('DOWNLOADS_FINISHED'));
                    FolderService.getFolder({path:$scope.folder.path}).newStuff = true;
                });
            };

            // if action = folders
            if($routeParams.action === 'folders'){
                $scope.error = null;
                CliService.getFolders($scope.workspace,$filter('decodePath')($scope.path)).then(function(folders){

                    angular.forEach(folders,function(folder){
                        var fullPath = '';
                        if($scope.path){
                            fullPath = $scope.path + ':';
                        }
                        fullPath += folder;
                        $scope.remoteFolders.push(fullPath);
                    });

                }).then(getDocumentsRevisions).catch(onError);

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
            $scope.openedDocument= false;
            $scope.toggleOpenedDocument = function () {
                $scope.openedDocument = ! $scope.openedDocument;
            };
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

                    $scope.output = [];

                    var onOutput = function(o){
                        $scope.output.push(o);
                    };

                    var onError = function(error){
                        $scope.error = error;
                    };

                    $scope.download = function () {
                        $scope.document.busy = true;
                        CliService.downloadDocumentFiles($scope.document, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }, onError, onProgress).then(onFinish).then(newStuff);
                    };

                    $scope.checkout = function () {
                        $scope.document.busy = true;
                        CliService.checkoutDocument($scope.document, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }, onError, onProgress).then(onFinish).then(newStuff).then(checkForWorkspaceReload);
                    };

                    $scope.checkin = function (e) {
                        PromptService.prompt(e, {title:$filter('translate')('CHECKIN_MESSAGE')}).then(function(message) {
                            $scope.document.busy = true;
                            CliService.checkinDocument($scope.document,{message:message}, onOutput).then(function () {
                                return CliService.getStatusForDocument($scope.document);
                            }, onError, onProgress).then(onFinish);
                        });
                    };

                    $scope.undoCheckout = function () {
                        $scope.document.busy = true;
                        CliService.undoCheckoutDocument($scope.document, onOutput).then(function () {
                            return CliService.getStatusForDocument($scope.document);
                        }, onError).then(onFinish);
                    };

                }

            };
        });
})();
