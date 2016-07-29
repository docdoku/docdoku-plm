(function () {

    'use strict';

    angular.module('dplm.services.file-transfer', [])


        .service('UploadService', function ($window, $q, $filter, ConfigurationService, RepositoryService) {

            var fs = $window.require('fs');
            var FormData = $window.require('form-data');
            var http = $window.require('http');
            var _this = this;

            var upload = function (url, path) {

                console.log('UPLOAD')
                console.log(url)
                console.log(path)
                var deferred = $q.defer();

                var form = new FormData();
                form.append('upload', fs.createReadStream(path));

                var requestOpts = ConfigurationService.getHttpFormRequestOpts();
                requestOpts.method = 'post';
                requestOpts.headers = form.getHeaders();
                requestOpts.path = '/api' + url;

                var request = http.request(requestOpts);
                form.pipe(request);

                var totalBytes = fs.statSync(path).size;
                var bytes = 0;

                request.on('data', function (chunk) {
                    bytes += chunk.length;
                    deferred.notify((bytes / totalBytes) * 100);
                });

                request.on('response', function (response) {
                    deferred.resolve(response);
                });

                request.on('error', function (err) {
                    deferred.reject(err);
                });

                return deferred.promise;
            };

            var getPartIterationURL = function (part) {
                return '/files/' + part.workspaceId + '/parts/' + part.number + '/' + part.version + '/' + part.partIterations.length;
            };

            var getDocumentIterationURL = function (doc) {
                return '/files/' + doc.workspaceId + '/documents/' + doc.documentId + '/' + doc.version + '/' + doc.documentIterations.length;
            };


            this.uploadNativeCADFile = function (path, part) {
                return upload(getPartIterationURL(part) + '/nativecad/', path);
            };

            /*
             // TODO : use it
             this.uploadPartAttachedFile = function (folder, path, part){
             return upload(getPartIterationURL(part) + '/attached-files/', path);
             };*/

            this.uploadFileToDocument = function (path, document) {
                return upload(getDocumentIterationURL(document), path);
            };


            this.bulkUpload = function (files, indexFolder) {

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;
                var total = files.length;

                var index = RepositoryService.getRepositoryIndex(indexFolder);
                var indexPath = RepositoryService.getIndexPath(indexFolder);

                files.forEach(function (file) {
                    var item = file.item;

                    chain = chain.then(function () {
                        if (item.id) {
                            return _this.uploadFileToDocument(file.path, item);

                        }
                        else if (item.number) {
                            return _this.uploadNativeCADFile(file.path, item);
                        }
                    });

                    chain = chain.then(function () {
                        RepositoryService.saveItemToIndex(indexFolder, file.path, item);
                    });

                });

                chain.then(null, null, function () {
                    deferred.notify({done: ++done,  total: total});
                }).then(function () {
                    RepositoryService.writeIndex(indexPath, index);
                }).then(deferred.resolve);

                return deferred.promise;

            };

        })

        .service('DownloadService', function ($window, $q, $filter, $timeout,
                                              ConfigurationService, RepositoryService, READ_WRITE) {

            var fs = $window.require('fs');
            var http = $window.require('http');
            var getFileName = $filter('fileshortname');
            var zlib = $window.require('zlib');
            var fileMode = $filter('fileMode');

            var download = function (url, destinationFolder, item, forceRewrite) {
                var deferred = $q.defer();

                var fileName = getFileName(url);
                var file = destinationFolder + '/' + fileName;

                var fileStream = fs.createWriteStream(file);

                var requestOpts = ConfigurationService.getHttpFormRequestOpts();
                requestOpts.path = $window.encodeURI('/api' + url);

                var bytes = 0, totalBytes = 0;

                try {
                    fs.statSync(file);
                } catch (e) {
                    fs.writeFileSync(file, '');
                }

                fs.chmodSync(file, READ_WRITE);

                var request = http.get(requestOpts, function (response) {
                    totalBytes = response.headers['content-length'];
                    var encoding = response.headers['content-encoding'];
                    if ('gzip' === encoding) {
                        response.pipe(zlib.createGunzip()).pipe(fileStream);
                    } else {
                        response.pipe(fileStream);
                    }
                });

                request.on('data', function (chunk) {
                    bytes += chunk.length;
                    deferred.notify((bytes / totalBytes) * 100);
                });

                request.on('response', function () {
                    fs.chmodSync(file, fileMode(item));
                    deferred.resolve(file);
                });

                request.on('error', function (err) {
                    fs.chmodSync(file, fileMode(item));
                    deferred.reject(err);
                });

                return deferred.promise;
            };

            this.downloadItem = function (url, item, destinationFolder, forceRewrite) {
                return download('/files/' + file, destinationFolder, forceRewrite)
                    .then(function (filePath) {
                        RepositoryService.saveItemToIndex(destinationFolder, filePath, item);
                    });
            };

            this.bulkDownload = function (fileMap, destinationFolder, forceRewrite) {

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;
                var fileUrls = Object.keys(fileMap);

                var index = RepositoryService.getRepositoryIndex(destinationFolder);
                var indexPath = RepositoryService.getIndexPath(destinationFolder);

                fileUrls.forEach(function (url) {

                    var item = fileMap[url];
                    chain = chain.then(function () {
                        return download('/files/' + url, destinationFolder, item, forceRewrite)
                            .then(function (filePath) {
                                RepositoryService.updateItemInIndex(index, item, filePath);
                            }, function () {
                                // Error while downloading ?
                            }, function (progress) {
                                deferred.notify({done: done, url: url, item: item, progress: progress});
                            })
                    });

                    chain = chain.then(function () {
                        deferred.notify({done: ++done, url: url, item: item, progress: 100});
                    });

                });


                chain.then(function () {
                    RepositoryService.writeIndex(indexPath, index);
                }).then(deferred.resolve);

                return deferred.promise;

            };

        });

})();
