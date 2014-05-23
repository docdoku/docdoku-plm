/*global App*/                                                                                                          // Todo Check if it used
function LevelGeometry(filename, quality, computeVertexNormals) {
    this.fileName = filename;
    this.quality = quality;
}

LevelGeometry.prototype = {
    loadMesh: function(callback) {
        App.instancesManager.loadMeshFromFile(this.fileName, function(mesh) {
            callback(mesh);
        });
    }
};
