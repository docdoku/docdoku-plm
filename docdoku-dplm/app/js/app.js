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

        // login dialog
        'dplm.login',

        // Components
        'dplm.services.cli',
        'dplm.services.configuration',
        'dplm.services.translations',
        'dplm.services.notification',
        'dplm.services.folders',
        'dplm.services.workspaces',
        'dplm.services.confirm',
        'dplm.services.prompt',
        'dplm.services.output',
        'dplm.services.auth',
        'dplm.services.api',

        'dplm.services.3d',
        'dplm.directives.filechange',
        'dplm.directives.scrollend',
        'dplm.directives.filedrop',
        'dplm.filters.fileshortname',
        'dplm.filters.timeago',
        'dplm.filters.last',
        'dplm.filters.join',
        'dplm.filters.humanreadablesize',

        'dplm.contextmenu',
        'dplm.menu'

    ])

        .config(function ($routeProvider,$mdThemingProvider,$compileProvider) {

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
                                         AuthService, NotificationService, ConfigurationService, CliService, WorkspaceService, FolderService) {

            $scope.title = 'DocDoku DPLM';

            $scope.user = AuthService.user;
            $scope.configuration = ConfigurationService.configuration;
            $scope.workspaces = WorkspaceService.workspaces;
            $scope.folders = FolderService.folders;

            var showLoginPage = function(data){

                if(data){
                    console.error(arguments)
                }

                $mdDialog.show({
                    templateUrl: 'js/login/login.html',
                    clickOutsideToClose:false,
                    fullscreen: true,
                    controller:'LoginCtrl'
                });

            };

            if(ConfigurationService.hasAuth()){
                AuthService.login().then(null, showLoginPage);
            }else{
                showLoginPage();
            }

            $scope.logout = function(){
                AuthService.logout().then(showLoginPage);
            };

            $scope.openMenu = function () {
                $mdSidenav('menu').open();
            };

            $scope.addFolder = function ($event, files) {
                if (files && files.length === 1) {
                    FolderService.add(files[0].path);
                }
            };


            $scope.isSelectedWorkspace = function(workspace){
                var currentParts = $location.path().split('/');
                return currentParts[1] === 'workspace'  && workspace === currentParts[2];
            };

            $scope.isSelectedFolder = function(folderUuid) {
                var currentParts = $location.path().split('/');
                return currentParts[1] === 'folder'  && folderUuid === currentParts[2];
            };

        });

})();