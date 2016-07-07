(function(){

    'use strict';

    angular.module('dplm.services.workspaces', [])
        .service('WorkspaceService', function ($log, $filter, $q, $location, DocdokuAPIService, NotificationService) {

            var _this = this;
            this.workspaces = [];

            this.getWorkspaces = function () {
                return $q(function(resolve, reject){
                    DocdokuAPIService.client.getApi().then(function(api){
                        api.workspaces.getWorkspacesForConnectedUser().then(function(response){
                            angular.copy(response.obj.allWorkspaces.map(function(workspace){return workspace.id;}),_this.workspaces);
                            resolve(_this.workspaces);
                        },reject)
                    },reject);
                });
            };

            this.reset = function(){
                _this.workspaces.length = 0;
            };

        });
})();
