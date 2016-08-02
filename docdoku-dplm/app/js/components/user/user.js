(function () {

    'use strict';

    angular.module('dplm.dialogs')

        .controller('UserCtrl', function ($scope, $mdDialog, user) {

            $scope.user = user;

        })

        .directive('userPreview', function ($mdDialog) {
            return {
                scope: {
                    user: '=userPreview'
                },
                link: function postLink(scope, element, attrs) {

                    element.on('click', function () {
                        $mdDialog.show({
                            templateUrl: 'js/components/user/user.html',
                            clickOutsideToClose: false,
                            fullscreen: true,
                            locals: {
                                user: scope.user
                            },
                            controller: 'UserCtrl'
                        });
                    });

                    scope.$on('$destroy', function () {
                        element.off('click');
                    });
                }

            };
        });

})();
