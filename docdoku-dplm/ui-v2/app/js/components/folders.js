'use strict';

angular.module('dplm.services.folders',[])
.service('FolderService',function(uuid4,$q,$filter, CliService){

	var _this = this;

	this.folders = angular.fromJson(localStorage.folders || '[]');

	function alreadyHave(path){
		return _this.folders.filter(function(folder){
			return folder.path == path;
		}).length > 0;
	}

	this.getFolder = function(params){
		return $filter('filter')(_this.folders,params)[0];
	};

	this.add = function(path){
		if(alreadyHave(path)){
			return;
		}
		_this.folders.push({
			uuid: uuid4.generate(),
			path:path,
			favorite:false
		});
		_this.save();
	};

	this.save = function(){
		localStorage.folders = angular.toJson(_this.folders);
	};	

	this.recursiveReadDir = function(path){		
		return $q(function(resolve,reject){
			var ignoreList = ['.dplm'];
			var recursive = require('recursive-readdir');
			recursive(path, ignoreList, function (err, files) {
			  if(err){
			  	reject(err);
			  }
			  else{			  	
			  	resolve(files);
			  }
			});
		});
	};

	this.fetchFilesStatus = function(files){

		var promises = [];
		
		angular.forEach(files,function(file){
			promises.push($q(function(resolve,reject){
				CliService.getStatusForFile(file).then(resolve,reject);
			}));
		});

		return $q.all(promises);

	};

});