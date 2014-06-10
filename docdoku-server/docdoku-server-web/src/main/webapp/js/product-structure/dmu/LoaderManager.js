/*global ColladaLoader2*/
define(["views/progress_bar_view"], function (ProgressBarView) {

    var LoaderManager = function (options) {

        this.ColladaLoader = null;
        this.StlLoader = null;
        this.BinaryLoader = null;

        _.extend(this,options);

        if(this.progressBar){
            this.listenXHRProgress();
        }
        // Reuse material
        this.material = new THREE.MeshPhongMaterial( { transparent:true, color: new THREE.Color(0xbbbbbb) } );

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

                if (arguments[1].indexOf("/files/") === 0) {

                    var totalAdded = false,
                        totalLoaded = 0,
                        xhrLength = 0;

                    this.addEventListener("loadstart", function () {
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
                        }, 20);
                    }, false);
                }

                return _xhrOpen.apply(this, arguments);
            };
        },

        parseFile: function (filename, texturePath, callbacks) {

            var material = this.material;

            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch (extension) {
                case 'dae':

                    if (this.ColladaLoader == null) {
                        this.ColladaLoader = new ColladaLoader2();
                    }

                    this.ColladaLoader.load(filename, function (collada) {

                        var geometries = [], combined = new THREE.Geometry();
                        getMeshGeometries(collada.threejs.scene, geometries);

                        // Merge all sub meshes into one
                        _.each(geometries, function (geometry) {
                            THREE.GeometryUtils.merge(combined, geometry);
                        });

                        combined.dynamic = false;
                        combined.mergeVertices();

                        combined.computeBoundingSphere();

                        callbacks.success(combined, material);

                    });

                    break;

                case 'stl':

                    if (this.StlLoader == null) {
                        this.StlLoader = new THREE.STLLoader();
                    }

                    this.StlLoader.addEventListener('load', function (stl) {
                        var geometry = stl.content;
                        callbacks.success(geometry, material);
                    });
                    this.StlLoader.load(filename);

                    break;

                case 'js':
                case 'json':
                    if (this.BinaryLoader == null) {
                        this.BinaryLoader = new THREE.BinaryLoader();
                    }

                    this.BinaryLoader.load(filename, function (geometry, materials) {
                        var _material = new THREE.MeshPhongMaterial( {color: materials[0].color,  overdraw: true } );
                        geometry.dynamic = false;
                        callbacks.success(geometry, _material);
                    }, texturePath);

                    break;

                default: break;

            }
        }
    };

    return LoaderManager;

});