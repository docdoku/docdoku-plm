angular.module('dplm.services.notification',[])
.service('NotificationService',function($mdToast){

        this.toast = function(message){

            var getToastPosition = function() {

                var toastPosition = {
                    bottom: false,
                    top: true,
                    left: false,
                    right: true
                };

                return Object.keys(toastPosition)
                    .filter(function(pos) { return toastPosition[pos]; })
                    .join(' ');

            };

            $mdToast.show({
                controller: function($scope){$scope.message=message;},
                template: '<md-toast><span flex>{{message}}</span></md-toast>',
                hideDelay: 6000,
                position: getToastPosition()
            });
        };

    })