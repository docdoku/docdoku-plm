function LevelGeometry(filename, visibleFromRating) {
    this.filename = filename;
    this.visibleFromRating = visibleFromRating;
    this.geometry = null;
    this.instances = 0;
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
                self.geometry = geometry;
                callback(self.geometry);
            }, 'images');
        } else {
            callback(this.geometry);
        }
    }

}