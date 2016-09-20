(function () {
    'use strict';

    function humanFileSize(bytes, si) {
        var thresh = si ? 1000 : 1024;
        if (Math.abs(bytes) < thresh) {
            return bytes + ' B';
        }
        var units = si ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'] : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
        var u = -1;
        do {
            bytes /= thresh;
            ++u;
        } while (Math.abs(bytes) >= thresh && u < units.length - 1);
        return bytes.toFixed(1) + ' ' + units[u];
    }

    angular.module('dplm.filters')

        .filter('humanreadablesize', function () {
            return function (bytes) {
                return humanFileSize(bytes, true);
            };
        })

        .filter('fileShortName', function () {
            return function (path) {
                return typeof path === 'string' ? path.replace(/^.*[\\\/]/, '') : path;
            };
        })

        .filter('fileMode', function (ConfigurationService, READ_ONLY, READ_WRITE) {
            return function (item) {
                return item.checkOutUser && item.checkOutUser.login === ConfigurationService.configuration.login ? READ_WRITE : READ_ONLY;
            };
        })

        .filter('fileExtension', function () {
            return function (path) {
                var lastDot = path.lastIndexOf('.');
                if (lastDot !== -1) {
                    return path.substring(lastDot + 1, path.length);
                }
                return '';
            };
        })

        .filter('repositoryBasePath', function (INDEX_LOCATION) {
            return function (arg) {
                return arg.replace(INDEX_LOCATION, '');
            };
        })

        .filter('isAvailableForViewer', function ($filter, Available3DLoaders) {
            var ext = $filter('fileExtension');
            return function (file) {
                return Available3DLoaders.indexOf(ext(file)) !== -1;
            };
        })

        .filter('isFileHidden',function(FileUtils, $window){

            if($window.process.platform === 'darwin' || $window.process.platform === 'linux') {
                return function (file) {
                    return (/(^|\/)\.[^\/\.]/g).test(file);
                };

            } else if($window.process.platform === 'win32' || $window.process.platform === 'win64'){
                return function(/*file*/){
                    // TODO : not tested on windows
                    // var winattr = $window.require('winattr');
                    // var attrs = winattr.getSync(file);
                    // return attrs && hidden.hidden;
                    return false;
                };
            } else {
                return function(/*file*/){
                    return false;
                };
            }

        });

})();
