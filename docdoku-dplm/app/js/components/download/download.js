(function () {

    'use strict';

    angular.module('dplm.dialogs')

        .controller('DownloadCtrl', function ($scope, $mdDialog, $filter,
                                              items, FolderService, DownloadService) {

            $scope.fileMap = $filter('itemsFiles')(items);
            $scope.files = Object.keys($scope.fileMap);
            $scope.folders = FolderService.folders;

            $scope.options = {
                destinationFolder: null
            };

            $scope.close = $mdDialog.hide;

            $scope.status = null;

            $scope.submit = function () {
                $scope.status = {done: 0, url: '', item: [], progress: 0, mainProgress: 0};

                DownloadService.bulkDownload($scope.fileMap,
                    FolderService.getFolder({uuid: $scope.options.destinationFolder}).path)
                    .then(null, null, function (status) {
                        $scope.status = status;
                    }).finally(function () {
                        $scope.status.ended = true;
                    });
            };

        });

})();
