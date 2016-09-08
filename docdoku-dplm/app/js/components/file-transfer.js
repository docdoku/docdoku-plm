(function () {

    'use strict';

    angular.module('dplm.services')


        .service('UploadService', function ($window, $q, $filter, ConfigurationService, RepositoryService) {

            var fs = $window.require('fs');
            var FormData = $window.require('form-data');
            var http = $window.require('http');
            var _this = this;

            var upload = function (url, path) {

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
                return '/files/' + doc.workspaceId + '/documents/' + doc.documentMasterId + '/' + doc.version + '/' + doc.documentIterations.length;
            };


            this.uploadNativeCADFile = function (path, part) {
                return upload(getPartIterationURL(part) + '/nativecad/', path);
            };

            this.uploadFileToDocument = function (path, document) {
                return upload(getDocumentIterationURL(document), path);
            };

            this.bulkUpload = function (files, indexFolder) {

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;
                var total = files.length;

                files.forEach(function (file) {
                    var item = file.item;

                    chain = chain.then(function () {
                        if (item.documentMasterId) {
                            return _this.uploadFileToDocument(file.path, item);

                        }
                        else if (item.number) {
                            return _this.uploadNativeCADFile(file.path, item);
                        }
                    });

                    chain = chain.then(function () {
                        deferred.notify({done: ++done, total: total});
                        RepositoryService.saveItemToIndex(indexFolder, file.path, item);
                        RepositoryService.updateFileInIndex(indexFolder, file.path);
                    });

                });

                chain.then(deferred.resolve);

                return deferred.promise;

            };

        })

        .service('DownloadService', function ($window, $q, $filter, $timeout,
                                              ConfigurationService, RepositoryService, FileUtils, READ_WRITE) {

            var fs = $window.require('fs');
            var http = $window.require('http');
            var getFileName = $filter('fileShortName');
            var zlib = $window.require('zlib');

            var download = function (url, destinationFolder, item) {

                var deferred = $q.defer();

                var fileName = getFileName(url);
                var file = destinationFolder + '/' + fileName;

                if(!FileUtils.fileExists(file)){
                    fs.writeFileSync(file, '');
                }

                FileUtils.setWritable(file);

                var fileStream = fs.createWriteStream(file);

                var requestOpts = ConfigurationService.getHttpFormRequestOpts();
                requestOpts.path = $window.encodeURI('/api' + url);

                var bytes = 0, totalBytes = 0;

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
                    // wait for writeStream end
                    fileStream.on('finish', function() {
                        deferred.resolve(file);
                    });
                });

                request.on('error', function (err) {
                    FileUtils.setFileMode(file, item);
                    deferred.reject(err);
                });

                return deferred.promise;
            };

            this.bulkDownload = function (fileMap, indexFolder) {

                var deferred = $q.defer();
                var chain = $q.when();
                var done = 0;
                var fileUrls = Object.keys(fileMap);

                fileUrls.forEach(function (url) {

                    var item = fileMap[url];
                    chain = chain.then(function () {
                        return download('/files/' + url, indexFolder, item)
                            .then(function (filePath) {
                                RepositoryService.saveItemToIndex(indexFolder, filePath, item);
                                RepositoryService.updateFileInIndex(indexFolder, filePath);
                                FileUtils.setFileMode(filePath, item);
                            }, function () {
                                // Error while downloading ?
                            }, function (progress) {
                                deferred.notify({done: done, url: url, item: item, progress: progress});
                            });
                    });

                });

                chain.then(deferred.resolve);

                return deferred.promise;

            };

        });

})();
