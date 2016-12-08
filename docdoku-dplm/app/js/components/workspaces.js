(function () {

    'use strict';

    angular.module('dplm.services')
        .service('WorkspaceService', function ($window, $log, $filter, $q, DocdokuAPIService, DBService, RepositoryService) {

            var _this = this;
            var fs = $window.require('fs');
            var fileMode = $filter('fileMode');
            var lastIteration = $filter('lastIteration');

            var api = DocdokuAPIService.getApi();
            var client = DocdokuAPIService.getClient();
            var documentsApi = new api.DocumentsApi(client);
            var foldersApi = new api.FoldersApi(client);
            var documentApi = new api.DocumentApi(client);
            var partsApi = new api.PartsApi(client);
            var partApi = new api.PartApi(client);
            var workspacesApi = new api.WorkspacesApi(client);

            var checkInDocument = function (document, indexFolder, path) {
                return $q(function (resolve, reject) {
                    documentApi.checkInDocument(document.workspaceId, document.documentMasterId, document.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeDocuments([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };

            var checkOutDocument = function (document, indexFolder, path) {
                return $q(function (resolve, reject) {
                    documentApi.checkOutDocument(document.workspaceId, document.documentMasterId, document.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeDocuments([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };


            var undoCheckOutDocument = function (document, indexFolder, path) {
                return $q(function (resolve, reject) {
                    documentApi.undoCheckOutDocument(document.workspaceId, document.documentMasterId, document.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeDocuments([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };


            var checkInPart = function (part, indexFolder, path) {
                return $q(function (resolve, reject) {
                    partApi.checkIn(part.workspaceId, part.number, part.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeParts([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };

            var checkOutPart = function (part, indexFolder, path) {
                return $q(function (resolve, reject) {
                    partApi.checkOut(part.workspaceId, part.number, part.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeParts([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };

            var undoCheckOutPart = function (part, indexFolder, path) {
                return $q(function (resolve, reject) {
                    partApi.undoCheckOut(part.workspaceId, part.number, part.version,
                        function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            if (indexFolder && path) {
                                RepositoryService.saveItemToIndex(indexFolder, path, updatedItem);
                            }
                            return DBService.storeParts([updatedItem]).then(function () {
                                resolve(updatedItem);
                            });
                        });
                });
            };

            var saveDocumentNote = function (document, note) {
                return $q(function (resolve, reject) {
                    var lastDocumentIteration = lastIteration(document);
                    documentApi.updateDocumentIteration(document.workspaceId, document.documentMasterId,
                        document.version, lastDocumentIteration.iteration, {
                            revisionNote: note
                        }, function (err, updatedItem, response) {
                            if (err) {
                                return reject(err);
                            }
                            angular.copy(updatedItem, lastDocumentIteration);
                            return DBService.storeDocuments([document]).then(resolve);
                        });
                });
            };

            var savePartNote = function (part, note) {

                return $q(function (resolve, reject) {
                    var lastPartIteration = lastIteration(part);
                    partApi.updatePartIteration(part.workspaceId, part.number, part.version, lastPartIteration.iteration, {
                        revisionNote: note
                    }, function (err, updatedItem, response) {
                        if (err) {
                            return reject(err);
                        }
                        angular.copy(updatedItem, lastPartIteration);
                        return DBService.storeParts([part]).then(resolve);
                    });
                });

            };

            var saveNote = function (item, note) {
                if (item.documentMasterId) {
                    return saveDocumentNote(item, note);
                } else if (item.number) {
                    return savePartNote(item, note);
                }
            };

            this.workspaces = [];

            this.reset = function () {
                _this.workspaces.length = 0;
            };

            this.getWorkspaces = function () {
                return $q(function (resolve, reject) {

                    workspacesApi.getWorkspacesForConnectedUser(function (err, workspaces, response) {

                        if (err) {
                            return reject(err);
                        }

                        angular.copy(workspaces.allWorkspaces.map(function (workspace) {
                            return workspace.id;
                        }), _this.workspaces);

                        resolve(_this.workspaces);

                    });
                });
            };

            this.createPartInWorkspace = function (part) {
                return $q(function (resolve, reject) {
                    partsApi.createNewPart(part.workspaceId, part, function (err, createdPart, response) {
                        if (err) {
                            return reject(err);
                        }
                        return DBService.storeParts([createdPart]).then(function () {
                            resolve(createdPart);
                        });
                    });
                });
            };

            this.createDocumentInWorkspace = function (document) {
                return $q(function (resolve, reject) {
                    foldersApi.createDocumentMasterInFolder(document.workspaceId, document, document.workspaceId, function (err, createdDocument, response) {
                        if (err) {
                            return reject(err);
                        }
                        return DBService.storeDocuments([createdDocument]).then(function () {
                            resolve(createdDocument);
                        });
                    });
                });
            };

            this.refreshData = function (workspaceId) {

                var deferred = $q.defer();
                var totalDocuments = 0;
                var totalParts = 0;
                var done = 0, total = 4;

                deferred.notify({total: total, done: done, workspaceId: workspaceId});

                _this.fetchDocumentsCount(workspaceId).then(function (count) {
                    totalDocuments = count;
                    deferred.notify({total: total, done: ++done, workspaceId: workspaceId});
                    return workspaceId;
                }).then(_this.fetchPartsCount).then(function (count) {
                    totalParts = count;
                    deferred.notify({total: total, done: ++done, workspaceId: workspaceId});
                    return _this.fetchParts(workspaceId, 0, totalParts);
                }).then(function () {
                    deferred.notify({total: total, done: ++done, workspaceId: workspaceId});
                    return _this.fetchDocuments(workspaceId, 0, totalDocuments);
                }).finally(function () {
                    deferred.notify({total: total, done: ++done, workspaceId: workspaceId});
                    onWorkspaceSynced(workspaceId);
                    deferred.resolve();
                });

                return deferred.promise;
            };

            this.fetchParts = function (workspace, start, max) {
                return $q(function (resolve, reject) {
                    partsApi.getPartRevisions(workspace, {start: start, length: max}, function (err, parts, response) {
                        if (err) {
                            return reject(err);
                        }
                        return DBService.storeParts(parts).then(resolve);
                    });
                });
            };

            this.fetchPartsCount = function (workspace) {
                return $q(function (resolve, reject) {
                    partsApi.getTotalNumberOfParts(workspace, function (err, data, response) {
                        if (err) {
                            return reject(err);
                        }
                        resolve(data.count);
                    });
                });
            };

            this.fetchDocuments = function (workspace, start, max) {
                return $q(function (resolve, reject) {
                   documentsApi.getDocumentsInWorkspace(workspace, {start: start, length: max}, function (err, documents, response) {
                        if (err) {
                            return reject(err);
                        }
                        return DBService.storeDocuments(documents).then(resolve);
                    });
                });
            };

            this.fetchDocumentsCount = function (workspace) {
                return $q(function (resolve, reject) {
                    documentsApi.getDocumentsInWorkspaceCount(workspace, function (err, data, response) {
                        if (err) {
                            return reject(err);
                        }
                        resolve(data.count);
                    });
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


            this.checkInItems = function (files, folderPath) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.item.documentMasterId) {
                            return checkInDocument(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
                            });
                        } else if (file.item.number) {
                            return checkInPart(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.checkOutItems = function (files, folderPath) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.item.documentMasterId) {
                            return checkOutDocument(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
                            });
                        } else if (file.item.number) {
                            return checkOutPart(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
                            });
                        }
                    });
                });

                chain.then(deferred.resolve);

                return deferred.promise;
            };

            this.undoCheckOutItems = function (files, folderPath) {

                var deferred = $q.defer();
                var chain = $q.when();
                var total = files.length, done = 0;

                angular.forEach(files, function (file) {
                    chain = chain.then(function () {
                        if (file.item.documentMasterId) {
                            return undoCheckOutDocument(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
                            });
                        } else if (file.item.number) {
                            return undoCheckOutPart(file.item, folderPath, file.path).then(function (item) {
                                file.item = item;
                                deferred.notify({total: total, done: ++done});
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
                return getLatestDateInIteration(b.lastIteration) - getLatestDateInIteration(a.lastIteration);
            };

            var hasLastIteration = function (obj) {
                obj.lastIteration = lastIteration(obj);
                return obj.lastIteration;
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
                    deferred.resolve(items.filter(hasLastIteration).sort(latestEventSort).slice(0, max));
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


            this.updateItemNotes = function (items, note) {
                var deferred = $q.defer();

                var chain = $q.when();
                var total = items.length, done = 0;
                angular.forEach(items, function (item) {
                    chain = chain.then(function () {
                        return saveNote(item, note).then(function () {
                            deferred.notify({total: total, done: ++done});
                        });
                    });
                });

                chain.then(function () {
                    deferred.resolve();
                });

                return deferred.promise;
            };


        });
})();
