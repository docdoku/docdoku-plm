angular.module('dplm.menu', [])
.directive('menuButton',function(){
	return {
		restrict:'E',
		templateUrl:'js/menu/menu-button.html',
		scope:false
	};
});