function LevelGeometry(filename, visibleFromRating, computeVertexNormals) {
    this.filename = filename;
    this.visibleFromRating = visibleFromRating;
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
            sceneManager.loader.load(this.filename, function(geometry) {
                if (self.computeVertexNormals) {
                    geometry.computeVertexNormals();
                }
                self.geometry = geometry;
                callback(self.geometry);
            }, 'images');
        } else {
            callback(this.geometry);
        }
    }

}