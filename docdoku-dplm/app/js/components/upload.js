(function(){

    'use strict';

    angular.module('dplm.services.upload', [])

        .service('UploadService', function ($window, $q, $filter, ConfigurationService) {

            var fs = $window.require('fs');
            var FormData = $window.require('form-data');
            var http = $window.require('http');
            var getFileName = $filter('fileshortname');

            var upload = function(url, path){

                var deferred = $q.defer();

                var form = new FormData();
                form.append('upload', fs.createReadStream(path));

                var requestOpts =  ConfigurationService.getHttpFormRequestOpts();
                requestOpts.method = 'post';
                requestOpts.headers = form.getHeaders();
                requestOpts.path = '/api' + url;

                var request = http.request(requestOpts);
                form.pipe(request);

                var totalBytes = fs.statSync(path).size;
                var bytes = 0;

                request.on('data', function ( chunk ) {
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

            var getPartIterationURL = function(part){
                return '/files/'+part.workspaceId + '/parts/' + part.number +  '/' + part.version + '/' + part.partIterations.length;
            };

            var getDocumentIterationURL = function(doc){
                return '/files/'+doc.workspaceId + '/documents/' + doc.documentId +  '/' + doc.version + '/' + doc.documentIterations.length;
            };


            this.uploadNativeCADFile = function (folder, path, part){
                return upload(getPartIterationURL(part) + '/nativecad/', path);
            };

            this.uploadPartAtttachedFile = function (folder, path, part){
                return upload(getPartIterationURL(part) + '/attached-files/', path);
            };

            this.uploadFileToDocument = function(folder, path, document){
                return upload(getDocumentIterationURL(document), path);
            };

            this.downloadNativeCadFile = function (folder, part){
            };

            this.downloadPartAttachedFile = function(d){
            };

            this.downloadFileFromDocument = function(d){
            };

        });

})();
