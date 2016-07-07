(function(){

    'use strict';

    angular.module('dplm.workspace', ['dplm.workspace.documents','dplm.workspace.parts'])

        .config(function ($routeProvider) {

            $routeProvider
                .when('/workspace/:workspace', {
                    template:'',
                    controller: function($routeParams,$location){
                        $location.path('workspace/'+$routeParams.workspace+'/documents/folders');
                    }
                })
                .when('/workspace/:workspace/:entity/:action/:path?', {
                    controller: 'WorkspaceController',
                    templateUrl: 'js/workspace/workspace.html'
                });
        })

        .controller('WorkspaceController', function ($scope,$routeParams,WorkspaceService) {

            $scope.tabs={
                selected:
                    $routeParams.entity==='documents' ? 0 :
                    $routeParams.entity==='parts' ? 1 :
                0,
                documents:0,
                parts:1
            };

            $scope.refresh = function() {
                $scope.$broadcast('refresh');
            };

            $scope.showInBrowser = function () {
                $scope.$broadcast('showInBrowser');
            };

            $scope.workspace = $routeParams.workspace;

            WorkspaceService.addLastVisited($scope.workspace);

        });

})();
