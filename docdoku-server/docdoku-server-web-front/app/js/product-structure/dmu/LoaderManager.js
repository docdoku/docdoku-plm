/*global _,define,THREE*/
define(['views/progress_bar_view'], function (ProgressBarView) {
	'use strict';
    var LoaderManager = function (options) {

        this.ColladaLoader = null;
        this.STLLoader = null;
        this.BinaryLoader = null;
        this.OBJLoader = null;
        this.JSONLoader = null;

        _.extend(this, options);

        if (this.progressBar) {
            this.listenXHRProgress();
        }
    };

    var defaultMaterial = new THREE.MeshLambertMaterial({color:new THREE.Color(0x62697B)});

    function setShadows(object){
        object.traverse( function ( o ) {
            if ( o instanceof THREE.Mesh) {
                o.castShadow = true;
                o.receiveShadow = true;
            }
        });
    }

    function updateMaterial(object){
        object.traverse( function ( o ) {
            if ( o instanceof THREE.Mesh && !o.material.name) {
                o.material = defaultMaterial;
            }
        });
    }

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


            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch (extension) {

                case 'obj' :

                    if (this.OBJLoader === null) {
                        this.OBJLoader = new THREE.OBJLoader();
                    }

                    this.OBJLoader.load(filename, texturePath+'/attachedfiles/', function ( object ) {
                        setShadows(object);
                        updateMaterial(object);
                        callbacks.success(object);
                    });


                    break;

                case 'dae':

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
                        var object = new THREE.Object3D();
                        object.add(new THREE.Mesh(combined));
                        setShadows(object);
                        updateMaterial(object);
                        callbacks.success(object);

                    });

                    break;

                case 'stl':
                    if (this.STLLoader === null) {
                        this.STLLoader = new THREE.STLLoader();
                    }

                    this.STLLoader.load(filename, function(geometry){
                        var object = new THREE.Object3D();
                        object.add(new THREE.Mesh(geometry));
                        setShadows(object);
                        updateMaterial(object);
                        callbacks.success(object);
                    });

                    break;

                // Used for json files only (no referenced buffers)
                case 'json':
                    if (this.JSONLoader === null) {
                        this.JSONLoader = new THREE.JSONLoader();
                    }

                    this.JSONLoader.load(filename, function (geometry, materials) {
                        geometry.dynamic = false;
                        var object = new THREE.Object3D();
                        object.add(new THREE.Mesh(geometry,new THREE.MeshFaceMaterial(materials)));
                        setShadows(object);
                        callbacks.success(object);
                    }, texturePath+'/attachedfiles/');

                    break;

                // Used for binary json files only (referenced buffers - bin file)
                case 'js':

                    if (this.BinaryLoader === null) {
                        this.BinaryLoader = new THREE.BinaryLoader();
                    }

                    this.BinaryLoader.load(filename, function (geometry, materials) {
                        var _material = new THREE.MeshPhongMaterial({color: materials[0].color, overdraw: true });
                        geometry.dynamic = false;
                        var object = new THREE.Object3D();
                        object.add(new THREE.Mesh(geometry,_material));
                        setShadows(object);
                        callbacks.success(object);
                    }, texturePath);

                    break;


                default:
                    break;

            }
        }
    };
    return LoaderManager;
});
