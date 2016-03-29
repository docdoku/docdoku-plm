(function(){

    'use strict';

    angular.module('dplm.workspace.parts', [])

        .controller('WorkspacePartsController', function ($scope, $q, $filter, $window, $timeout, $routeParams, CliService, ConfigurationService, NotificationService, WorkspaceService) {

            $scope.count = 0;
            $scope.start = 0;
            $scope.max = 20;
            $scope.parts = [];
            $scope.loadingParts = true;
            $scope.loadingMore = false;

            $scope.filters = {
                checkoutable: true,
                checkedOutByMe: true,
                isReleased: true,
                checkedOut: true,
                search:''
            };

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

            var onError = function(error){
                $scope.error = error;
                $scope.loadingParts = false;
            };

            var runSearch = function (search) {
                $scope.error = null;
                $scope.loadingParts = true;
                return CliService.searchPartMasters($scope.workspace, search)
                    .then(onSearchResults, onError);
            };

            var getPartMasters = function () {
                $scope.error = null;
                return CliService.getPartMasters($scope.workspace, $scope.start, $scope.max)
                    .then(onListResults, onError);
            };

            var showInBrowser = function () {
                $window.open(ConfigurationService.resolveUrl() + '/product-management/#' + $scope.workspace);
            };

            var searchTimeout;

            $scope.$watch('filters.search', function (newValue, oldValue) {
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

            $scope.$on('refresh',resetList);
            $scope.$on('showInBrowser',showInBrowser);

            $scope.onScrollEnd = function () {
                if (!$scope.loadingParts && !$scope.search && $scope.start < $scope.count) {
                    $scope.start += $scope.max;
                    $scope.loadingMore = true;
                    NotificationService.toastBottomRight($filter('translate')('LOADING_MORE'));
                    getPartMasters().then(NotificationService.hide);
                }
            };

            $scope.refreshCurrent = function(){

                var chain = $q.when();

                angular.forEach($scope.parts, function (part) {
                    chain = chain.then(function(){
                        part.busy=true;
                        return CliService.getStatusForPart(part).then(function(){
                            part.busy = false;
                        });
                    });
                });

            };

            CliService.getPartMastersCount($routeParams.workspace).then(function (data) {
                $scope.count = data.count;
            }).then(getPartMasters).catch(onError);

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

                    if (!filters.checkedOut && part.checkoutUser && part.checkoutUser !== ConfigurationService.configuration.user) {
                        return false;
                    }

                    if (!filters.checkedOutByMe && part.checkoutUser && part.checkoutUser === ConfigurationService.configuration.user) {
                        return false;
                    }

                    return true;

                });

            };
        })
        .controller('PartController', function ($scope, ConfigurationService) {
            $scope.configuration = ConfigurationService.configuration;
            $scope.actions = false;
            $scope.openedPart = false;
            $scope.toggleOpenedPart = function () {
                $scope.openedPart = !$scope.openedPart;
            };
        })

        .directive('partActions', function () {

            return {

                templateUrl: 'js/workspace/part-actions.html',
                scope:false,
                controller: function ($scope, $filter, $element, $attrs, $timeout, CliService, FolderService, PromptService) {

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

                    var onError = function(error){
                        $scope.error = error;
                    };

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

                    $scope.output = [];

                    var onOutput = function(o){
                        $scope.$apply(function(){
                            $scope.output.push(o);
                        });
                    };

                    $scope.download = function () {
                        $scope.part.busy = true;
                        CliService.downloadNativeCad($scope.part, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, onError, onProgress).then(onFinish).then(newStuff);
                    };

                    $scope.checkout = function () {
                        $scope.part.busy = true;
                        CliService.checkoutPart($scope.part, $scope.folder.path, $scope.options, onOutput).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, onError, onProgress).then(onFinish).then(newStuff).then(checkForWorkspaceReload);
                    };

                    $scope.checkin = function (e) {
                        PromptService.prompt(e, {title:$filter('translate')('CHECKIN_MESSAGE')}).then(function(message) {
                            $scope.part.busy = true;
                            CliService.checkinPart($scope.part, {message:message}, onOutput).then(function () {
                                return CliService.getStatusForPart($scope.part);
                            }, onError, onProgress).then(onFinish);
                        });
                    };

                    $scope.undoCheckout = function () {
                        $scope.part.busy = true;
                        CliService.undoCheckoutPart($scope.part, onOutput).then(function () {
                            return CliService.getStatusForPart($scope.part);
                        }, onError).then(onFinish);
                    };

                }

            };
        });
})();
