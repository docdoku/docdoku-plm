(function(){

    'use strict';

    angular.module('dplm.services.repository', [])
        .constant('indexLocation','/.dplm/index.json')
        .controller('RepositorySearchCtrl', function ($timeout,$scope,$mdDialog,
                                                      RepositoryService, FolderService, indexLocation) {


            $scope.search = function($ev,files){
                var file = files[0];
                if(file){
                    var path = file.path;
                    $scope.folder = path;
                    RepositoryService.search(path).then(function(repositories){
                        $scope.repositories = repositories.map(function(repository){
                            return $scope.folder + '/' + repository.replace('/.dplm/index.xml','').replace(indexLocation,'');
                        });
                    }, function(err){
                        $scope.repositories = err;
                    });
                }
            };

            $scope.retainedRepositories = [];

            $scope.addAllRepositories = function(){
                angular.forEach($scope.repositories,function(repository){
                    FolderService.add(repository);
                });
                $mdDialog.hide();
            };

            $scope.addRepository = function(repository){
                FolderService.add(repository);
                $scope.repositories.splice($scope.repositories.indexOf(repository),1);
            };

            $scope.close = function(){
                $mdDialog.hide();
            };
        })

        .service('RepositoryService', function ($q,$window,indexLocation) {

            var _this = this;
            var fs = $window.require('fs');
            var crypto = $window.require('crypto');

            this.search = function(folder){
                return $q(function(resolve,reject) {
                    var glob = $window.require("glob");
                    glob('**/.dplm/index.xml', {
                        cwd: folder,
                        nodir: true
                    }, function (err, files) {
                        if (err) {
                            reject(err);
                        }
                        else {
                            resolve(files.map(function(file){
                                return file;
                            }));
                        }
                    });
                });
            };

            this.getRepositoryIndex = function(repository){
                return $q(function(resolve) {
                    try{
                        resolve($window.require(repository+indexLocation));
                    }catch(e){
                        resolve({});
                    }
                });
            };

            var getIndexValue = function(index,path,key){
                return index[path+'.'+key] || null;
            };

            var setIndexValue = function(index,path,key,value){
                index[path+'.'+key] = value;
            };

            var getHashFromFile = function(path){
              return crypto.createHash('MD5').update(fs.readFileSync(path)).digest('base64');
            };

            this.getFileIndex = function(index,path){

                var digest = getIndexValue(index,path,'digest');

                return digest ? {
                    digest:digest,
                    workspace:getIndexValue(index,path,'workspace'),
                    id:getIndexValue(index,path,'id'),
                    partNumber:getIndexValue(index,path,'partNumber'),
                    revision:getIndexValue(index,path,'revision'),
                    iteration:getIndexValue(index,path,'iteration'),
                    lastModifiedDate:getIndexValue(index,path,'lastModifiedDate'),
                    hash:getHashFromFile(path)
                } : null;
            };

            var writeIndex = function(indexPath, index){
                fs.writeFileSync(indexPath,JSON.stringify(index));
            };

            this.savePartToIndex = function(indexFolder,path,part){
                var indexPath = indexFolder + indexLocation;
                var index = require(indexPath);
                setIndexValue(index, path, 'digest',getHashFromFile(path));
                setIndexValue(index, path, 'workspace',part.workspaceId);
                setIndexValue(index, path, 'partNumber',part.number);
                setIndexValue(index, path, 'revision', part.version);
                setIndexValue(index, path, 'iteration', part.partIterations.length);
                setIndexValue(index, path, 'lastModifiedDate',Date.now());
                writeIndex(indexPath,index);
                return index;
            };

            this.saveDocumentToIndex = function(indexFolder,path,document){
                var indexPath = indexFolder + indexLocation;
                var index = require(indexPath);
                setIndexValue(index, path, 'digest',getHashFromFile(path));
                setIndexValue(index, path, 'workspace',document.workspaceId);
                setIndexValue(index, path, 'id',document.id);
                setIndexValue(index, path, 'revision', document.version);
                setIndexValue(index, path, 'iteration', document.documentIterations.length);
                setIndexValue(index, path, 'lastModifiedDate',Date.now());
                writeIndex(indexPath,index);
            };

        })

        .filter('repositoryBasePath',function(indexLocation){
            return function(arg){
                return arg.replace('/.dplm/index.xml','')
                .replace(indexLocation,'');
            };
        });

})();
