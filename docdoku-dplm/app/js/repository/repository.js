(function(){

    'use strict';

    angular.module('dplm.services.repository', [])
        .controller('RepositorySearchCtrl', function ($timeout,$scope,$mdDialog,
                                                      RepositoryService, FolderService) {

            $scope.search = function($ev,files){
                var file = files[0];
                if(file){
                    var path = file.path;
                    $scope.folder = path;
                    RepositoryService.search(path).then(function(repositories){
                        $scope.repositories = repositories.map(function(repository){
                            return $scope.folder + '/' + repository.replace('/.dplm/index.xml','').replace('/.dplm/index.json','');
                        });
                    }, function(err){
                        $scope.repositories = err
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

        .service('RepositoryService', function ($q,$window) {

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
                            }))
                        }
                    });
                });
            };

            this.getRepositoryIndex = function(repository){
                return $q(function(resolve) {
                    try{
                        resolve($window.require(repository+'/.dplm/index.json'));
                    }catch(e){
                        resolve({});
                    }
                });
            };

            var getIndexValue = function(index,path,key){
                return index[path+'.'+key];
            };

            var setIndexValue = function(index,path,key,value){
                return index[path+'.'+key] = value;
            };

            this.getFileIndex = function(index,path){

                var digest = getIndexValue(index,path,'digest');

                if(digest){
                    var fileIndex ={
                        digest:digest,
                        workspace:getIndexValue(index,path,'workspace'),
                        id:getIndexValue(index,path,'id'),
                        partNumber:getIndexValue(index,path,'partNumber'),
                        revision:getIndexValue(index,path,'revision'),
                        iteration:getIndexValue(index,path,'iteration'),
                        lastModifiedDate:getIndexValue(index,path,'lastModifiedDate')
                    };
                    if(index[path+'.id']){
                        fileIndex.id = index[path+'.id'];
                    }
                    return fileIndex;
                }

                return null;
            };

        })

        .filter('repositoryBasePath',function(){
            return function(arg){
                return arg.replace('/.dplm/index.xml','')
                .replace('/.dplm/index.json','');
            }
        })
    ;
})();
