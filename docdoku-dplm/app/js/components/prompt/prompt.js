(function(){

    'use strict';

    angular.module('dplm.services.prompt',[])

        .service('PromptService',function($q,$mdDialog){

            this.prompt = function($event,confirmOptions){
                var deferred = $q.defer();
                var confirmed = false;
                var value = null;
                $mdDialog.show({
                    targetEvent: $event,
                    templateUrl:'js/components/prompt/prompt.html' ,
                    controller: function($scope){
                        $scope.value='';
                        $scope.title=confirmOptions.title;
                        $scope.cancel = function(){
                            $mdDialog.hide();
                        };
                        $scope.confirm = function(){
                            confirmed = true;
                            value=$scope.value;
                            $mdDialog.hide();
                        };
                    },
                    onComplete: afterShowAnimation
                }).finally(function() {
                    if(confirmed){
                        deferred.resolve(value);
                    }else{
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
