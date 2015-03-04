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
            $scope.openedFile = null;

            $scope.filters = {
                sync: true,
                modified: true,
                entity:$routeParams.entity
            };

            $scope.refresh = function () {
                $scope.loadingFiles = true;
                $scope.files.length = 0;

                FolderService.recursiveReadDir($scope.folder.path).then(function (files) {

                    var statusPromises = [];

                    angular.forEach(files,function(file){
                        statusPromises.push($q(function (resolve, reject) {
                            FolderService.fetchFileStatus(file).then(function () {
                                if(file.document && $scope.filters.entity == 'documents'){
                                    $scope.files.push(file);
                                }else if(file.part && $scope.filters.entity == 'parts'){
                                    $scope.files.push(file);
                                }else if(!file.part && !file.document && $scope.filters.entity == 'unknown'){
                                    $scope.files.push(file);
                                }
                                resolve();
                            }, function () {
                                resolve();
                            });
                        }));
                    });

                    $q.all(statusPromises).then(function(){
                        $scope.loadingFiles = false;
                    });

                }, function () {
                    $scope.loadingFiles = false;
                }).then(function(){
                    $scope.folder.newStuff = false;
                });

            };

            $scope.toggleOpenedFile = function (file) {
                $scope.openedFile = file == $scope.openedFile ? null : file;
            };

            $scope.reveal = function () {
                FolderService.reveal($scope.folder.path);
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
            $scope.fetchStatus = function () {
                $scope.loading = true;
                FolderService.fetchFileStatus($scope.file).then(function () {
                    $scope.loading = false;
                }, function () {
                    $scope.loading = false;
                });
            };
            $scope.isViewable = AvailableLoaders.indexOf($scope.file.path.split('.').pop()) !== -1;
        })

        .directive('filePartActions', function () {

            return {

                templateUrl: 'js/folder/file-part-actions.html',

                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, NotificationService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;
                    $scope.show3D = false;

                    var onFinish = function () {
                        $scope.file.busy = false;
                        $scope.file.progress = 0;
                    };

                    var onProgress = function (progress) {
                        $scope.file.progress = progress;
                    };

                    $scope.checkout = function () {
                        $scope.file.busy = true;
                        CliService.checkoutPart($scope.file.part, $scope.folder.path, $scope.options).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.checkin = function () {
                        $scope.file.busy = true;
                        CliService.checkinPart($scope.file.part,$scope.folder.path).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.put = function () {
                        $scope.file.busy = true;
                        CliService.putCADFile($scope.file.part.workspace, $scope.file.path).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.undoCheckout = function () {
                        $scope.file.busy = true;
                        CliService.undoCheckoutPart($scope.file.part).then(function () {
                            return $scope.fetchStatus();
                        }).then(onFinish);
                    };

                    $scope.conversionStatus = function () {
                        CliService.getConversionStatus($scope.file.part).then(function (conversion) {
                            var message = conversion.pending ? $filter('translate')('PENDING') :
                                conversion.succeed ? $filter('translate')('SUCCESS') :
                                    $filter('translate')('FAIL');

                                NotificationService.toast(message);
                        },function(){
                            NotificationService.toast($filter('translate')('NO_CONVERSION'));
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

                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, NotificationService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;

                    var onFinish = function () {
                        $scope.file.busy = false;
                        $scope.file.progress = 0;
                    };

                    var onProgress = function (progress) {
                        $scope.file.progress = progress;
                    };

                    $scope.checkout = function () {
                        $scope.file.busy = true;
                        CliService.checkoutDocument($scope.file.document, $scope.folder.path, $scope.options).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.checkin = function () {
                        $scope.file.busy = true;
                        CliService.checkinDocument($scope.file.document,$scope.folder.path).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.put = function () {
                        $scope.file.busy = true;
                        CliService.putDocumentFile($scope.file.document.workspace, $scope.file.path).then(function () {
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.undoCheckout = function () {
                        $scope.file.busy = true;
                        CliService.undoCheckoutDocument($scope.file.document).then(function () {
                            return $scope.fetchStatus();
                        }).then(onFinish);
                    };

                }

            };
        })
        .directive('fileUnknownActions', function () {

            return {

                templateUrl: 'js/folder/file-unknown-actions.html',

                controller: function ($scope, $element, $attrs, $transclude, $timeout, $filter, CliService, WorkspaceService, NotificationService) {

                    $scope.options = {force: true,recursive:true};
                    $scope.workspaces = WorkspaceService.workspaces;
                    $scope.newPart = {workspace: $scope.workspaces[0]};
                    $scope.newDocument = {workspace: $scope.workspaces[0]};

                    var onFinish = function () {
                        $scope.file.busy = false;
                        $scope.file.progress = 0;
                    };

                    var onProgress = function (progress) {
                        $scope.file.progress = progress;
                    };

                    $scope.createPart = function () {
                        $scope.file.busy = true;
                        CliService.createPart($scope.newPart, $scope.file.path).then(function () {
                            $scope.newPart = {workspace: $scope.workspaces[0]};
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.createDocument = function () {
                        $scope.file.busy = true;
                        CliService.createDocument($scope.newDocument, $scope.file.path).then(function () {
                            $scope.newDocument = {workspace: $scope.workspaces[0]};
                            return $scope.fetchStatus();
                        }, null, onProgress).then(onFinish);
                    };

                }

            };
        });

})();
