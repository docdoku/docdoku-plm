(function(){

    'use strict';

    angular.module('dplm.services.notification', [])
        .service('NotificationService', function ($mdToast) {

            this.toast = function (message, pos, length) {

                var getToastPosition = function () {

                    var toastPosition = pos || {
                            bottom: false,
                            top: true,
                            left: false,
                            right: true
                        };

                    return Object.keys(toastPosition)
                        .filter(function (pos) {
                            return toastPosition[pos];
                        })
                        .join(' ');

                };

                $mdToast.show({
                    controller: function ($scope) {
                        $scope.message = message;
                    },
                    template: '<md-toast><span flex>{{message}}</span></md-toast>',
                    hideDelay: length || 6000,
                    position: getToastPosition()
                });

            };

            this.hide = function(){
                $mdToast.hide();
            };

        });

})();
