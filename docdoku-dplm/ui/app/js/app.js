(function(){

    'use strict';

    process.on('uncaughtException', function (e) {
        console.log(e);
    });

    angular.module('dplm', [

        // Dependencies
        'ngMaterial',
        'ngAnimate',
        'ngRoute',
        'pascalprecht.translate',
        'uuid4',
        'ngDragDrop',
        'ngAnimate',
        'ngAria',

        // Templates
        'dplm.templates',

        // Routes
        'dplm.home',
        'dplm.settings',
        'dplm.workspace',

        'dplm.folder',
        // Components
        'dplm.services.cli',
        'dplm.services.configuration',
        'dplm.services.translations',
        'dplm.services.notification',
        'dplm.services.folders',
        'dplm.services.workspaces',
        'dplm.services.confirm',

        'dplm.services.3d',
        'dplm.directives.filechange',
        'dplm.directives.scrollend',
        'dplm.filters.fileshortname',
        'dplm.filters.timeago',
        'dplm.filters.last',

        'dplm.contextmenu'

    ])

        .config(function ($routeProvider) {
            $routeProvider.otherwise('/');
        })

        .controller('AppCtrl', function ($scope, $location, $mdSidenav, $filter, NotificationService, ConfigurationService, CliService, WorkspaceService, FolderService) {

            $scope.title = 'DocDoku DPLM';

            $scope.openMenu = function () {
                $mdSidenav('menu').open();
            };

            $scope.configuration = ConfigurationService.configuration;
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.folders = FolderService.folders;

            $scope.addFolder = function ($event, files) {
                if (files && files.length === 1) {
                    FolderService.add(files[0].path);
                }
            };

            $scope.isActive = function(route) {
                return route === $location.path();
            };

        })

        .controller('MenuController', function () {
        })

        .controller('FolderMenuController', function ($scope) {
            $scope.onDrop = function () {
            };
        })
        .controller('WorkspaceMenuController', function ($scope) {
            $scope.onDrop = function () {
            };
        });

})();