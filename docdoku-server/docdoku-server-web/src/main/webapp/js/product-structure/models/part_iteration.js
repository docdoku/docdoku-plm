define(function() {

    var PartIteration = function(partIterationParams) {
        this.partIterationId = partIterationParams.partIterationId;
        this.files = partIterationParams.files;
        this.attributes = partIterationParams.attributes;
        this.initialize();
    }

    PartIteration.prototype = {

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

                var levelGeometry1 = (isIpad) ? 1.0 : 0.7;
                var levelGeometry2 = (isIpad) ? 0.7 : 0.4;

                var self = this;

                _.each(this.files, function(file) {
                    var filename = '/files/' + file.fullName;
                    switch (file.quality) {
                        case 0:
                            self.addLevelGeometry(filename, levelGeometry1, false);
                            break;
                        case 1:
                            self.addLevelGeometry(filename, levelGeometry2, true);
                            break;
                    }
                });

            }

        },

        addLevelGeometry: function(filename, visibleFromRating, computeVertexNormals) {
            for (var i = 0; i<this.levels.length ; i++) {
                if (visibleFromRating < this.levels[i].visibleFromRating) {
                    break;
                }
            }
            this.levels.splice(i, 0, new LevelGeometry(filename, visibleFromRating, computeVertexNormals));
        },

        getLevelGeometry: function(rating) {
            for (var i = this.levels.length-1; i>=0 ; i--) {
                if (rating > this.levels[i].visibleFromRating) {
                    return this.levels[i];
                }
            }
            //no level found for this rating
            return null;
        }

    }

   return PartIteration;

});