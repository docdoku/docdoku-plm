(function () {

    'use strict';

    angular.module('dplm.dialogs')

        .controller('DownloadCtrl', function ($scope, $mdDialog, $filter, $location,
                                              items, FolderService, DownloadService) {

            $scope.fileMap = $filter('itemsFiles')(items);
            $scope.files = Object.keys($scope.fileMap);
            $scope.folders = FolderService.folders;

            $scope.options = {
                destinationFolder: null
            };

            $scope.close = function(openDestinationFolder){
                $mdDialog.hide();
                if(openDestinationFolder){
                    $location.path('folder/'+$scope.options.destinationFolder);
                }
            };

            $scope.status = null;

            $scope.submit = function () {

                $scope.loading = true;

                var destinationFolder = FolderService.getFolder({uuid: $scope.options.destinationFolder}).path;

                DownloadService.bulkDownload($scope.fileMap, destinationFolder)
                    .then(null, null, function (status) {
                        $scope.status = status;
                    }).finally(function () {
                        $scope.status.ended = true;
                    });
            };

        });

})();
