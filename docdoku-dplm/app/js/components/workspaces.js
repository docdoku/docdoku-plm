(function(){

    'use strict';

    angular.module('dplm.services.workspaces', [])
        .service('WorkspaceService', function ($log, $filter, $q, $location, DocdokuAPIService, DBService) {

            var _this = this;
            this.workspaces = [];

            this.reset = function(){
                _this.workspaces.length = 0;
            };

            this.getWorkspaces = function () {
                console.log('fetching workspaces')
                return $q(function(resolve, reject){
                    DocdokuAPIService.getClient().getApi().then(function(api){
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
                    DocdokuAPIService.getClient().getApi().then(function(api){
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
                    DocdokuAPIService.getClient().getApi().then(function(api){
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

            this.refreshData = function(workspace){
                var totalDocuments = 0;
                var totalParts = 0;
                return _this.fetchDocumentsCount(workspace).then(function(count){
                    totalDocuments = count;
                    return workspace;
                }).then(_this.fetchPartsCount).then(function(count){
                    totalParts = count;
                }).then(function(){
                    // TODO split with 20 elements by request
                    return _this.fetchParts(workspace,0,totalParts);
                }).then(function(){
                    // TODO split with 20 elements by request
                    return _this.fetchDocuments(workspace,0,totalDocuments);
                });
            };

            this.fetchParts = function(workspace, start, max){
                return $q(function(resolve, reject){
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.parts.getPartRevisions({
                            workspaceId:workspace,
                            start:start,
                            length:max
                        }).then(function(response){
                            return DBService.storeParts(response.obj);
                        }).then(resolve);
                    },reject);
                });
            };

            this.fetchPartsCount = function(workspace){
                return $q(function(resolve, reject){
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.parts.getTotalNumberOfParts({
                            workspaceId:workspace
                        }).then(function(response){
                            resolve(response.obj.count);
                        });
                    },reject);
                });
            };

            this.fetchDocuments = function(workspace, start, max){
                return $q(function(resolve, reject){
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.documents.getDocumentsInWorkspace({
                            workspaceId:workspace,
                            start:start,
                            max:max
                        }).then(function(response){
                            return DBService.storeDocuments(response.obj);
                        }).then(resolve);
                    },reject);
                });
            };

            this.fetchDocumentsCount = function(workspace){
                return $q(function(resolve, reject){
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.documents.getDocumentsInWorkspaceCount({
                            workspaceId:workspace
                        }).then(function(response){
                            resolve(response.obj.count);
                        });
                    },reject);
                });
            };
        });
})();
