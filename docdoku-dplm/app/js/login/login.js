(function(){
    'use strict';
    angular.module('dplm.login',[])
        .controller('LoginCtrl',function($scope, $mdDialog, ConfigurationService, AuthService, WorkspaceService, xhrFrom){

            $scope.configuration = ConfigurationService.configuration;
            $scope.xhrFrom = xhrFrom;

            $scope.connect = function() {
                $scope.xhrFrom=null;
                if(ConfigurationService.hasAuth()){
                    $scope.loggingIn = true;
                    ConfigurationService.save();
                    AuthService.login()
                        .then(WorkspaceService.getWorkspaces)
                        .then($mdDialog.hide)
                        .catch(function(xhr){
                            $scope.xhrFrom = xhr
                        })
                        .finally(function(){
                            $scope.loggingIn = false;
                        });
                }
            };

        });
})();