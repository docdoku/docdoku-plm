/*global sceneManager, ColladaLoader2*/
define(["views/progress_bar_view"], function (ProgressBarView) {

    var LoaderManager = function (options) {

        this.ColladaLoader = null;
        this.StlLoader = null;
        this.BinaryLoader = null;

        _.extend(this,options);

        if(this.progressBar){
            this.listenXHR();
        }

    };

    /*
     * Parse all meshes in collada object given by COlladaLoader
     * */
    function getMeshes(collada, geometries) {
        if (collada) {
            _.each(collada.children, function (child) {
                if (child instanceof THREE.Mesh && child.geometry) {
                    geometries.push(child.geometry);
                }
                getMeshes(child, geometries);
            });
        }
    }

    LoaderManager.prototype = {

        listenXHR: function () {

            // Override xhr open prototype
            var pbv = new ProgressBarView().render();
            var xhrCount = 0;
            var _xhrOpen = XMLHttpRequest.prototype.open;

            XMLHttpRequest.prototype.open = function () {

                if (arguments[1].indexOf("/files/") === 0) {

                    var totalAdded = false,
                        totalLoaded = 0,
                        xhrLength = 0;

                    this.addEventListener("loadstart", function (pe) {
                        xhrCount++;
                    }, false);

                    this.addEventListener("progress", function (pe) {

                        if (xhrLength == 0) {
                            xhrLength = pe.total;
                        }

                        if (totalAdded == false) {
                            pbv.addTotal(xhrLength);
                            totalAdded = true;
                        }

                        pbv.addLoaded(pe.loaded - totalLoaded);
                        totalLoaded = pe.loaded;

                    }, false);

                    this.addEventListener("loadend", function () {
                        xhrCount--;
                        setTimeout(function () {
                            pbv.removeXHRData(xhrLength);
                        }, 100);
                    }, false);
                }

                return _xhrOpen.apply(this, arguments);
            };
        },

        parseFile: function (filename, texturePath, callback) {

            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch (extension) {
                case 'dae':

                    if (this.ColladaLoader == null) {
                        this.ColladaLoader = new ColladaLoader2();
                    }

                    this.ColladaLoader.load(filename, function (collada) {

                        // Merge all sub meshes into one
                        var geometries = [],
                            combined = new THREE.Geometry(),
                            materials = collada.threejs.materials[0];

                        getMeshes(collada.threejs.scene, geometries);

                        _.each(geometries, function (geometry) {
                            THREE.GeometryUtils.merge(combined, geometry);
                        });

                        combined.dynamic = false;
                        combined.mergeVertices();
                        materials.transparent = true;
                        materials.color = new THREE.Color(0xbbbbbb);

                        callback(new THREE.Mesh(combined, materials));

                    });

                    break;

                case 'stl':

                    if (this.StlLoader == null) {
                        this.StlLoader = new THREE.STLLoader();
                    }

                    this.StlLoader.addEventListener('load', function (stl) {
                        var geometry = stl.content,
                            materials = new THREE.MeshPhongMaterial();

                        materials.transparent = true;
                        materials.color = new THREE.Color(0xbbbbbb);
                        callback(new THREE.Mesh(geometry, materials));
                    });
                    this.StlLoader.load(filename);

                    break;

                case 'js':
                case 'json':

                    if (this.BinaryLoader == null) {
                        this.BinaryLoader = new THREE.BinaryLoader();
                    }

                    this.BinaryLoader.load(filename, function (geometry, materials) {
                        _.each(materials, function (material) {
                            material.transparent = true;
                            material.color = new THREE.Color(0xbbbbbb);
                        });
                        geometry.dynamic = false;
                        callback(new THREE.Mesh(geometry, new THREE.MeshFaceMaterial(materials)));
                    }, texturePath);

                    break;

                default: break;

            }
        }
    };

    return LoaderManager;

});