/*global sceneManager*/
define(function() {

    var LoaderManager = function() {
        this.loader = null;
    }

    LoaderManager.prototype = {

        parseFile: function ( filename, texturePath, computeVertexNormals, callback ) {

            var extension = filename.substr(filename.lastIndexOf('.') + 1);

            switch ( extension ) {
                case 'dae':

                    if(this.loader == null) {
                        this.loader = new THREE.ColladaLoader();
                    }

                    this.loader.load( filename , function(collada) {
                        var dae = collada.scene;
                        dae.scale.x = dae.scale.y = dae.scale.z = 1;
                        dae.updateMatrix();
                        callback(dae);
                    });

                    break;

                case 'stl':

                    if(this.loader == null) {
                        this.loader = new THREE.STLLoader();
                    }

                    this.loader.addEventListener( 'load', function ( stl ) {
                        var geometry = stl.content;
                        var material = new THREE.MeshPhongMaterial();
                        var mesh = new THREE.Mesh(geometry,material);

                        callback(mesh);
                    });

                    this.loader.load( filename );

                    break;

                case 'js':
                case 'json':

                    if(this.loader == null) {
                        this.loader = new THREE.BinaryLoader();
                    }

                    this.loader.load(filename, function(geometry, materials) {
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
    }

    return LoaderManager;

});