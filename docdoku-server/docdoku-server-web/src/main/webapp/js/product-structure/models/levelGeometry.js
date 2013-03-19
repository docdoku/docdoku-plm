/*global sceneManager*/
function LevelGeometry(filename, quality, computeVertexNormals) {
    this.filename = filename;
    this.quality = quality;
    this.geometry = null;
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
        this.geometry = null;
    },

    getGeometry: function(callback) {
        if (this.geometry == null) {
            var self = this;
            var texturePath = this.filename.substring(0,this.filename.lastIndexOf('/'));
            sceneManager.loader.load(this.filename, function(geometry, materials) {
                if (self.computeVertexNormals) {
                    geometry.computeVertexNormals();
                }
                _.each(materials, function(material) {
                    material.wireframe = sceneManager.wireframe;
                    material.transparent = true;
                });
                self.geometry = geometry;
                self.materials = materials;
                callback(self.geometry, self.materials);
            }, texturePath);
        } else {
            callback(this.geometry, this.materials);
        }
    }

};