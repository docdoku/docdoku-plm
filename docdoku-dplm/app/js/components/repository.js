(function(){

    'use strict';

    angular.module('dplm.services.repository', [])
        .constant('indexLocation','/.dplm/index.json')
        .constant('indexPatternSearch','**/.dplm/index.json')
        .service('RepositoryService', function ($q,$window,indexLocation,indexPatternSearch, DocdokuAPIService) {

            var _this = this;
            var fs = $window.require('fs');
            var crypto = $window.require('crypto');
            var glob = $window.require("glob");

            var getOrCreateIndex = function(indexFolder){
                try{
                    return $window.require(indexFolder+indexLocation);
                }catch(e){
                    var dir = indexFolder+'/.dplm/';
                    if (!fs.existsSync(dir)){
                        fs.mkdirSync(dir);
                    }
                    fs.writeFileSync(indexFolder+indexLocation,'{}');
                    return {};
                }
            };

            this.search = function(folder){
                return $q(function(resolve,reject) {
                    glob(indexPatternSearch, {
                        cwd: folder,
                        nodir: true
                    }, function (err, files) {
                        if (err) {
                            reject(err);
                        }
                        else {
                            resolve(files);
                        }
                    });
                });
            };

            this.getRepositoryIndex = function(repository){
                return $q(function(resolve) {
                    try{
                        resolve($window.require(repository+indexLocation));
                    }catch(e){
                        resolve({});
                    }
                });
            };

            var getIndexValue = function(index,path,key){
                return index[path+'.'+key] || null;
            };

            var setIndexValue = function(index,path,key,value){
                index[path+'.'+key] = value;
            };

            var getHashFromFile = function(path){
              return crypto.createHash('MD5').update(fs.readFileSync(path)).digest('base64');
            };

            this.getFileIndex = function(index,path){

                var digest = getIndexValue(index,path,'digest');

                return digest ? {
                    digest:digest,
                    workspace:getIndexValue(index,path,'workspace'),
                    id:getIndexValue(index,path,'id'),
                    partNumber:getIndexValue(index,path,'partNumber'),
                    revision:getIndexValue(index,path,'revision'),
                    iteration:getIndexValue(index,path,'iteration'),
                    lastModifiedDate:getIndexValue(index,path,'lastModifiedDate'),
                    hash:getHashFromFile(path)
                } : null;
            };

            var writeIndex = function(indexPath, index){
                fs.writeFileSync(indexPath,JSON.stringify(index));
            };

            var updateIndexForPart = function(index, path, part){
                setIndexValue(index, path, 'digest',getHashFromFile(path));
                setIndexValue(index, path, 'workspace',part.workspaceId);
                setIndexValue(index, path, 'partNumber',part.number);
                setIndexValue(index, path, 'revision', part.version);
                setIndexValue(index, path, 'iteration', part.partIterations.length);
                setIndexValue(index, path, 'lastModifiedDate',Date.now());
            };

            var updateIndexForDocument = function(index, path, document){
                setIndexValue(index, path, 'digest',getHashFromFile(path));
                setIndexValue(index, path, 'workspace',document.workspaceId);
                setIndexValue(index, path, 'id',document.documentMasterId);
                setIndexValue(index, path, 'revision', document.version);
                setIndexValue(index, path, 'iteration', document.documentIterations.length);
                setIndexValue(index, path, 'lastModifiedDate',Date.now());
            };

            this.savePartToIndex = function(indexFolder,path, part){
                var indexPath = indexFolder + indexLocation;
                var index = getOrCreateIndex(indexFolder);
                updateIndexForPart(index, path, part);
                writeIndex(indexPath,index);
                return index;
            };

            this.saveDocumentToIndex = function(indexFolder,path,document){
                var indexPath = indexFolder + indexLocation;
                var index = getOrCreateIndex(indexFolder);
                updateIndexForDocument(index, path, document);
                writeIndex(indexPath,index);
                return index;
            };

            var documentRequest = function(api,workspaceId, documentId,version){
                return function(){
                    return $q(function(resolve, reject){
                        api.apis.document.getDocumentRevision({
                            workspaceId:workspaceId,
                            documentId:documentId,
                            documentVersion:version
                        }).then(function(response){
                            resolve(response.obj);
                        },reject);
                    });
                };
            };

            var partRequest = function(api,workspaceId,number,version){
                return function(){
                    return $q(function(resolve, reject){
                        api.apis.part.getPartRevision({
                            workspaceId:workspaceId,
                            partNumber:number,
                            partVersion:version
                        }).then(function(response){
                            resolve(response.obj);
                        },reject);
                    });
                };
            };

            this.syncIndex = function(indexFolder){

                var indexPath = indexFolder + indexLocation;
                var index = getOrCreateIndex(indexFolder);
                var keys = Object.keys(index);

                var documents = keys.filter(function(key){
                    return key.endsWith('.id');
                });

                var parts = keys.filter(function(key){
                    return key.endsWith('.partNumber');
                });

                var chain = $q.when();

                return DocdokuAPIService.client.getApi().then(function(api){

                    documents.forEach(function(id){
                        var filePath = id.substr(0,id.length-3);
                        var version = getIndexValue(index,filePath,'revision');
                        var workspaceId = getIndexValue(index,filePath,'workspace');
                        chain = chain.then(documentRequest(api,workspaceId,index[id],version)).then(function(document){
                            updateIndexForDocument(index, filePath, document);
                            return document;
                        });
                    });

                    parts.forEach(function(number){
                        var filePath = number.substr(0,number.length-11);
                        var version = getIndexValue(index,filePath,'revision');
                        var workspaceId = getIndexValue(index,filePath,'workspace');
                        chain = chain.then(partRequest(api,workspaceId,index[number],version)).then(function(part){
                            updateIndexForPart(index, filePath, part);
                            return part;
                        });
                    });

                    return chain;

                }).then(function(){
                    writeIndex(indexPath,index);
                });

            };

        })

        .filter('repositoryBasePath',function(indexLocation){
            return function(arg){
                return arg.replace(indexLocation,'');
            };
        });

})();
