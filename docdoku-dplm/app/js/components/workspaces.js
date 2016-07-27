(function(){

    'use strict';

    angular.module('dplm.services.workspaces', [])
        .service('WorkspaceService', function ($window,$log, $filter, $q, $location, DocdokuAPIService, DBService, RepositoryService) {

            var _this = this;
            var fs = $window.require('fs');
            var fileMode = $filter('fileMode');

            var checkInDocument = function(document, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.documents.checkInDocument({
                            workspaceId:document.workspaceId,
                            documentId:document.documentMasterId,
                            documentVersion:document.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };

            var checkOutDocument = function(document, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.documents.checkOutDocument({
                            workspaceId:document.workspaceId,
                            documentId:document.documentMasterId,
                            documentVersion:document.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };


            var undoCheckOutDocument = function(document, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.documents.checkOutDocument({
                            workspaceId:document.workspaceId,
                            documentId:document.documentMasterId,
                            documentVersion:document.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };


            var checkInPart = function(part, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.part.checkIn({
                            workspaceId:part.workspaceId,
                            partNumber:part.number,
                            partVersion:part.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeParts([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };

            var checkOutPart = function(part, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.part.checkOut({
                            workspaceId:part.workspaceId,
                            partNumber:part.number,
                            partVersion:part.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeParts([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };

            var undoCheckOutPart = function(part, index, path){
                return $q(function(resolve, reject){
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.part.undoCheckOut({
                            workspaceId:part.workspaceId,
                            partNumber:part.number,
                            partVersion:part.version
                        }).then(function(response){
                            updatedItem = response.obj;
                            RepositoryService.updateItemInIndex(index, updatedItem, path);
                            return DBService.storeParts([updatedItem]);
                        }).then(function(){
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    },reject);
                });
            };


            this.workspaces = [];

            this.reset = function(){
                _this.workspaces.length = 0;
            };

            this.getWorkspaces = function () {
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
                    var createdPart;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.parts.createNewPart({
                            workspaceId: part.workspaceId,
                            body:part
                        }).then(function(response){
                            createdPart = response.obj;
                            return DBService.storeParts([createdPart]);
                        },reject).then(function(){
                            resolve(createdPart);
                        });
                    },reject);
                });
            };

            this.createDocumentInWorkspace = function(document){
                return $q(function(resolve, reject){
                    var createdDocument;
                    DocdokuAPIService.getClient().getApi().then(function(api){
                        api.apis.folders.createDocumentMasterInFolder({
                            workspaceId:document.workspaceId,
                            folderId:document.workspaceId,
                            body:document
                        }).then(function(response){
                            createdDocument = response.obj;
                            return DBService.storeDocuments([createdDocument]);
                        },reject).then(function(){
                            resolve(createdDocument);
                        });
                    },reject);
                });
            };

            this.refreshData = function(workspace){
                // TODO wrap with $q.defer and notify
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


            this.checkInItems = function(files, index){

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;

                angular.forEach(files,function(file){
                    chain = chain.then(function(){
                        if(file.index.id){
                           return checkInDocument(file.item,index, file.path).then(function(item){
                               file.item = item;
                                deferred.notify(++done);
                            });
                        }else if(file.index.number){
                            return checkInPart(file.item, index, file.path).then(function(item){
                                file.item = item;
                                deferred.notify(++done);
                            });
                        }
                    })
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.checkOutItems = function(files, index){

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;

                angular.forEach(files,function(file){
                    chain = chain.then(function(){
                        if(file.index.id){
                            return checkOutDocument(file.item,index, file.path).then(function(item){
                                file.item = item;
                                deferred.notify(++done);
                            });
                        }else if(file.index.number){
                            return checkOutPart(file.item, index, file.path).then(function(item){
                                file.item = item;
                                deferred.notify(++done);
                            });
                        }
                    })
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.undoCheckOutItems = function(files, index){

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;

                angular.forEach(files,function(file){
                    chain = chain.then(function(){
                        if(file.index.id){
                            return undoCheckOutDocument(file.item,index, file.path).then(function(item){
                                file.item = item;
                                deferred.notify(++done);
                            });
                        }else if(file.index.number){
                            return undoCheckOutPart(file.item, index, file.path).then(function(item){
                                file.item = item;
                                deferred.notify(++done);
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

        });
})();
