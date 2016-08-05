(function () {

    'use strict';

    angular.module('dplm.services')
        .service('WorkspaceService', function ($window, $log, $filter, $q, $location, DocdokuAPIService, DBService, RepositoryService) {

            var _this = this;
            var fs = $window.require('fs');
            var fileMode = $filter('fileMode');
            var lastIteration = $filter('lastIteration');

            var checkInDocument = function (document, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.documents.checkInDocument({
                            workspaceId: document.workspaceId,
                            documentId: document.documentMasterId,
                            documentVersion: document.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };

            var checkOutDocument = function (document, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.documents.checkOutDocument({
                            workspaceId: document.workspaceId,
                            documentId: document.documentMasterId,
                            documentVersion: document.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };


            var undoCheckOutDocument = function (document, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.documents.checkOutDocument({
                            workspaceId: document.workspaceId,
                            documentId: document.documentMasterId,
                            documentVersion: document.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeDocuments([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };


            var checkInPart = function (part, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.part.checkIn({
                            workspaceId: part.workspaceId,
                            partNumber: part.number,
                            partVersion: part.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeParts([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };

            var checkOutPart = function (part, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.part.checkOut({
                            workspaceId: part.workspaceId,
                            partNumber: part.number,
                            partVersion: part.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeParts([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };

            var undoCheckOutPart = function (part, index, path) {
                return $q(function (resolve, reject) {
                    var updatedItem;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.part.undoCheckOut({
                            workspaceId: part.workspaceId,
                            partNumber: part.number,
                            partVersion: part.version
                        }).then(function (response) {
                            updatedItem = response.obj;
                            if (index) {
                                RepositoryService.updateItemInIndex(index, updatedItem, path);
                            }
                            return DBService.storeParts([updatedItem]);
                        }).then(function () {
                            fs.chmodSync(path, fileMode(updatedItem));
                            resolve(updatedItem);
                        });
                    }, reject);
                });
            };

            var saveDocumentNote = function(document,note){
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        var lastDocumentIteration = lastIteration(document);
                        api.apis.document.updateDocumentIteration({
                            workspaceId: document.workspaceId,
                            documentId: document.documentMasterId,
                            documentVersion: document.version,
                            docIteration: lastDocumentIteration.iteration,
                            body:{
                                revisionNote:note
                            }
                        }).then(function(response){
                            angular.copy(response.obj,lastDocumentIteration);
                            return DBService.storeDocuments([document]).then(resolve);
                        },reject);
                    });
                });
            };

            var savePartNote = function(part,note){
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.part.updatePartIteration({
                            workspaceId: part.workspaceId,
                            partNumber: part.number,
                            partVersion: part.version,
                            partIteration: lastIteration(part).iteration,
                            body:{
                                iterationNote:note
                            }
                        }).then(function(response){
                            return DBService.storeParts([response.obj]).then(resolve);
                        },reject);
                    });
                });
            };

            var saveNote = function(item,note){
                if(item.documentMasterId){
                    return saveDocumentNote(item,note);
                }else if(item.number){
                    return savePartNote(item,note);
                }
            };

            this.workspaces = [];

            this.reset = function () {
                _this.workspaces.length = 0;
            };

            this.getWorkspaces = function () {
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.workspaces.getWorkspacesForConnectedUser()
                            .then(function (response) {
                                angular.copy(response.obj.allWorkspaces.map(function (workspace) {
                                    return workspace.id;
                                }), _this.workspaces);
                                resolve(_this.workspaces);
                            }, reject);
                    }, reject);
                });
            };

            this.createPartInWorkspace = function (part) {
                return $q(function (resolve, reject) {
                    var createdPart;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.parts.createNewPart({
                            workspaceId: part.workspaceId,
                            body: part
                        }).then(function (response) {
                            createdPart = response.obj;
                            return DBService.storeParts([createdPart]);
                        }, reject).then(function () {
                            resolve(createdPart);
                        });
                    }, reject);
                });
            };

            this.createDocumentInWorkspace = function (document) {
                return $q(function (resolve, reject) {
                    var createdDocument;
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.folders.createDocumentMasterInFolder({
                            workspaceId: document.workspaceId,
                            folderId: document.workspaceId,
                            body: document
                        }).then(function (response) {
                            createdDocument = response.obj;
                            return DBService.storeDocuments([createdDocument]);
                        }, reject).then(function () {
                            resolve(createdDocument);
                        });
                    }, reject);
                });
            };

            this.refreshData = function (workspace) {

                var deferred = $q.defer();
                var totalDocuments = 0;
                var totalParts = 0;
                var done = 0, total = 4;

                deferred.notify({total: total, done: done, workspace: workspace});

                _this.fetchDocumentsCount(workspace).then(function (count) {
                    totalDocuments = count;
                    deferred.notify({total: total, done: ++done, workspace: workspace});
                    return workspace;
                }).then(_this.fetchPartsCount).then(function (count) {
                    totalParts = count;
                    deferred.notify({total: total, done: ++done, workspace: workspace});
                    return _this.fetchParts(workspace, 0, totalParts);
                }).then(function () {
                    deferred.notify({total: total, done: ++done, workspace: workspace});
                    return _this.fetchDocuments(workspace, 0, totalDocuments);
                }).finally(function () {
                    deferred.notify({total: total, done: ++done, workspace: workspace});
                    onWorkspaceSynced(workspace);
                    deferred.resolve();
                });

                return deferred.promise;
            };

            this.fetchParts = function (workspace, start, max) {
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.parts.getPartRevisions({
                            workspaceId: workspace,
                            start: start,
                            length: max
                        }).then(function (response) {
                            return DBService.storeParts(response.obj);
                        }).then(resolve);
                    }, reject);
                });
            };

            this.fetchPartsCount = function (workspace) {
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.parts.getTotalNumberOfParts({
                            workspaceId: workspace
                        }).then(function (response) {
                            resolve(response.obj.count);
                        });
                    }, reject);
                });
            };

            this.fetchDocuments = function (workspace, start, max) {
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.documents.getDocumentsInWorkspace({
                            workspaceId: workspace,
                            start: start,
                            max: max
                        }).then(function (response) {
                            return DBService.storeDocuments(response.obj);
                        }).then(resolve);
                    }, reject);
                });
            };

            this.fetchDocumentsCount = function (workspace) {
                return $q(function (resolve, reject) {
                    DocdokuAPIService.getClient().getApi().then(function (api) {
                        api.apis.documents.getDocumentsInWorkspaceCount({
                            workspaceId: workspace
                        }).then(function (response) {
                            resolve(response.obj.count);
                        });
                    }, reject);
                });
            };

            this.fetchAllWorkspaces = function (workspaceIds) {

                var deferred = $q.defer();
                var chain = $q.when();

                angular.forEach(workspaceIds, function (workspaceId) {
                    chain = chain.then(function () {
                        return _this.refreshData(workspaceId);
                    });
                });

                chain.then(deferred.resolve, null, deferred.notify);

                return deferred.promise;
            };


            this.checkInItems = function (files, index) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.index.documentMasterId) {
                            return checkInDocument(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        } else if (file.index.number) {
                            return checkInPart(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.checkOutItems = function (files, index) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.index.documentMasterId) {
                            return checkOutDocument(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        } else if (file.index.number) {
                            return checkOutPart(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.undoCheckOutItems = function (files, index) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.index.documentMasterId) {
                            return undoCheckOutDocument(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        } else if (file.index.number) {
                            return undoCheckOutPart(file.item, index, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total:total,done:++done});
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            var getLatestDateInIteration = function (iteration) {
                var date = iteration.creationDate;
                if (date < iteration.modificationDate) {
                    date = iteration.modificationDate;
                }
                if (date < iteration.checkInDate) {
                    date = iteration.checkInDate;
                }
                return new Date(date).getTime();
            };

            var latestEventSort = function (a, b) {
                return getLatestDateInIteration(lastIteration(b)) - getLatestDateInIteration(lastIteration(a));
            };

            this.getLatestEventsInWorkspace = function (workspaceId, max) {
                var deferred = $q.defer();
                var items = [];
                DBService.getDocuments(workspaceId).then(function (documents) {
                    items = items.concat(documents);
                    return workspaceId;
                }).then(DBService.getParts).then(function (parts) {
                    items = items.concat(parts);
                }).then(function () {
                    deferred.resolve(items.sort(latestEventSort).slice(0, max));
                });
                return deferred.promise;
            };

            this.workspaceSyncs = angular.fromJson($window.localStorage.workspaceSyncs || '{}');

            var onWorkspaceSynced = function (workspace) {
                _this.workspaceSyncs[workspace] = new Date();
                $window.localStorage.workspaceSyncs = angular.toJson(_this.workspaceSyncs);
            };

            this.resetWorkspaceSyncs = function () {
                _this.workspaceSyncs = {};
                $window.localStorage.workspaceSyncs = '{}';
            };


            this.updateItemNotes = function(items,note){
                var deferred = $q.defer();

                var chain = $q.when();
                var total = items.length, done = 0;
                angular.forEach(items,function(item){
                   chain = chain.then(function(){
                       return saveNote(item,note).then(function(){
                           deferred.notify({total: total, done: ++done});
                       });
                   });
                });

                chain.then(function(){
                    deferred.resolve();
                });

                return deferred.promise;
            };


        });
})();
