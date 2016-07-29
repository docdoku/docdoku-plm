(function(){

    'use strict';

    angular.module('dplm.repository', [])
        .controller('RepositorySearchCtrl', function ($timeout,$scope,$mdBottomSheet,
                                                      RepositoryService, FolderService, INDEX_LOCATION) {


            $scope.search = function(files){
                var file = files[0];
                if(file){
                    var path = file.path;
                    $scope.folder = path;
                    RepositoryService.search(path).then(function(repositories){
                        $scope.repositories = repositories.map(function(repository){
                            return $scope.folder + '/' + repository.replace(INDEX_LOCATION,'');
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
                $mdBottomSheet.hide();
            };

            $scope.addRepository = function(repository){
                FolderService.add(repository);
                $scope.repositories.splice($scope.repositories.indexOf(repository),1);
            };

            $scope.close = function(){
                $mdBottomSheet.hide();
            };
        });

})();
