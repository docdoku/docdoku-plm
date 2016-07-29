(function(){

    'use strict';

    angular.module('dplm.dialogs.download',[])

        .controller('DownloadCtrl',function($scope,$mdDialog,$filter,
                                            items,FolderService,DownloadService){

            $scope.fileMap = $filter('itemsFiles')(items);
            $scope.files = Object.keys($scope.fileMap);
            $scope.folders = FolderService.folders;

            var total = $scope.files.length;

            $scope.options={
                forceRewrite:true,
                destinationFolder:null
            };

            $scope.close = $mdDialog.hide;

            $scope.status = null;

            $scope.submit = function(){
                $scope.status = {done:0,url:'',item:[],progress:0, mainProgress:0};

                DownloadService.bulkDownload($scope.fileMap,
                    FolderService.getFolder({uuid:$scope.options.destinationFolder}).path,
                    $scope.options.forceRewrite)
                    .then(null,null,function(status){
                        $scope.status = status;
                        $scope.status.mainProgress = (status.done/total) * 100;
                    }).finally(function(){
                        $scope.status.ended = true;
                    });
            };

        })

        .filter('lastIteration',function(){
            return function(item){
                if(item.id){return item.documentIterations[item.documentIterations.length-1]}
                else if(item.number){return item.partIterations[item.partIterations.length-1]}
            };
        })

        .filter('itemsFiles',function($filter){
            var lastIteration = $filter('lastIteration');
            return function(items){
                var fileMap = {};
                items.forEach(function(item){
                    var lastItemIteration = lastIteration(item);
                    if(item.number && lastItemIteration.nativeCADFile){
                        fileMap[lastItemIteration.nativeCADFile]=item;
                    }
                    if(item.id){
                        lastItemIteration.attachedFiles.forEach(function(file){
                            fileMap[file]=item;
                        });
                    }
                });
                return fileMap;
            };
        });

})();
