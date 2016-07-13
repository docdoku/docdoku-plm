(function(){

    'use strict';

    angular.module('dplm.services.workspaces', [])
        .service('WorkspaceService', function ($log, $filter, $q, $location, DocdokuAPIService, NotificationService) {

            var _this = this;
            this.workspaces = [];

            this.reset = function(){
                _this.workspaces.length = 0;
            };

            this.getWorkspaces = function () {
                return $q(function(resolve, reject){
                    DocdokuAPIService.client.getApi().then(function(api){
                        api.workspaces.getWorkspacesForConnectedUser()
                            .then(function(response){
                                angular.copy(response.obj.allWorkspaces.map(function(workspace){return workspace.id;}),_this.workspaces);
                                resolve(_this.workspaces);
                        },reject);
                    },reject);
                });
            };

            this.createPartInWorkspace = function(part){
                return $q(function(resolve, reject){
                    DocdokuAPIService.client.getApi().then(function(api){
                        api.parts.createNewPart({
                            workspaceId: part.workspaceId,
                            body:part
                        }).then(function(response){
                            resolve(response.obj);
                        },reject);
                    },reject);
                });
            };

            this.createDocumentInWorkspace = function(document){
                return $q(function(resolve, reject){
                    DocdokuAPIService.client.getApi().then(function(api){
                        api.apis.folders.createDocumentMasterInFolder({
                            workspaceId:document.workspaceId,
                            folderId:document.workspaceId,
                            body:document
                        }).then(function(response){
                            resolve(response.obj);
                        },reject);
                    },reject);
                });
            };

        });
})();
