var THREE = require("three");
require("./lib/OBJLoader");

var getMeshGeometries = function (object, geometries) {
    if (object) {
        object.children.forEach(function (child) {
            if (child instanceof THREE.Mesh && child.geometry) {
                geometries.push(child.geometry);
            }
            getMeshGeometries(child, geometries);
        });
    }
};

var calculateBox = function (data, callback) {

    var loader = new THREE.OBJLoader();

    var geometries = [];
    var combined = new THREE.Geometry();

    var object = loader.parse(data);

    getMeshGeometries(object, geometries);

    geometries.forEach(function (geometry) {
        THREE.GeometryUtils.merge(combined, geometry);
    });

    combined.mergeVertices();

    combined.computeBoundingBox();
    callback(combined.boundingBox);

};

module.exports = {
    calculateBox:calculateBox
};