(function(){

    'use strict';

    angular.module('dplm.services.upload', [])

        .service('UploadService', function ($window, $q, RepositoryService) {

            this.uploadNativeCADFile = function (folder, path, part){
                console.log('lets upload')
                console.log(arguments)
            };

            this.downloadNativeCadFile = function (folder, part){

            };

            this.uploadFileToDocument = function(){

            };

            this.downloadFileFromDocument = function(d){

            };

        });

})();
