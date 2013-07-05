/*global isIpad,LevelGeometry,sceneManager*/
define(function() {

    var PartIterationVisualization = function(partIterationParams) {
        this.partIterationId = partIterationParams.partIterationId;
        this.files = partIterationParams.files;
        this.attributes = partIterationParams.attributes;
        this.initialize();
    };

    PartIterationVisualization.prototype = {

        hasGeometry: function() {
            return this.files.length > 0;
        },

        initialize: function() {

            this.idle = true;

            if (this.hasGeometry()) {

                this.levels = [];

                var radiusAttribute = _.find(this.attributes, function(attribute) {
                    return attribute.name == 'radius';
                });

                if (radiusAttribute) {
                    this.radius = radiusAttribute.value;
                }

                var self = this;

                _.each(this.files, function(file) {
                    var filename = '/files/' + file.fullName;
                    switch (file.quality) {
                        case 0:
                            self.addLevelGeometry(filename, file.quality,  false);
                            break;
                        case 1:
                            self.addLevelGeometry(filename, file.quality, true);
                            break;
                    }
                });

            }

        },

        addLevelGeometry: function(filename, quality, computeVertexNormals) {
            for (var i = 0; i<this.levels.length ; i++) {
                if (quality < this.levels[i].quality) {
                    break;
                }
            }
            this.levels.splice(i, 0, new LevelGeometry(filename, quality, computeVertexNormals));
        },

        getLevelGeometry: function(rating) {
            for (var i = 0; i < this.levels.length ; i++) {
                if (rating > sceneManager.levelGeometryValues[i]) {
                    return this.levels[i];
                }
            }
            //no level found for this rating
            return null;
        },

        getBestLevelGeometry: function() {
            return this.levels[this.levels.length-1];
        }

    };

   return PartIterationVisualization;

});