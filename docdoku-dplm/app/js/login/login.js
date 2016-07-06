(function(){
    'use strict';
    angular.module('dplm.login',[])
        .controller('LoginCtrl',function($scope, $mdDialog, ConfigurationService, AuthService){

            $scope.configuration = ConfigurationService.configuration;

            var onError = function(error){
                $scope.error = error
            };

            $scope.connect = function() {
                if(ConfigurationService.hasAuth()){
                    ConfigurationService.save();
                    AuthService.login().then($mdDialog.hide, onError);
                }
            };

        });
})();