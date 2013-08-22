/*global sceneManager*/
define(function() {

    var LoaderManager = function() {
        this.ColladaeLoader = null;
        this.StlLoader = null;
        this.BinaryLoader = null;
    };

    LoaderManager.prototype = {

        parseFile: function ( filename, texturePath, computeVertexNormals, callback ) {

            var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

            switch ( extension ) {
                case 'dae':

                    if(this.ColladaeLoader == null) {
                        this.ColladaeLoader = new THREE.ColladaLoader();
                    }

                    this.ColladaeLoader.load( filename , function(collada) {
                        var dae = collada.scene;
                        dae.scale.x = dae.scale.y = dae.scale.z = 1;
                        dae.updateMatrix();
                        callback(dae);
                    });

                    break;

                case 'stl':

                    if(this.StlLoader == null) {
                        this.StlLoader = new THREE.STLLoader();
                    }

                    this.StlLoader.addEventListener( 'load', function ( stl ) {
                        var geometry = stl.content;
                        var material = new THREE.MeshPhongMaterial();
                        var mesh = new THREE.Mesh(geometry,material);

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