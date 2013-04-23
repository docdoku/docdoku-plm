/*global sceneManager*/
function LevelGeometry(filename, quality, computeVertexNormals) {
    this.filename = filename;
    this.quality = quality;
    this.mesh = null;
    this.instances = 0;
    this.computeVertexNormals = computeVertexNormals;
}

LevelGeometry.prototype = {

    onAdd: function() {
        this.instances++;
    },

    onRemove: function() {
        if (--this.instances == 0) {
            this.clearGeometry();
        }
    },

    clearGeometry: function() {
        this.mesh = null;
    },

    getMesh: function(callback) {
        if (this.mesh == null) {
            var texturePath = this.filename.substring(0,this.filename.lastIndexOf('/'));

            var self = this;
            sceneManager.loaderManager.parseFile(this.filename, texturePath, this.computeVertexNormals, function(mesh) {
                self.mesh = mesh;
                callback(self.mesh);
            });

        } else {
            callback(this.mesh);
        }
    }

};