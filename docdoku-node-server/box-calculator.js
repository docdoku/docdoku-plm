var THREE = require("three");
var fs = require('fs');

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
    handlePost:function(req, res, next) {

        var filename = req.body.filename;

        if (!filename) {
            console.log("Filename not specified. Exiting.");
            res.end();
            return;
        }

        try {
            fs.statSync(filename);
        } catch (ex) {
            console.log("File '" + filename + "' does not exist. Exiting.");
            res.end();
            return;
        }

        fs.readFile(filename, 'utf-8' ,function (err, data) {
            if (err) {
                console.log("Cannot read the file, is it corrupted or in an other format ?");
                res.end();
            }
            else{
                calculateBox(data,function(box){
                    res.write(JSON.stringify(box));
                    res.end();
                });
            }
        });

    }
};