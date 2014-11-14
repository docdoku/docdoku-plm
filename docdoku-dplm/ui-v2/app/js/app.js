'use strict';

angular.module('dplm',[

    // Dependencies
    'ngMaterial',
    'ngAnimate',
    'ngRoute',
    'pascalprecht.translate',

    // Routes
    'dplm.home',
    'dplm.settings',

    // Components
    'dplm.services.cli',
    'dplm.services.configuration',
    'dplm.services.translations',
    'dplm.services.notification'

])

.config(function($routeProvider){
    $routeProvider.otherwise('/');
})

.controller('AppCtrl', function($scope, $mdSidenav, CliService) {

    CliService.checkRequirements().then(CliService.getWorkspaces,onError);

    function onError(){
        console.log('Error')
    }

    $scope.title="DocDoku DPLM";

    $scope.toggleLeft = function() {
        $mdSidenav('left').toggle();
    };

});