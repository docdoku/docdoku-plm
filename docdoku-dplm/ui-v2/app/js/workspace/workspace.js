'use strict';

angular.module('dplm.workspace',[])
    .config(function($routeProvider){
        $routeProvider.when('/workspace/:workspace',{
            controller:'WorkspaceController',
            templateUrl:'js/workspace/workspace.html',
            resolve:{
            	WorkspaceInfos:function(CliService,$route){
            		return CliService.getPartMastersCount($route.current.params.workspace);
            	}
            }
        });
    })

.controller('WorkspaceController', function($scope,$window,$timeout,$routeParams,CliService, ConfigurationService,WorkspaceInfos){	
	
	$scope.workspace = $routeParams.workspace;
	$scope.count = WorkspaceInfos.count;
	$scope.start = 0;
	$scope.max = 10;
	$scope.parts = [];
	$scope.loadingParts = true;
	$scope.openedPart = null;
	$scope.search = '';
			
	$scope.filters = {
		released : true,
		checkoutable : true,
		checkouted : true,
		checkoutedByMe : true
	};

	var resetList = function() {
		$scope.start = 0;
		$scope.parts.length = 0;
		getPartMasters();
	};

	var onSearchResults = function(parts){
		$scope.loadingParts = false;
		$scope.parts = parts;
	};

	var onListResults = function(parts){
		$scope.loadingParts = false;
		angular.forEach(parts,function(part){
			$scope.parts.push(part);
		});
	};

	var runSearch = function(search){
		$scope.loadingParts = true;
		return CliService.searchPartMasters($scope.workspace, search)
			.then(onSearchResults);
	};

	var getPartMasters = function(){
		$scope.loadingParts = true;		
		return CliService.getPartMasters($scope.workspace, $scope.start, $scope.max)
			.then(onListResults);
	};

	var searchTimeout;

	$scope.$watch('search',function(newValue,oldValue){		
		if (searchTimeout){
			$timeout.cancel(searchTimeout);
		}
		if(newValue){			
	        searchTimeout = $timeout(function() {
	        	runSearch(newValue);
	        }, 750);			
		}else if(oldValue){
			resetList();
		}
	});

	$scope.toggleOpenedPart = function(part){
		$scope.openedPart = part == $scope.openedPart ? null : part;
	};

	$scope.showInBrowser = function(){
		var host = ConfigurationService.configuration.host;
		var port = ConfigurationService.configuration.port;
		$window.open('http://'+host+':'+port+'/product-management/#'+$scope.workspace);
	};

	$scope.onScrollEnd = function(){		
		if(!$scope.loadingParts && !$scope.search && $scope.start < $scope.count){
			$scope.start+=$scope.max;
			getPartMasters();
		}
	};

	getPartMasters();

})
.filter('filterParts',function(ConfigurationService){
	return function(arr,filters) {
	 
		if(!arr){
			return [];
		}

	    return arr.filter(function(part){

	  		if(!filters.isReleased && part.isReleased){
	  			return false;
	  		}

	  		if(!filters.checkoutable && !part.checkoutUser){
  				return false;
	  		}

	  		if(!filters.checkouted && part.checkoutUser && part.checkoutUser !== ConfigurationService.configuration.user){
  				return false;
	  		}

			if(!filters.checkoutedByMe && part.checkoutUser && part.checkoutUser === ConfigurationService.configuration.user){
  				return false;
	  		}

	  		return true;

	  });

	};
})
.controller('PartController',function($scope,ConfigurationService){
	$scope.configuration = ConfigurationService.configuration;	
	$scope.actions = false;	     
})

.directive('partActions', function(){

	return {

		templateUrl: 'js/workspace/part-actions.html',

		controller: function($scope, $element, $attrs, $transclude, $timeout, CliService,FolderService) {

			$scope.folders = FolderService.folders;
			$scope.options = {force:true,recursive:true};
			

			$scope.folder = {};
			$scope.folder.path = FolderService.folders[0].path;

			var onFinish = function(){
				$scope.part.busy = false;
			};

			var onProgress = function(progress){
				$scope.part.progress = progress;
			};

			$scope.download = function(){
				$scope.part.busy = true;
				CliService.download($scope.part,$scope.folder.path,$scope.options).then(function(){
					return CliService.getStatusForPart($scope.part);
				},null,onProgress).then(onFinish);
			};

			$scope.checkout = function(){
				$scope.part.busy = true;
				CliService.checkout($scope.part,$scope.folder.path,$scope.options).then(function(){
					return CliService.getStatusForPart($scope.part);					
				}).then(onFinish);
			};

			$scope.checkin = function(){
				$scope.part.busy = true;
				CliService.checkin($scope.part).then(function(){
					return CliService.getStatusForPart($scope.part);
				}).then(onFinish);
			};

			$scope.undoCheckout = function(){
				$scope.part.busy = true;
				CliService.undoCheckout($scope.part).then(function(){
					return CliService.getStatusForPart($scope.part);
				}).then(onFinish);
			};

		}

	};
});