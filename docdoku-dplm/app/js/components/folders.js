(function(){

    'use strict';

    angular.module('dplm.services.folders', [])
        .service('FolderService', function ($window, uuid4, $q, $filter, CliService) {

            var _this = this;

            var glob = $window.require("glob");
            var home = process.env.HOME;
            var pathsForGlobalResearch ='**/.dplm/index.xml';
            var ignoreList = ['.dplm'];

            this.folders = angular.fromJson(localStorage.folders || '[]');

            var alreadyHave = function (path) {
                return _this.folders.filter(function (folder) {
                        return folder.path == path;
                    }).length > 0;
            };

            var statFiles = function (fileNames) {
                var promises = [];
                var fs = require('fs');
                var isWindows = require('os').type() === 'Windows_NT';
                angular.forEach(fileNames, function (fileName) {
                    promises.push($q(function (resolve, reject) {
                        fs.stat(fileName, function (err, stats) {
                            if(isWindows){
                                fileName =  fileName.charAt(0).toUpperCase()+ fileName.slice(1);
                            }
                            var file = {path: fileName};

                            if (err) reject(file);
                            else resolve(angular.extend(stats, file));
                        });
                    }));
                });
                return $q.all(promises);
            };

            this.getFolder = function (params) {
                return $filter('filter')(_this.folders, params)[0];
            };

            this.add = function (path) {
                if (alreadyHave(path)) {
                    return;
                }
                _this.folders.push({
                    uuid: uuid4.generate(),
                    path: path,
                    favorite: false
                });
                _this.save();
            };

            this.delete = function(folder){
                if (alreadyHave(folder.path)) {
                    _this.folders.splice(_this.folders.indexOf(folder),1);
                    _this.save();
                }
            };

            this.save = function () {
                localStorage.folders = angular.toJson(_this.folders);
            };

            this.recursiveReadDir = function (path) {
                return $q(function (resolve, reject) {
                    var recursive = require('recursive-readdir');
                    recursive(path, ignoreList, function (err, files) {
                        if (err) {
                            reject(err);
                        }
                        else {
                            resolve(files);
                        }
                    });
                });
            };

            this.getFilesCount = function(path){
                return $q(function (resolve, reject) {
                    var recursive = require('recursive-readdir');
                    recursive(path, ignoreList, function (err, files) {
                        if (err) {
                            reject(err);
                        }
                        else {
                            resolve(files.length);
                        }
                    });
                });
            };

            this.fetchFileStatus = function (file) {
                return CliService.getStatusForFile(file).then(function () {
                    var userModif = parseInt(file.mtime.getTime() / 1000);
                    var lastModified = file.part ? file.part.lastModified : file.document ? file.document.lastModified:0;
                    var dplmModif = parseInt(lastModified / 1000);
                    file.modified = userModif > dplmModif;
                    file.sync = !file.modified;
                    file.notSync = false;
                }, function () {
                    file.sync = false;
                    file.modified = false;
                    file.notSync = true;
                });
            };

            this.reveal = function (path) {
                var os = require('os');
                var command = '';
                switch (os.type()) {
                    case 'Windows_NT' :
                        command = 'explorer';
                        break;
                    case 'Darwin' :
                        command = 'open';
                        break;
                    default :
                        command = 'nautilus';
                        break;
                }
                require('child_process').spawn(command, [path]);

            };

            this.isFolder = function(path){
                return $q(function(resolve,reject){
                    var fs = require('fs');
                    fs.stat(path, function(err,stats){
                        if(err){
                            reject(err);
                        }
                        else if(stats.isDirectory()){
                            resolve();
                        }else{
                            reject(null);
                        }
                    });
                });

            };

        });

})();
