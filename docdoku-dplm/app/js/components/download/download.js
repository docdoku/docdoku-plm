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


        .filter('itemsFiles',function(){
            return function(items){
                var fileMap = {};
                items.forEach(function(item){
                    var lastIteration;
                    if(item.partKey){
                        lastIteration = item.partIterations[item.partIterations.length-1];
                        if(lastIteration.nativeCADFile){
                            fileMap[lastIteration.nativeCADFile]=item;
                        }
                    }
                    if(item.documentMasterId){
                        lastIteration = item.documentIterations[item.documentIterations.length-1];
                        lastIteration.attachedFiles.forEach(function(file){
                            fileMap[file]=item;
                        });
                    }
                });
                return fileMap;
            };
        });

})();
