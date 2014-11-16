'use strict';

angular.module('dplm.services.workspaces',[])
.service('WorkspaceService',function($log, CliService){

	var _this = this;

	this.workspaces = [];

	this.getWorkspaces = function(){
		_this.workspaces.length = 0;
		return CliService.getWorkspaces().then(function(workspaces){			
			angular.forEach(workspaces,function (workspace) {
				_this.workspaces.push(workspace);
			});
		});
	};

});