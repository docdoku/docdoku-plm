/*global sceneManager*/
function LevelGeometry(filename, quality, computeVertexNormals) {
    this.fileName = filename;
    this.quality = quality;
}

LevelGeometry.prototype = {
    loadMesh: function(callback) {
        instancesManager.loadMeshFromFile(this.fileName, function(mesh) {
            callback(mesh);
        });
    }
};
