(function () {

    'use strict';

    angular.module('dplm.services.confirm', [])

        .service('ConfirmService', function ($q, $mdDialog) {

            this.confirm = function ($event, confirmOptions) {
                var deferred = $q.defer();
                var confirmed = false;
                $mdDialog.show({
                    targetEvent: $event,
                    templateUrl: 'js/components/confirm/confirm.html',
                    controller: function ($scope) {
                        $scope.title = confirmOptions.content;
                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                        $scope.confirm = function () {
                            confirmed = true;
                            $mdDialog.hide();
                        };
                    },
                    onComplete: afterShowAnimation
                }).finally(function () {
                    if (confirmed) {
                        deferred.resolve();
                    } else {
                        deferred.reject();
                    }
                });
                // When the 'enter' animation finishes...
                function afterShowAnimation(scope, element, options) {
                }

                return deferred.promise;
            };

        });

})();
