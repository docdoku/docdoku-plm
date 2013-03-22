/*global sceneManager*/
define(function() {

    var LoaderManager = function() {}

    LoaderManager.prototype = {

        parseFile: function ( filename, texturePath, computeVertexNormals, callback ) {

            var extension = filename.substr(filename.lastIndexOf('.') + 1);

            switch ( extension ) {
                case 'dae':

                    var loader = new THREE.ColladaLoader();

                    loader.load( filename , function(collada) {
                        var dae = collada.scene;
                        dae.scale.x = dae.scale.y = dae.scale.z = 1;
                        dae.updateMatrix();
                        callback(dae);
                    });

                    break;

                case 'js':
                case 'json':

                    var loader = new THREE.BinaryLoader();

                    loader.load(filename, function(geometry, materials) {
                        if (computeVertexNormals) {
                            geometry.computeFaceNormals();
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