(function () {

    'use strict';
/*
     process.on('uncaughtException', function (e) {
        console.log(e);
     });*/

    angular.module('dplm', [

        // Dependencies
        'ngMaterial',
        'ngAnimate',
        'ngRoute',
        'pascalprecht.translate',
        'uuid4',
        'ngAnimate',
        'ngAria',
        'md.data.table',
        'pascalprecht.translate',

        // Templates
        'dplm.templates',

        // DPLM components
        'dplm.pages',
        'dplm.menu',
        'dplm.services',
        'dplm.dialogs',
        'dplm.filters',
        'dplm.directives'

    ])

        .config(function ($routeProvider, $mdThemingProvider, $compileProvider) {

            $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|chrome-extension):/);

            $mdThemingProvider.definePalette('dplm-palette', $mdThemingProvider.extendPalette('blue', {
                '500': '#1A658A'
            }));

            $mdThemingProvider.theme('default')
                .primaryPalette('dplm-palette')
                .accentPalette('blue');

            $routeProvider.otherwise('/');

        })

        .controller('AppCtrl', function ($scope, $location, $mdMedia, $mdDialog, $mdSidenav, $filter,
                                         AuthService, NotificationService, ConfigurationService, WorkspaceService, FolderService) {

            var configuration = ConfigurationService.configuration;

            var showLoginPage = function (xhrFrom) {
                $mdDialog.show({
                    templateUrl: 'js/components/login/login.html',
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        xhrFrom: xhrFrom
                    },
                    controller: 'LoginCtrl'
                });
            };

            $scope.title = 'DocDoku DPLM';

            $scope.user = AuthService.user;
            $scope.configuration = configuration;
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.folders = FolderService.folders;


            var watchUser = function () {
                $scope.$watch('user.login', function (login) {
                    if (!login) {
                        showLoginPage();
                    }
                });
            };

            if (ConfigurationService.hasAuth()) {
                AuthService.login(configuration.login, configuration.password)
                    .then(WorkspaceService.getWorkspaces, showLoginPage)
                    .then(watchUser);
            } else {
                watchUser();
            }


            $scope.logout = function () {
                AuthService.logout();
            };

            $scope.openMenu = function () {
                $mdSidenav('menu').open();
            };

        });

})();