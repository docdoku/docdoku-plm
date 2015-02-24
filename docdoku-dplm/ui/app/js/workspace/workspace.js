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
                    templateUrl: 'js/workspace/workspace.html',
                    resolve:{
                        conf:function(ConfigurationService,WorkspaceService){
                            return ConfigurationService.checkAtStartup().then(WorkspaceService.getWorkspaces);
                        }
                    }
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

            $scope.workspace = $routeParams.workspace;

            WorkspaceService.addLastVisited($scope.workspace);

        });

})();
