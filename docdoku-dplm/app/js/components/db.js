(function () {

    'use strict';

    angular.module('dplm.services')
        .service('DBService', function ($window, $q) {

            var indexedDB = $window.indexedDB;

            var storeEntity = function (store, entity) {
                var deferred = $q.defer();
                var request = store.put(entity);
                request.onsuccess = deferred.resolve;
                request.onerror = deferred.reject;
                return deferred.promise;
            };

            var openDb = function () {
                var deferred = $q.defer();
                var open = indexedDB.open('Workspaces');
                open.onupgradeneeded = function () {
                    var db = open.result;
                    var documentStore = db.createObjectStore('Documents', {keyPath: ['workspaceId', 'id']});
                    var partStore = db.createObjectStore('Parts', {keyPath: ['workspaceId', 'partKey']});
                    documentStore.createIndex('WorkspaceIndex', 'workspaceId');
                    partStore.createIndex('WorkspaceIndex', 'workspaceId');
                };
                open.onsuccess = function () {
                    deferred.resolve(open.result);
                };
                open.onerror = deferred.reject;
                return deferred.promise;
            };

            this.getDocuments = function (workspaceId) {
                return $q(function (resolve, reject) {
                    openDb().then(function (db) {
                        var tx = db.transaction('Documents', 'readwrite');
                        var store = tx.objectStore('Documents');
                        var index = store.index('WorkspaceIndex');
                        var request = index.getAll(workspaceId);
                        request.onsuccess = function () {
                            resolve(request.result);
                        };
                        request.onerror = reject;
                        tx.oncomplete = function () {
                            db.close();
                        };
                    }, reject);
                });
            };

            this.getParts = function (workspaceId) {
                return $q(function (resolve, reject) {
                    openDb().then(function (db) {
                        var tx = db.transaction('Parts', 'readwrite');
                        var store = tx.objectStore('Parts');
                        var index = store.index('WorkspaceIndex');
                        var request = index.getAll(workspaceId);
                        request.onsuccess = function () {
                            resolve(request.result);
                        };
                        request.onerror = reject;
                        tx.oncomplete = function () {
                            db.close();
                        };
                    }, reject);
                });
            };

            this.storeDocuments = function (documents) {
                return openDb().then(function (db) {
                    var tx = db.transaction('Documents', 'readwrite');
                    var store = tx.objectStore('Documents');
                    tx.oncomplete = function () {
                        db.close();
                    };
                    return $q.all(documents.map(function (document) {
                        return storeEntity(store, document);
                    }));
                });
            };

            this.storeParts = function (parts) {
                return openDb().then(function (db) {
                    var tx = db.transaction('Parts', 'readwrite');
                    var store = tx.objectStore('Parts');
                    tx.oncomplete = function () {
                        db.close();
                    };
                    return $q.all(parts.map(function (part) {
                        return storeEntity(store, part);
                    }));
                });
            };

            this.getItem = function (itemIndex) {
                return $q(function (resolve, reject) {
                    return openDb().then(function (db) {

                        var tx, store, request;

                        if (itemIndex.documentMasterId) {
                            tx = db.transaction('Documents', 'readwrite');
                            store = tx.objectStore('Documents');
                            //var index = store.index('WorkspaceIndex');
                            request = store.get([itemIndex.workspaceId, itemIndex.documentMasterId + '-' + itemIndex.revision]);
                        }
                        else if (itemIndex.number) {
                            tx = db.transaction('Parts', 'readwrite');
                            store = tx.objectStore('Parts');
                            request = store.get([itemIndex.workspaceId, itemIndex.number + '-' + itemIndex.revision]);
                        } else {
                            return reject();
                        }

                        tx.oncomplete = function () {
                            db.close();
                        };

                        request.onsuccess = function () {
                            resolve(request.result);
                        };

                        request.onerror = reject;

                    });
                });
            };

            this.removeDb = function () {
                indexedDB.deleteDatabase('Workspaces');
            };

        });
})();
