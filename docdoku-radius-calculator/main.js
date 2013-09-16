var THREE = require("three");
var _ = require('underscore')._;
var fs = require('fs');

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

function computeRadius(geometry) {
    geometry.computeBoundingSphere();
    console.log(JSON.stringify({radius: geometry.boundingSphere.radius}));
}

(function (args) {
    var filename = args[0];

    if (!filename) {
        console.log("Filename argument is missing, usage : node main.js [filename]")
        process.exit(1);
    }

    try {
        fs.statSync(filename);
    } catch (ex) {
        console.log("File '" + filename + "' does not exists. Exiting.");
        process.exit(1);
    }

    var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();
    var texturePath = filename.substring(0, filename.lastIndexOf('/'));


    switch (extension) {
        case 'dae':
            require("./lib/ColladaLoader");
            var loader = new THREE.ColladaLoader();

            loader.load(filename, function (collada) {

                var geometries = [];
                var combined = new THREE.Geometry();

                getMeshGeometries(collada.threejs.scene, geometries);

                _.each(geometries, function (geometry) {
                    THREE.GeometryUtils.merge(combined, geometry);
                });

                combined.mergeVertices();
                computeRadius(combined);

            }, texturePath);

            break;

        case 'stl':

            require("./lib/STLLoader");
            loader = new THREE.STLLoader();

            loader.addEventListener('load', function (stl) {
                var geometry = stl.content;
                computeRadius(geometry);
            });
            loader.load(filename);

            break;

        case 'js':
        case 'json':
            require("./lib/BinaryLoader");
            var loader = new THREE.BinaryLoader();
            loader.load(filename, function (geometry, materials) {
                computeRadius(geometry);
            }, texturePath);

            break;

        default:
            console.log("Cannot process " + extension + " files. Allowed files are [json, js, dae, stl]");
            process.exit(1);
            break;
    }
})(process.argv.splice(2));

