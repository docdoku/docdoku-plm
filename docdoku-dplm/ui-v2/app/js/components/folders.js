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

	function statFiles(fileNames){
		var promises = [];
		var fs = require('fs');
		angular.forEach(fileNames,function(fileName){			
			promises.push($q(function(resolve,reject){
				fs.stat(fileName,function(err,stats){
					var file = {path:fileName};
					if (err) reject(file);
					else resolve(angular.extend(stats,file));
				});
			}));			
		});
		return $q.all(promises);
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
		}).then(statFiles);
	};

	this.fetchFileStatus = function(file){		
		return	CliService.getStatusForFile(file);
	};

	this.reveal = function(path){		
		var os = require('os');
		var command = '';	   
		switch(os.type()){
			case 'Windows_NT' :
				command = 'explorer';
			break;	       
			case 'Darwin' : 
				command = 'explorer';
			break;
			default :
				command = 'nautilus';
			break;
		}
		require('child_process').spawn(command, [path]);	
           
	};

});