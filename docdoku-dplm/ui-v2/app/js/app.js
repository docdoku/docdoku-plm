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
    'dplm.services.translations'

])

.config(function($routeProvider){
        $routeProvider.otherwise('/');
})

.controller('AppCtrl', function($scope, $mdSidenav) {

    $scope.toggleLeft = function() {
        $mdSidenav('left').toggle();
    };

})

.controller('LeftCtrl', function($scope, $mdSidenav) {
    $scope.close = function() {
        $mdSidenav('left').close();
    };
})