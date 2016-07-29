(function(){
    'use strict';

    angular.module('dplm.services.items', [])

        .directive('statusIcon', function (ConfigurationService) {
            return {
                scope:{
                    item:'=statusIcon'
                },
                templateUrl:'js/components/item/status-icon.html',
                link:function(scope){

                    scope.$watch('item',function(){

                        var item = scope.item;
                        var configuration = ConfigurationService.configuration;
                        var icon;

                        if(item.releaseAuthor && !item.obsoleteAuthor){
                            icon = 'check';
                        } else if(item.obsoleteAuthor){
                            icon = 'broken_image';
                        } else if(!item.obsoleteAuthor && !item.releaseAuthor && !item.checkOutUser){
                            icon = 'remove_red_eye';
                        } else if(item.checkOutUser && item.checkOutUser.login === configuration.login){
                            icon = 'mode_edit';
                        } else if(item.checkOutUser && item.checkOutUser.login !== configuration.login){
                            icon = 'lock_outline';
                        } else {
                            icon = 'help'; // should not happen
                        }

                        scope.icon = icon;
                    });

                }
            };
        });

})();
