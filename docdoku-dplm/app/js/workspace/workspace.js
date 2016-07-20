(function(){

    'use strict';

    angular.module('dplm.workspace', [])

        .config(function ($routeProvider) {
            $routeProvider
                .when('/workspace/:workspace', {
                    controller: 'WorkspaceController',
                    templateUrl: 'js/workspace/workspace.html'
                });
        })

        .controller('WorkspaceController', function ($scope, $routeParams, $filter,
                                                     WorkspaceService, ConfigurationService) {

            var workspace = $routeParams.workspace;
            var allParts = [];
            var allDocuments = [];
            var filteredItems = [];
            var translate = $filter('translate');

            $scope.workspace = workspace;
            $scope.configuration = ConfigurationService.configuration;
            $scope.selected = [];
            $scope.totalDocuments = 0;
            $scope.totalParts = 0;

            $scope.view = { type : 'documents', include:'js/workspace/documents.html'};

            $scope.$watch('view.type', function(type){
                $scope.selected.length = 0;
                $scope.view.include = 'js/workspace/'+type+'.html';
                $scope.search();
            });

            $scope.filters = [
                { name: translate('CHECKED_OUT') , code:'CHECKED_OUT', value:true},
                { name: translate('CHECKED_IN'), code:'CHECKED_IN', value:true},
                { name: translate('RELEASED'), code:'RELEASED', value:true},
                { name: translate('OBSOLETE'), code:'OBSOLETE', value:true},
                { name: translate('LOCKED'), code:'LOCKED', value:true},
                { name: translate('SHOW_EMPTY_DATA'), code:'SHOW_EMPTY_DATA', value:true}
            ];

            $scope.toggleFilters = function(state){
                angular.forEach($scope.filters,function(filter){
                    filter.value = state;
                });
                $scope.search();
            };

            $scope.query = {
                limit: 10,
                limits: [10,20,50,100],
                page: 1
            };

            var hasFilter = function(code){
                return $scope.filters.filter(function(filter){
                    return filter.code === code && filter.value;
                }).length>0;
            };

            var getData = function(){
                return WorkspaceService.getDocuments(workspace).then(function(documents){
                    allDocuments = documents;
                    return WorkspaceService.getParts(workspace)
                }).then(function(parts){
                    allParts = parts;
                }).then(function(){
                    $scope.totalDocuments = allDocuments.length;
                    $scope.totalParts = allParts.length;
                });
            };

            var commonFilter = function(item){
                if(!hasFilter('CHECKED_OUT') && item.checkOutUser && item.checkOutUser.login === ConfigurationService.configuration.login){
                    return false;
                }

                if(!hasFilter('CHECKED_IN') && !item.checkOutUser && !item.releaseAuthor && !item.obsoleteAuthor){
                    return false;
                }

                if(!hasFilter('LOCKED') && item.checkOutUser && item.checkOutUser.login !== ConfigurationService.configuration.login){
                    return false;
                }

                if(!hasFilter('RELEASED') && item.releaseAuthor && !item.obsoleteAuthor){
                    return false;
                }

                if(!hasFilter('OBSOLETE') && item.obsoleteAuthor){
                    return false;
                }

                return true;
            };

            var documentFilter = function(item){
                if($scope.pattern && !item.id.match($scope.pattern)){
                    return false;
                }

                var lastIteration = item.documentIterations[item.documentIterations.length-1];

                if(!hasFilter('SHOW_EMPTY_DATA') && !lastIteration.attachedFiles.length){
                    return false;
                }

                return commonFilter(item);
            };

            var documentMap = function(item){
                item.lastIteration = item.documentIterations[item.documentIterations.length-1];
                return item;
            };

            var partFilter = function(item){
                if($scope.pattern && !item.number.match($scope.pattern)){
                    return false;
                }
                var lastIteration = item.partIterations[item.partIterations.length-1];

                if(!hasFilter('SHOW_EMPTY_DATA') && !lastIteration.nativeCADFile){
                    return false;
                }

                return commonFilter(item);
            };

            var partMap = function(item){
                item.lastIteration = item.partIterations[item.partIterations.length-1];
                return item;
            };

            $scope.paginate = function(page, count){
                var start = (page - 1)*count;
                var end  = start + count;
                $scope.displayedItems = filteredItems.slice(start, end);
            };

            $scope.search = function(){
                var items = $scope.view.type === 'documents' ? allDocuments: allParts;
                var filter = $scope.view.type === 'documents' ? documentFilter: partFilter;
                var map = $scope.view.type === 'documents' ? documentMap: partMap;
                filteredItems = items.filter(filter).map(map);
                $scope.filteredItemsCount = filteredItems.length;
                $scope.paginate(1,10);
            };

            $scope.sync = {
                running:false
            };

            $scope.refresh = function(){
                $scope.sync.running = true;
                WorkspaceService.refreshData(workspace).then(function(){
                    $scope.sync.running = false;
                    getData().then($scope.search);
                });
            };

            getData().then($scope.search);

        });

})();
