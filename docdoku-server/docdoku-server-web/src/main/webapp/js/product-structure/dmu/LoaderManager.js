/*global sceneManager, ColladaLoader2*/
define(function() {

    var LoaderManager = function() {
        this.ColladaLoader = null;
        this.StlLoader = null;
        this.BinaryLoader = null;
    };

    function getMeshes(collada,geometries){
        if(collada){
            _.each(collada.children,function(child){
                if(child instanceof THREE.Mesh && child.geometry){
                    geometries.push(child.geometry);
                }
                getMeshes(child,geometries);
            });
        }
    }

    LoaderManager.prototype = {

        parseFile: function ( filename, texturePath, computeVertexNormals, callback ) {

            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch ( extension ) {
                case 'dae':

                    if(this.ColladaLoader == null) {
                        this.ColladaLoader = new ColladaLoader2();
                    }

                    this.ColladaLoader.load( filename , function(collada) {

                        // Merge all sub meshes into one
                        var geometries = [];
                        getMeshes(collada.threejs.scene,geometries);
                        var combined = new THREE.Geometry();

                        _.each(geometries,function(geometry){
                            THREE.GeometryUtils.merge( combined, geometry);
                        });

                        if (computeVertexNormals) {
                            combined.computeVertexNormals();
                        }

                        var materials = collada.threejs.materials[0];
                        materials.wireframe = sceneManager.wireframe;
                        materials.transparent = true ;

                        combined.dynamic = false;
                        combined.mergeVertices();

                        var mesh = new THREE.Mesh( combined,materials);
                        callback(mesh);

                    });

                    break;

                case 'stl':

                    if(this.StlLoader == null) {
                        this.StlLoader = new THREE.STLLoader();
                    }

                    this.StlLoader.addEventListener( 'load', function ( stl ) {
                        var geometry = stl.content;
                        var materials = new THREE.MeshPhongMaterial();
                        materials.wireframe = sceneManager.wireframe;
                        materials.transparent = true ;
                        var mesh = new THREE.Mesh(geometry,materials);
                        callback(mesh);
                    });

                    this.StlLoader.load( filename );

                    break;

                case 'js':
                case 'json':

                    if(this.BinaryLoader == null) {
                        this.BinaryLoader = new THREE.BinaryLoader();
                    }

                    this.BinaryLoader.load(filename, function(geometry, materials) {
                        if (computeVertexNormals) {
                            geometry.computeVertexNormals();
                        }
                        _.each(materials, function(material) {
                            material.wireframe = sceneManager.wireframe;
                            material.transparent = true;
                        });
                        geometry.dynamic = false;
                        var mesh = new THREE.Mesh(geometry, new THREE.MeshFaceMaterial(materials));
                        callback(mesh);
                    }, texturePath);

            }
        }
    };

    return LoaderManager;

});