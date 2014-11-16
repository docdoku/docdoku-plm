'use strict';

process.on('uncaughtException', function(e) { console.log(e); });

angular.module('dplm',[

    // Dependencies
    'ngMaterial',
    'ngAnimate',
    'ngRoute',
    'pascalprecht.translate',
    'uuid4',
    'ngDragDrop',
    'ngAnimate',

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

    'dplm.directives.filechange',
    'dplm.directives.scrollend',
    
    'dplm.filters.fileshortname',
    'dplm.filters.last'

])

.config(function($routeProvider){
    $routeProvider.otherwise('/');
})

.controller('AppCtrl', function($scope, $location, $mdSidenav, NotificationService, ConfigurationService, CliService, WorkspaceService) {
 
    $scope.title = 'DocDoku DPLM';
   
    $scope.openMenu = function() {
        $mdSidenav('menu').open();
    };

    var handleError = function (error) {
        if(error === ConfigurationService.error){
            NotificationService.toast('Configuration missing');             
        }else if(error == CliService.requirementsError){
            NotificationService.toast('Requirements missing');
        }
        $location.path('settings');
    };

    ConfigurationService.checkAtStartup()
        .then(CliService.checkRequirements)
        .then(WorkspaceService.getWorkspaces)
        .catch(handleError);
})

.controller('MenuController',function($scope,ConfigurationService,WorkspaceService,FolderService){
    
    $scope.configuration = ConfigurationService.configuration;
    $scope.workspaces = WorkspaceService.workspaces;
    $scope.folders = FolderService.folders;

    $scope.addFolder = function($event,files){
        if(files && files.length === 1){
          FolderService.add(files[0].path);  
        }
    };     
  
})

.controller('FolderMenuController',function($scope){
    $scope.onDrop = function(){       
    };
})

.controller('WorkspaceMenuController',function($scope){
    $scope.onDrop = function(){       
    };
});
