/*global _,$,define,App,THREE,Worker*/
define(['dmu/LoaderManager', 'async', 'backbone', 'common-objects/log'],
    function (LoaderManager, async, Backbone, Logger) {

        'use strict';

        /**
         *  This class handles instances management.
         *
         *  Dialog to sceneManager  : add and remove objects
         *  Dialog from sceneManager  : init and update camera and frustum
         *
         *  Dialog with worker :
         *   - insert and remove instances from tree,
         *   - update frustum and camera
         *
         *  Dialog from worker
         *   - show/hide instances and update instances quality
         *
         * */

        var InstancesManager = function () {

            var _this = this;

            var timestamp = Date.now();

            Backbone.Events.on('part:saved', function () {
                timestamp = Date.now();
            }, this);

            this.xhrQueue = null;
            this.loadQueue = null;
            this.aborted = 0;
            this.alreadySameQuality = 0;
            this.xhrsDone = 0;

            var instancesIndexed = {};
            var loadedInstances = [];
            var loaderManager = new LoaderManager({progressBar: true});
            var loaderIndicator = $('#product_title').find('img.loader');
            var timer = null;
            var evalRunning = false;

            var worker = new Worker(App.config.contextPath + '/product-structure/js/workers/InstancesWorker.js');

            var workerMessages = {
                stats: function (stats) {
                    _this.workerStats = stats;
                },

                directives: function (directives) {
                    _this.aborted += _this.xhrQueue.tasks.length;
                    _this.xhrQueue.kill();

                    _(directives).each(function (directive) {
                        var instance = _this.getInstance(directive.id);
                        if (directive.nowait && directive.quality === undefined) {
                            App.sceneManager.removeObjectById(directive.id);
                            instance.qualityLoaded = undefined;
                            worker.postMessage({
                                fn: 'setQuality',
                                obj: {id: instance.id, quality: instance.qualityLoaded}
                            });
                        } else {
                            instance.directiveQuality = directive.quality;
                            _this.xhrQueue.push(directive);
                        }
                    });


                    setTimeout(function () {
                        evalRunning = false;
                    }, 500);
                }
            };

            worker.addEventListener('message', function (message) {
                if (typeof  workerMessages[message.data.fn] === 'function') {
                    workerMessages[message.data.fn](message.data.obj);
                } else {
                    Logger.log('%c Unrecognized command  : \n\t' + message.data, 'IM');
                }
            }, false);


            function loadProcess(directive, callback) {

                var instance = _this.getInstance(directive.id);

                if (!instance) {
                    setTimeout(callback, 0);
                    return;
                }

                if (directive.quality === undefined) {

                    // don't unload edited objects
                    if (App.sceneManager.editedObjects.indexOf(instance.id) !== -1) {
                        _this.aborted++;
                        worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                        setTimeout(callback, 0);
                        return;
                    }

                    App.sceneManager.removeObjectById(instance.id);
                    instance.qualityLoaded = undefined;
                    worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});
                    setTimeout(callback, 0);
                    return;
                }

                if (directive.quality === instance.qualityLoaded) {
                    _this.alreadySameQuality++;
                    setTimeout(callback, 0);
                    return;
                }

                // Load the instance
                var quality = App.config.contextPath + '/' + instance.files[directive.quality].fullName;

                var texturePath = quality.substring(0, quality.lastIndexOf('/'));
                loaderManager.parseFile(quality, texturePath, {
                    success: function (object3d) {

                        _this.xhrsDone++;

                        loadedInstances.push({
                            id: directive.id,
                            partIterationId: instance.partIterationId,
                            path: instance.path,
                            quality: directive.quality,
                            object3d: object3d
                        });

                        instance.qualityLoaded = directive.quality;
                        worker.postMessage({fn: 'setQuality', obj: {id: instance.id, quality: instance.qualityLoaded}});

                        callback();
                    }
                });
            }

            function adaptMatrix(matrix) {
                return new THREE.Matrix4(matrix[0], matrix[1], matrix[2], matrix[3],
                    matrix[4], matrix[5], matrix[6], matrix[7],
                    matrix[8], matrix[9], matrix[10], matrix[11],
                    matrix[12], matrix[13], matrix[14], matrix[15]);
            }

            function updateWorker() {
                if (evalRunning) {
                    return;
                }
                evalRunning = true;
                var sceneContext = App.sceneManager.getControlsContext();
                Logger.log('%c Updating worker', 'IM');
                worker.postMessage({
                    fn: 'context',
                    obj: {
                        camera: sceneContext.camPos,
                        target: sceneContext.target || {},
                        WorkerManagedValues: App.WorkerManagedValues,
                        debug: App.debug
                    }
                });

                if (App.router) {
                    App.router.updateRoute(sceneContext);
                }
            }

            function onSuccessLoadPath(instances) {
                _.each(instances, function (instance) {
                    if (instancesIndexed[instance.id]) {
                        worker.postMessage({fn: 'check', obj: instance.id});
                    } else {

                        instancesIndexed[instance.id] = instance;
                        instance.matrix = adaptMatrix(instance.matrix);

                        var min = new THREE.Vector3(instance.xMin, instance.yMin, instance.zMin);
                        var max = new THREE.Vector3(instance.xMax, instance.yMax, instance.zMax);
                        var box = new THREE.Box3(min, max).applyMatrix4(instance.matrix);

                        var cog = box.center();

                        // Allow parts that don't have box to be displayed
                        var radius = box.size().length() || 0.01;


                        worker.postMessage({
                            fn: 'addInstance',
                            obj: {
                                instanceRow: instance,
                                id: instance.id,
                                box: box,
                                cog: cog,
                                radius: radius,
                                qualities: instance.qualities,
                                checked: true
                            }
                        });
                    }
                });

                _this.planNewEval();
                loaderIndicator.hide();
            }

            function getTimestamp() {
                return timestamp || Date.now();
            }

            function loadPath(path, callback) {

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
                    App.config.productId + '/instances' +
                    '?configSpec=' + App.config.productConfigSpec + '&path=' + path + '&timestamp=' + getTimestamp();

                if (App.config.diverge) {
                    url += '&diverge=true';
                }

                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (instances) {
                        onSuccessLoadPath(instances);
                        if (callback) {
                            callback(instances);
                        }
                    }
                });
            }

            function loadPaths(paths, callback) {

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
                    App.config.productId + '/instances';

                if (App.config.diverge) {
                    url += '&diverge=true';
                }

                $.ajax({
                    url: url,
                    type: 'POST',
                    contentType: 'application/json',
                    dataType: 'json',
                    data: JSON.stringify({
                        configSpec: App.config.productConfigSpec,
                        paths: paths
                    }),
                    success: function (instances) {
                        onSuccessLoadPath(instances);
                        if (callback) {
                            callback(instances);
                        }
                    }
                });

            }

            function unLoadPath(path, callback) {

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
                    App.config.productId + '/instances' +
                    '?configSpec=' + App.config.productConfigSpec + '&path=' + path + '&timestamp=' + getTimestamp();

                if (App.config.diverge) {
                    url += '&diverge=true';
                }

                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (instances) {
                        _.each(instances, function (instance) {
                            worker.postMessage({fn: 'unCheck', obj: instance.id});
                        });
                        _this.planNewEval();
                        callback();
                    }
                });
            }

            this.loadQueue = async.queue(function (directive, callback) {
                if (directive.process === 'load') {
                    loadPaths(directive.paths, callback);
                } else if (directive.process === 'loadOne') {
                    loadPath(directive.path, callback);
                } else {
                    unLoadPath(directive.path, callback);
                }
            }, 1);

            this.loadQueue.drain = function () {
                Logger.log('Load Queue %c All paths have been processed', 'IM');
            };
            this.loadQueue.empty = function () {
                Logger.log('Load Queue %c  Empty Queue', 'IM');
            };
            this.loadQueue.saturated = function () {
                Logger.log('Load Queue %c Saturated Queue', 'IM');
            };


            this.xhrQueue = async.queue(loadProcess, 4);

            this.xhrQueue.drain = function () {
                Logger.log('XHR Queue %c All items have been processed', 'IM');
            };
            this.xhrQueue.empty = function () {
                Logger.log('XHR Queue %c Empty Queue', 'IM');
            };
            this.xhrQueue.saturated = function () {
                Logger.log('XHR Queue %c Saturated Queue', 'IM');
            };

            this.getLoadedGeometries = function (n) {
                return loadedInstances.splice(0, n || 1);
            };

            this.loadComponent = function (component) {
                loaderIndicator.show();
                var path = component.getEncodedPath();
                if (path) {
                    _this.loadQueue.push({'process': 'loadOne', 'path': [component.getEncodedPath()]});
                }
            };

            this.loadComponentsByPaths = function (paths) {
                loaderIndicator.show();
                var directive = {
                    process: 'load',
                    paths: []
                };
                _.each(paths, function (path) {
                    directive.paths.push(path);
                });
                _this.loadQueue.push(directive);
            };

            this.unLoadComponent = function (component) {
                var path = component.getEncodedPath();
                if (path) {
                    _this.loadQueue.push({'process': 'unload', 'path': component.getEncodedPath()});
                }
            };

            this.unLoadComponentsByPaths = function (pathsToUnload) {
                _(pathsToUnload).each(function (path) {
                    _this.loadQueue.push({'process': 'unload', 'path': path});
                });
            };

            this.clear = function () {
                Logger.log('%c Clearing Scene', 'IM');

                _this.xhrQueue.kill();
                _this.loadQueue.kill();

                _(_(instancesIndexed).pluck('id')).map(App.sceneManager.removeObjectById);

                worker.postMessage({fn: 'clear', obj: null});

                instancesIndexed = {};
                loadedInstances = [];
            };

            this.planNewEval = function () {
                clearTimeout(timer);
                if (!evalRunning) {
                    updateWorker();
                } else {
                    timer = setTimeout(updateWorker, 500);
                }
            };

            this.getInstance = function (instanceId) {
                return instancesIndexed[instanceId];
            };

            // Method called from product visualization iframe
            this.loadProduct = function(pathToLoad, callback){

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' +
                    App.config.productId + '/instances' +
                    '?configSpec=' + App.config.productConfigSpec + '&path=' + pathToLoad + '&timestamp=' + getTimestamp();

                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (instances) {
                        onSuccessLoadPath(instances);
                        callback();
                    }
                });

            };

            // Method called from assembly visualization iframe
            this.loadAssembly = function(partRevisionKey, callback){

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' +
                    partRevisionKey + '/instances';

                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (instances) {
                        onSuccessLoadPath(instances);
                        callback();
                    }

                });

            };

            this.computeGlobalBBox = function(){

                var box = new THREE.Box3(new THREE.Vector3(0,0,0),new THREE.Vector3(0,0,0));

                _.each(instancesIndexed, function (instance) {
                    var min = new THREE.Vector3(instance.xMin, instance.yMin, instance.zMin);
                    var max = new THREE.Vector3(instance.xMax, instance.yMax, instance.zMax);
                    var instanceBox = new THREE.Box3(min, max).applyMatrix4(instance.matrix);
                    box.union(instanceBox);
                });

                return box;
            };

        };

        return InstancesManager;
    });
