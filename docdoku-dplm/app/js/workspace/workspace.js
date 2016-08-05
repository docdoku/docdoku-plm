(function () {

    'use strict';

    angular.module('dplm.pages')

        .config(function ($routeProvider) {
            $routeProvider
                .when('/workspace/:workspaceId', {
                    controller: 'WorkspaceController',
                    templateUrl: 'js/workspace/workspace.html'
                });
        })

        .controller('WorkspaceController', function ($scope, $routeParams, $filter, $mdDialog,
                                                     WorkspaceService, DBService, ConfigurationService) {

            var workspaceId = $routeParams.workspaceId;
            var allParts = [];
            var allDocuments = [];
            var filteredItems = [];
            var translate = $filter('translate');
            var filter = $filter('filter');

            var hasFilter = function (code) {
                return $scope.filters.filter(function (filter) {
                        return filter.code === code && filter.value;
                    }).length > 0;
            };

            var getData = function () {
                return DBService.getDocuments(workspaceId).then(function (documents) {
                    allDocuments = documents;
                    return DBService.getParts(workspaceId);
                }).then(function (parts) {
                    allParts = parts;
                }).then(function () {
                    $scope.totalDocuments = allDocuments.length;
                    $scope.totalParts = allParts.length;
                    $scope.lastSyncDate = WorkspaceService.workspaceSyncs[workspaceId];
                });
            };

            var commonFilter = function (item) {
                if (!hasFilter('CHECKED_OUT') && item.checkOutUser && item.checkOutUser.login === ConfigurationService.configuration.login) {
                    return false;
                }

                if (!hasFilter('CHECKED_IN') && !item.checkOutUser && !item.releaseAuthor && !item.obsoleteAuthor) {
                    return false;
                }

                if (!hasFilter('LOCKED') && item.checkOutUser && item.checkOutUser.login !== ConfigurationService.configuration.login) {
                    return false;
                }

                if (!hasFilter('RELEASED') && item.releaseAuthor && !item.obsoleteAuthor) {
                    return false;
                }

                if (!hasFilter('OBSOLETE') && item.obsoleteAuthor) {
                    return false;
                }

                if ($scope.pattern && filter([item], $scope.pattern).length === 0) {
                    return false;
                }

                return true;
            };

            var documentMap = function (item) {
                item.lastIteration = item.documentIterations[item.documentIterations.length - 1];
                return item;
            };

            var documentFilter = function (item) {
                var lastIteration = item.documentIterations[item.documentIterations.length - 1];

                if (!hasFilter('SHOW_EMPTY_DATA') && !lastIteration.attachedFiles.length) {
                    return false;
                }

                return commonFilter(item);
            };

            var partFilter = function (item) {

                var lastIteration = item.partIterations[item.partIterations.length - 1];

                if (!hasFilter('SHOW_EMPTY_DATA') && !lastIteration.nativeCADFile) {
                    return false;
                }

                if (!hasFilter('LEAVES') && !lastIteration.components.length) {
                    return false;
                }

                if (!hasFilter('ASSEMBLIES') && lastIteration.components.length) {
                    return false;
                }

                return commonFilter(item);
            };

            var partMap = function (item) {
                item.lastIteration = item.partIterations[item.partIterations.length - 1];
                return item;
            };

            $scope.workspaceId = workspaceId;
            $scope.configuration = ConfigurationService.configuration;
            $scope.selected = [];
            $scope.totalDocuments = 0;
            $scope.totalParts = 0;

            var Types = $scope.viewTypes = {

                DOCUMENTS: {
                    code: 'DOCUMENT',
                    include: 'js/workspace/documents.html',
                    searchLabel: translate('SEARCH_IN_DOCUMENTS')
                },

                PARTS: {
                    code: 'PARTS',
                    include: 'js/workspace/parts.html',
                    searchLabel: translate('SEARCH_IN_PARTS')
                }
            };

            $scope.view = Types.DOCUMENTS;

            $scope.$watch('view', function (type) {
                $scope.view = type;
                $scope.search();
            });

            $scope.filters = [
                {name: translate('CHECKED_OUT'), code: 'CHECKED_OUT', value: true},
                {name: translate('CHECKED_IN'), code: 'CHECKED_IN', value: true},
                {name: translate('RELEASED'), code: 'RELEASED', value: true},
                {name: translate('OBSOLETE'), code: 'OBSOLETE', value: true},
                {name: translate('LOCKED'), code: 'LOCKED', value: true},
                {name: translate('SHOW_EMPTY_DATA'), code: 'SHOW_EMPTY_DATA', value: true},
                {name: translate('LEAVES'), code: 'LEAVES', value: true},
                {name: translate('ASSEMBLIES'), code: 'ASSEMBLIES', value: true}
            ];

            $scope.toggleFilters = function (state) {
                angular.forEach($scope.filters, function (filter) {
                    filter.value = state;
                });
                $scope.search();
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

            $scope.sync = {
                running: false
            };

            $scope.paginate = function (page, count) {
                var start = (page - 1) * count;
                var end = start + count;
                $scope.displayedItems = filteredItems.slice(start, end);
            };

            $scope.search = function () {
                var items = $scope.view === Types.DOCUMENTS ? allDocuments : allParts;
                var filter = $scope.view === Types.DOCUMENTS ? documentFilter : partFilter;
                var map = $scope.view === Types.DOCUMENTS ? documentMap : partMap;
                filteredItems = items.filter(filter).map(map);
                $scope.filteredItemsCount = filteredItems.length;
                $scope.paginate(1, 10);
            };


            $scope.refresh = function () {
                $scope.sync.running = true;
                WorkspaceService.refreshData(workspaceId).then(function () {
                    $scope.sync.running = false;
                    getData().then($scope.search);
                });
            };

            var refreshDisplay = function () {
                getData().then($scope.search);
            };


            $scope.actions = {
                download: function (selection) {
                    $mdDialog.show({
                        templateUrl: 'js/components/download/download.html',
                        clickOutsideToClose: false,
                        fullscreen: true,
                        locals: {
                            items: selection
                        },
                        controller: 'DownloadCtrl'
                    });
                },
                checkin: function (selection) {

                    WorkspaceService.checkInItems(selection)
                        .then(refreshDisplay, function () {
                            console.log('Something bad happened');
                        }, function () {
                            console.log('Some progress notifications');
                        });
                },

                checkout: function (selection) {
                    // TODO : filter for action availability
                    WorkspaceService.checkOutItems(selection)
                        .then(refreshDisplay, function () {
                            console.log('Something bad happened');
                        }, function () {
                            console.log('Some progress notifications');
                        });
                },

                undoCheckout: function (selection) {
                    WorkspaceService.undoCheckOutItems(selection)
                        .then(refreshDisplay, function () {
                            console.log('Something bad happened');
                        }, function () {
                            console.log('Some progress notifications');
                        });
                }
            };


            getData().then($scope.search);

        });

})();
