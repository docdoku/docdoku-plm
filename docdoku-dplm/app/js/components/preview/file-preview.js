(function(){

    angular.module('dplm.dialogs.file-preview',[])


        .controller('FilePreviewCtrl',function($scope, $filter,$mdDialog,
                                               Available3DLoaders,file){

            var ext = $filter('fileExtension')(file);
            $scope.file = file;
            $scope.is3dAvailable  = Available3DLoaders.indexOf(ext) !== -1;
            $scope.close = $mdDialog.hide;
            $scope.noPreviewAvailable = !$scope.is3dAvailable;

        })

        .directive('filePreview',function($mdDialog, $window, $filter, FileUtils){

            var isAvailableForViewer = $filter('isAvailableForViewer');

            return {
                scope:{
                    file:'=filePreview'
                },
                link: function postLink(scope, element, attrs) {

                    element.on('click',function() {

                        if (isAvailableForViewer(scope.file)) {
                            $mdDialog.show({
                                templateUrl: 'js/components/preview/file-preview.html',
                                clickOutsideToClose: false,
                                fullscreen: true,
                                locals: {
                                    file: scope.file
                                },
                                controller: 'FilePreviewCtrl'
                            });

                        } else {
                            FileUtils.openInOS(scope.file);
                        }
                    });

                    scope.$on('$destroy',function(){
                        element.off('click');
                    });
                }

            };
        });

})();