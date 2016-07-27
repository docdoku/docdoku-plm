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
        .directive('filePreview',function($mdDialog){
            return {
                scope:{
                    file:'=filePreview'
                },
                link: function postLink(scope, element, attrs) {
                    element.on('click',function(){
                        $mdDialog.show({
                            templateUrl: 'js/components/preview/file-preview.html',
                            clickOutsideToClose:false,
                            fullscreen: true,
                            locals : {
                                file : scope.file
                            },
                            controller:'FilePreviewCtrl'
                        });
                    });
                }
            };
        })
        .filter('fileExtension',function(){
            return function(path){
                var lastDot = path.lastIndexOf('.');
                if(lastDot !== -1){
                    return path.substring(lastDot+1,path.length);
                }
                return '';
            };
        });

})();