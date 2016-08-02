(function () {
    'use strict';

    angular.module('dplm.services.items', [])

        .directive('statusIcon', function (ConfigurationService) {
            return {
                scope: {
                    item: '=statusIcon'
                },
                templateUrl: 'js/components/item/status-icon.html',
                link: function (scope) {

                    scope.$watch('item', function () {

                        var item = scope.item;
                        var configuration = ConfigurationService.configuration;
                        var icon, status;

                        if (item.releaseAuthor && !item.obsoleteAuthor) {
                            icon = 'check';
                            status = 'RELEASED';
                        } else if (item.obsoleteAuthor) {
                            icon = 'broken_image';
                            status = 'OBSOLETE';
                        } else if (!item.obsoleteAuthor && !item.releaseAuthor && !item.checkOutUser) {
                            icon = 'remove_red_eye';
                            status = 'CHECKED_IN';
                        } else if (item.checkOutUser && item.checkOutUser.login === configuration.login) {
                            icon = 'mode_edit';
                            status = 'CHECKED_OUT';
                        } else if (item.checkOutUser && item.checkOutUser.login !== configuration.login) {
                            icon = 'lock_outline';
                            status = 'LOCKED';
                        } else {
                            icon = 'help'; // should not happen
                            status = '';
                        }

                        scope.icon = icon;
                        scope.status = status;
                    });

                }
            };
        });

})();
