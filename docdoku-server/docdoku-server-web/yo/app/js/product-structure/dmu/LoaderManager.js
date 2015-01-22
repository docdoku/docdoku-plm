/*global _,define,THREE*/
define(['views/progress_bar_view'], function (ProgressBarView) {
	'use strict';
    var LoaderManager = function (options) {

        this.ColladaLoader = null;
        this.STLLoader = null;
        this.BinaryLoader = null;
        this.OBJLoader = null;

        _.extend(this, options);

        if (this.progressBar) {
            this.listenXHRProgress();
        }
    };

    /*
     * Parse all meshes geometries in collada object given by COlladaLoader
     * */
    function getMeshGeometries(collada, geometries) {
        if (collada) {
            _.each(collada.children, function (child) {
                if (child instanceof THREE.Mesh && child.geometry) {
                    geometries.push(child.geometry);
                }
                getMeshGeometries(child, geometries);
            });
        }
    }

    LoaderManager.prototype = {

        listenXHRProgress: function () {

            // Override xhr open prototype
            var pbv = new ProgressBarView().render();
            var xhrCount = 0;
            var _xhrOpen = XMLHttpRequest.prototype.open;

            XMLHttpRequest.prototype.open = function () {

                if (arguments[1].indexOf('/api/files/') === 0) {

                    var totalAdded = false,
                        totalLoaded = 0,
                        xhrLength = 0;

                    this.addEventListener('loadstart', function () {
                        xhrCount++;
                    }, false);

                    this.addEventListener('progress', function (pe) {

                        if (xhrLength === 0) {
                            xhrLength = pe.total;
                        }

                        if (totalAdded === false) {
                            pbv.addTotal(xhrLength);
                            totalAdded = true;
                        }

                        pbv.addLoaded(pe.loaded - totalLoaded);
                        totalLoaded = pe.loaded;

                    }, false);

                    this.addEventListener('loadend', function () {
                        xhrCount--;
                        setTimeout(function () {
                            pbv.removeXHRData(xhrLength);
                        }, 20);
                    }, false);
                }

                return _xhrOpen.apply(this, arguments);
            };
        },

        parseFile: function (filename, texturePath, callbacks) {
            var material;
            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch (extension) {

                case 'obj' :

                    if (this.OBJLoader === null) {
                        this.OBJLoader = new THREE.OBJLoader();
                    }

                    material = new THREE.MeshPhongMaterial({  transparent: true, color: new THREE.Color(0xbbbbbb) });
                    material.side = THREE.doubleSided;

                    this.OBJLoader.load(filename, function ( object ) {

                        var geometries = [], combined = new THREE.Geometry();
                        getMeshGeometries(object, geometries);

                        // Merge all sub meshes into one
                        _.each(geometries, function (geometry) {
                            combined.merge(geometry);
                        });

                        combined.dynamic = false;
                        combined.mergeVertices();

                        combined.computeBoundingSphere();

                        callbacks.success(combined, material);

                    }, function onProgress(){},  function onError(){});


                    break;

                case 'dae':
                     material = new THREE.MeshPhongMaterial({ transparent: true, color: new THREE.Color(0xbbbbbb) });

                    if (this.ColladaLoader === null) {
                        this.ColladaLoader = new THREE.ColladaLoader();
                    }

                    this.ColladaLoader.load(filename, function (collada) {

                        var geometries = [], combined = new THREE.Geometry();
                        getMeshGeometries(collada.scene, geometries);

                        // Merge all sub meshes into one
                        _.each(geometries, function (geometry) {
                            combined.merge(geometry);
                        });

                        combined.dynamic = false;
                        combined.mergeVertices();

                        combined.computeBoundingSphere();

                        callbacks.success(combined, material);

                    });

                    break;

                case 'stl':
                    if (this.STLLoader === null) {
                        this.STLLoader = new THREE.STLLoader();
                    }

                    material = new THREE.MeshPhongMaterial({ transparent: true, color: new THREE.Color(0xbbbbbb) });

                    this.STLLoader.load(filename, function(geometry){
                        callbacks.success(geometry, material);
                    });

                    break;

                case 'js':
                case 'json':
                    if (this.BinaryLoader === null) {
                        this.BinaryLoader = new THREE.BinaryLoader();
                    }

                    this.BinaryLoader.load(filename, function (geometry, materials) {
                        var _material = new THREE.MeshPhongMaterial({color: materials[0].color, overdraw: true });
                        geometry.dynamic = false;
                        callbacks.success(geometry, _material);
                    }, texturePath);

                    break;

                default:
                    break;

            }
        }
    };
    return LoaderManager;
});
