(function(){

    'use strict';

    angular.module('dplm.services.workspaces', [])
        .service('WorkspaceService', function ($log, $filter, $q, $location, CliService, NotificationService) {

            var _this = this;

            this.workspaces = [];

            var lastVisitedWorkspaces = JSON.parse(localStorage.lastVisitedWorkspaces || '[]');

            this.output = {
                error: null
            };

            this.getWorkspaces = function () {

                _this.output.error = null;

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
                },function(error){
                    _this.output.error = error;
                    // Should be offline, or auth error
                    $location.path('settings');
                    deferred.reject();
                });

                return deferred.promise;
            };

            this.reset = function(){
                _this.workspaces.length = 0;
            };

            this.getLastVisitedWorkspaces = function(){
                return lastVisitedWorkspaces;
            };

            this.addLastVisited = function(workspace){

                var alreadyIndexed = lastVisitedWorkspaces.indexOf(workspace);
                if(alreadyIndexed !== -1){
                    lastVisitedWorkspaces.splice(alreadyIndexed,1);
                }

                lastVisitedWorkspaces.unshift(workspace);
                lastVisitedWorkspaces.splice(4,lastVisitedWorkspaces.length-1);
                localStorage.lastVisitedWorkspaces = JSON.stringify(lastVisitedWorkspaces);
            };

        });
})();
