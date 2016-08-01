(function(){

    'use strict';
/*

    process.on('uncaughtException', function (e) {
        console.log(e);
    });
*/

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

        // Templates
        'dplm.templates',

        // DPLM components
        'dplm.home',
        'dplm.settings',
        'dplm.workspace',
        'dplm.folder',
        'dplm.repository',
        'dplm.login',
        'dplm.menu',

        'dplm.services.configuration',
        'dplm.services.translations',
        'dplm.services.notification',
        'dplm.services.folders',
        'dplm.services.workspaces',
        'dplm.services.confirm',
        'dplm.services.prompt',
        'dplm.services.auth',
        'dplm.services.api',
        'dplm.services.repository',
        'dplm.services.file-transfer',
        'dplm.services.db',
        'dplm.services.3d',
        'dplm.services.items',

        'dplm.dialogs.download',
        'dplm.dialogs.file-preview',

        'dplm.filters.files',
        'dplm.filters.arrays',
        'dplm.filters.strings',
        'dplm.filters.dates'

    ])

        .config(function ($routeProvider,$mdThemingProvider,$compileProvider,$mdIconProvider) {

            $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|chrome-extension):/);

            $mdThemingProvider.definePalette('dplm-palette', $mdThemingProvider.extendPalette('blue', {
                '500': '#1A658A'
            }));

            $mdThemingProvider.theme('default')
                .primaryPalette('dplm-palette')
                .accentPalette('blue');

            $routeProvider.otherwise('/');
            $mdIconProvider
                .defaultIconSet('img/core-icons.svg', 24);

        })

        .controller('AppCtrl', function ($scope, $location, $mdMedia, $mdDialog, $mdSidenav, $filter,
                                         AuthService, NotificationService, ConfigurationService, WorkspaceService, FolderService) {


            var showLoginPage = function(xhrFrom){
                $mdDialog.show({
                    templateUrl: 'js/login/login.html',
                    clickOutsideToClose:false,
                    fullscreen: true,
                    locals : {
                        xhrFrom : xhrFrom
                    },
                    controller:'LoginCtrl'
                });
            };

            // Auto connect
            if(ConfigurationService.hasAuth()){
                AuthService.login().then(WorkspaceService.getWorkspaces, showLoginPage);
            }

            $scope.title = 'DocDoku DPLM';

            $scope.user = AuthService.user;
            $scope.configuration = ConfigurationService.configuration;
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.folders = FolderService.folders;

            $scope.$watch('user.login',function(login){
                if(!login){
                    showLoginPage();
                }
            });

            $scope.logout = function(){
                AuthService.logout();
            };

            $scope.openMenu = function () {
                $mdSidenav('menu').open();
            };

    });

})();