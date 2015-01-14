(function(){

    'use strict';

    angular.module('dplm.workspace', [])
        .config(function ($routeProvider) {
            $routeProvider.when('/workspace/:workspace', {
                controller: 'WorkspaceController',
                templateUrl: 'js/workspace/workspace.html',
                resolve:{
                    conf:function(ConfigurationService,WorkspaceService){
                        return ConfigurationService.checkAtStartup().then(WorkspaceService.getWorkspaces);
                    }
                }
            });
        })

        .controller('WorkspaceController', function ($scope, $filter, $window, $timeout, $routeParams, CliService, ConfigurationService, NotificationService, WorkspaceService) {

            $scope.workspace = $routeParams.workspace;
            $scope.count = 0;
            $scope.start = 0;
            $scope.max = 20;
            $scope.parts = [];
            $scope.loadingParts = true;
            $scope.loadingMore = false;
            $scope.openedPart = null;
            $scope.search = '';

            $scope.filters = {
                checkoutable: true,
                checkoutedByMe: true,
                released: false,
                checkouted: false
            };

            WorkspaceService.addLastVisited($scope.workspace);

            var resetList = function () {
                $scope.start = 0;
                $scope.parts.length = 0;
                $scope.loadingParts = true;
                $scope.loadingMore = false;
                getPartMasters();
            };

            var onSearchResults = function (parts) {
                $scope.loadingParts = false;
                $scope.parts = parts;
            };

            var onListResults = function (parts) {
                $scope.loadingParts = false;
                $scope.loadingMore = false;
                angular.forEach(parts, function (part) {
                    $scope.parts.push(part);
                });
            };

            var runSearch = function (search) {
                $scope.loadingParts = true;
                return CliService.searchPartMasters($scope.workspace, search)
                    .then(onSearchResults);
            };

            var getPartMasters = function () {
                return CliService.getPartMasters($scope.workspace, $scope.start, $scope.max)
                    .then(onListResults);
            };

            var searchTimeout;

            $scope.$watch('search', function (newValue, oldValue) {
                if (searchTimeout) {
                    $timeout.cancel(searchTimeout);
                }
                if (newValue) {
                    searchTimeout = $timeout(function () {
                        runSearch(newValue);
                    }, 750);
                } else if (oldValue) {
                    resetList();
                }
            });

            $scope.toggleOpenedPart = function (part) {
                $scope.openedPart = part == $scope.openedPart ? null : part;
            };

            $scope.showInBrowser = function () {
                var host = ConfigurationService.configuration.host;
                var port = ConfigurationService.configuration.port;
                $window.open('http://' + host + ':' + port + '/product-management/#' + $scope.workspace);
            };

            $scope.onScrollEnd = function () {
                if (!$scope.loadingParts && !$scope.search && $scope.start < $scope.count) {
                    $scope.start += $scope.max;
                    $scope.loadingMore = true;
                    NotificationService.toastBottomRight($filter('translate')('LOADING_MORE'));
                    getPartMasters().then(NotificationService.hide);
                }
            };

            $scope.refreshCurrent = function(){
                angular.forEach($scope.parts, function (part) {
                    part.busy=true;
                    CliService.getStatusForPart(part).then(function(){
                        part.busy = false;
                    });
                });
            };

            CliService.getPartMastersCount($routeParams.workspace).then(function (data) {
                $scope.count = data.count;
            }).then(getPartMasters);

        })
        .filter('filterParts', function (ConfigurationService) {
            return function (arr, filters) {

                if (!arr) {
                    return [];
                }

                return arr.filter(function (part) {


                    if (!filters.isReleased && part.isReleased) {
                        return false;
                    }

                    if (!filters.checkoutable && !part.checkoutUser && !part.isReleased) {
                        return false;
                    }

                    if (!filters.checkouted && part.checkoutUser && part.checkoutUser !== ConfigurationService.configuration.user) {
                        return false;
                    }

                    if (!filters.checkoutedByMe && part.checkoutUser && part.checkoutUser === ConfigurationService.configuration.user) {
                        return false;
                    }

                    return true;

                });

            };
        })
        .controller('PartController', function ($scope, ConfigurationService) {
            $scope.configuration = ConfigurationService.configuration;
            $scope.actions = false;
        })

        .directive('partActions', function () {

            return {

                templateUrl: 'js/workspace/part-actions.html',

                controller: function ($scope, $filter,$element, $attrs, $transclude, $timeout, CliService, FolderService) {

                    $scope.folders = FolderService.folders;
                    $scope.options = {force: true, recursive: true};
                    $scope.baselines = [{name:$filter('translate')('LATEST'),id:''}];
                    $scope.baseline = $scope.baselines[0];

                    CliService.getBaselines($scope.part).then(function(baselines){
                        angular.forEach(baselines,function(baseline){
                            $scope.baselines.push(baseline);
                        });
                    });

                    $scope.folder = {};
                    $scope.folder.path = FolderService.folders.length ? FolderService.folders[0].path : '';

                    var onFinish = function () {
                        $scope.part.busy = false;
                        $scope.part.progress = 0;
                    };

                    var onProgress = function (progress) {
                        $scope.part.progress = progress;
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
                        $scope.part.busy = true;
                        CliService.download($scope.part, $scope.folder.path, $scope.options).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, null, onProgress).then(onFinish).then(newStuff);
                    };

                    $scope.checkout = function () {
                        $scope.part.busy = true;
                        CliService.checkout($scope.part, $scope.folder.path, $scope.options).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, null, onProgress).then(onFinish).then(newStuff).then(checkForWorkspaceReload);
                    };

                    $scope.checkin = function () {
                        $scope.part.busy = true;
                        CliService.checkin($scope.part).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, null, onProgress).then(onFinish);
                    };

                    $scope.undoCheckout = function () {
                        $scope.part.busy = true;
                        CliService.undoCheckout($scope.part).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }).then(onFinish);
                    };

                }

            };
        });
})();
