'use strict';

angular.module('dplm.services.workspaces', [])
    .service('WorkspaceService', function ($log, $filter, $q, $location, CliService, NotificationService) {

        var _this = this;

        this.workspaces = [];

        this.getWorkspaces = function () {

            var deferred = $q.defer();

            if(_this.workspaces.length){
                deferred.resolve();
                return deferred.promise;
            }

            NotificationService.toast($filter('translate')('FETCHING_WORKSPACES'));

            CliService.getWorkspaces().then(function (workspaces) {
                angular.copy(workspaces,_this.workspaces);
                NotificationService.hide();
                deferred.resolve();
            },function(){
                // Should be offline, or auth error
                $location.path('settings');
                deferred.reject();
            });

            return deferred.promise;
        };

        this.reset = function(){
            _this.workspaces.length = 0;
        };

    });