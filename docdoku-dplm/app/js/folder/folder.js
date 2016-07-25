(function () {

    'use strict';

    angular.module('dplm.folder', [])

        .config(function ($routeProvider) {

            $routeProvider
                .when('/folder/:uuid', {
                    templateUrl: 'js/folder/folder.html',
                    controller: 'FolderController'
                });
        })

        .controller('FolderController', function ($scope, $location, $routeParams, $filter, $mdDialog,
                                                  FolderService, DBService, RepositoryService) {

            $scope.folder = FolderService.getFolder({uuid: $routeParams.uuid});

            var folderPath = $scope.folder.path;
            var allFiles = [];
            var filteredFiles = [];
            var repositoryIndex;
            var translate = $filter('translate');
            var getFileName = $filter('fileshortname');
            var filter = $filter('filter');

            var hasFilter = function (code) {
                return $scope.filters.filter(function (filter) {
                        return filter.code === code && filter.value;
                    }).length > 0;
            };

            $scope.query = {
                limit: 10,
                limits: [10, 20, 50, 100],
                page: 1
            };

            $scope.paginationLabels = {
                page: translate('PAGINATION_LABELS_PAGE'),
                rowsPerPage: translate('PAGINATION_LABELS_ROWS_PER_PAGE'),
                of: translate('PAGINATION_LABELS_OF')
            };

            $scope.selected = [];

            $scope.filters = [
                {name: translate('CHECKED_OUT'), code: 'CHECKED_OUT', value: true},
                {name: translate('CHECKED_IN'), code: 'CHECKED_IN', value: true},
                {name: translate('RELEASED'), code: 'RELEASED', value: true},
                {name: translate('OBSOLETE'), code: 'OBSOLETE', value: true},
                {name: translate('LOCKED'), code: 'LOCKED', value: true},
                {name: translate('DOCUMENTS'), code: 'DOCUMENTS', value: true},
                {name: translate('PARTS'), code: 'PARTS', value: true},
                {name: translate('OUT_OF_INDEX'), code: 'OUT_OF_INDEX', value: true}
            ];

            $scope.sync = {
                running: false,
                progress: 0,
                total: 0
            };

            $scope.reveal = function () {
                FolderService.reveal(folderPath);
            };

            $scope.toggleFavorite = function () {
                $scope.folder.favorite = !$scope.folder.favorite;
                FolderService.save();
            };

            $scope.paginate = function (page, count) {
                var start = (page - 1) * count;
                var end = start + count;
                $scope.displayedFiles = filteredFiles.slice(start, end);
            };


            $scope.search = function () {

                filteredFiles = allFiles.filter(function (path) {

                    var fileName = getFileName(path);

                    var index = RepositoryService.getFileIndex(repositoryIndex, path);

                    if (!hasFilter('OUT_OF_INDEX') && !index) {
                        return false;
                    }

                    if (!hasFilter('DOCUMENTS') && index && index.id) {
                        return false;
                    }

                    if (!hasFilter('PARTS') && index && index.number) {
                        return false;
                    }

                    if ($scope.pattern) {
                        if (filter([index || {}, fileName], $scope.pattern).length === 0) {
                            return false;
                        }
                    }

                    return true;
                })
                    .map(function (path) {
                        var file = FolderService.createFileObject(path);
                        file.index = RepositoryService.getFileIndex(repositoryIndex, path);
                        file.stat = FolderService.getFileSize(path);

                        if (file.index) {
                            DBService.getItem(file.index).then(function (item) {
                                file.item = item;
                            });
                        }

                        return file;
                    });

                $scope.filteredFilesCount = filteredFiles.length;
                $scope.paginate(1, $scope.query.limit);
            };

            $scope.fetchFolder = function () {
                $scope.sync.running = true;
                RepositoryService.getRepositoryIndex(folderPath)
                    .then(function (index) {
                        repositoryIndex = index;
                        return folderPath;
                    })
                    .then(FolderService.recursiveReadDir)
                    .then(function (files) {
                        $scope.totalFilesInFolder = files.length;
                        allFiles = files;
                        $scope.search();
                        $scope.sync.running = false;
                    });
            };

            $scope.createPart = function (file) {
                $mdDialog.show({
                    templateUrl: 'js/folder/create-part.html',
                    fullscreen: true,
                    controller: 'PartCreationCtrl',
                    locals: {
                        file: file,
                        folderPath: folderPath
                    }
                });
            };

            $scope.createDocument = function (file) {
                $mdDialog.show({
                    templateUrl: 'js/folder/create-document.html',
                    fullscreen: true,
                    controller: 'DocumentCreationCtrl',
                    locals: {
                        file: file,
                        folderPath: folderPath
                    }
                });
            };

            $scope.delete = function (ev) {
                $mdDialog.show({
                    targetEvent: ev,
                    templateUrl: 'js/folder/delete-folder.html',
                    controller: 'DeleteFolderCtrl',
                    locals: {
                        folder: $scope.folder
                    }
                });
            };

            $scope.updateIndex = function () {
                $scope.sync.running = true;
                RepositoryService.syncIndex(folderPath)
                    .then($scope.fetchFolder, function () {
                        // todo handle error
                    }, function (sync) {
                        $scope.sync.total = sync.total;
                        $scope.sync.progress = sync.progress;
                    }).catch(function () {
                        // todo handle error
                    }).finally(function () {
                        $scope.sync.total = 0;
                        $scope.sync.progress = 0;
                    });
            };

            $scope.toggleFilters = function (state) {
                angular.forEach($scope.filters, function (filter) {
                    filter.value = state;
                });
                $scope.search();
            };

            $scope.fetchFolder();


            $scope.$on("$destroy", function () {

            });

        })

        .controller('AddFolderCtrl', function ($scope, $mdDialog,
                                               FolderService) {

            $scope.search = function (files) {
                var file = files[0];
                if (file) {
                    FolderService.add(file.path);
                    $mdDialog.hide(file.path);
                }
            };

            $scope.close = $mdDialog.hide;

        })

        .controller('DocumentCreationCtrl', function ($scope, $mdDialog,
                                                      WorkspaceService, UploadService, RepositoryService,
                                                      file, folderPath) {

            $scope.file = file;
            $scope.workspaces = WorkspaceService.workspaces;

            var onError = function (error) {
                $scope.creating = false;
                $scope.error = error;
            };

            $scope.close = $mdDialog.hide;

            $scope.create = function () {
                $scope.creating = true;
                WorkspaceService.createDocumentInWorkspace($scope.document).then(function (document) {
                    $scope.document = document;
                    return UploadService.uploadFileToDocument(folderPath, file.path, document);
                }).then(function () {
                    var newIndex = RepositoryService.saveDocumentToIndex(folderPath, file.path, $scope.document);
                    file.index = RepositoryService.getFileIndex(newIndex, file.path);
                    $mdDialog.hide();
                }).catch(onError);
            };

        })

        .controller('PartCreationCtrl', function ($scope, $mdDialog,
                                                  WorkspaceService, UploadService, RepositoryService, file, folderPath) {

            $scope.file = file;
            $scope.workspaces = WorkspaceService.workspaces;

            var onError = function (error) {
                $scope.creating = false;
                if (typeof error === 'object' && error.statusText) {
                    $scope.error = error.statusText;
                } else if (typeof error === 'string') {
                    $scope.error = error;
                }
            };

            $scope.close = $mdDialog.hide;

            $scope.create = function () {
                $scope.creating = true;
                $scope.error = null;
                WorkspaceService.createPartInWorkspace($scope.part).then(function (part) {
                    $scope.part = part;
                    return UploadService.uploadNativeCADFile(folderPath, file.path, part);
                }).then(function () {
                    var newIndex = RepositoryService.savePartToIndex(folderPath, file.path, $scope.part);
                    file.index = RepositoryService.getFileIndex(newIndex, file.path);
                    $mdDialog.hide();
                })
                    .catch(onError);
            };
        })

        .controller('DeleteFolderCtrl', function ($scope, $filter, $mdDialog, $location,
                                                  FolderService, NotificationService,
                                                  folder) {

            $scope.close = $mdDialog.hide;

            $scope.confirm = function () {
                FolderService.delete(folder);
                NotificationService.toast($filter('translate')('DELETE_FOLDER_CONFIRMED'));
                $mdDialog.hide();
                $location.path('/');
            };

        });

})();
