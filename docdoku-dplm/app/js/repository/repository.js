(function(){

    'use strict';

    angular.module('dplm.repository', [])
        .constant('indexLocation','/.dplm/index.json')
        .controller('RepositorySearchCtrl', function ($timeout,$scope,$mdDialog,
                                                      RepositoryService, FolderService, indexLocation) {


            $scope.search = function(files){
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

        .filter('repositoryBasePath',function(indexLocation){
            return function(arg){
                return arg.replace('/.dplm/index.xml','')
                .replace(indexLocation,'');
            };
        });

})();
