define(function() {

    var Part = Backbone.Model.extend({

        defaults: {
            workspaceId: null,
            name: null,
            number: null,
            version: null,
            description: null,
            files: null,
            components: [],
            isNode: false
        },

        idAttribute: "number",

        initialize : function() {
            this.idle = true;
            this.levels = [];

            if (this.getComponents().length > 0) {
                this.set('isNode', true, {silent: true});
            }

            var radiusAttribute = _.find(this.getAttributes(), function(attribute) {
                return attribute.name == 'radius';
            });

            if (radiusAttribute) {
                this.radius = radiusAttribute.value;
            }

            var self = this;

            _.each(this.getFiles(), function(file) {
                var filename = '/files/' + file.fullName;
                switch (file.quality) {
                    case 0:
                        self.addLevelGeometry(filename, 0.5, false);
                        break;
                    case 1:
                        self.addLevelGeometry(filename, 0.2, true);
                        break;
                }
            });

            _.each(this.get('instances'), function(instanceRaw) {

                var instance = new Instance(
                    self,
                    instanceRaw.tx*10,
                    instanceRaw.ty*10,
                    instanceRaw.tz*10,
                    instanceRaw.rx,
                    instanceRaw.ry,
                    instanceRaw.rz
                );

                sceneManager.instances.push(instance);

            });

        },

        getName : function() {
            return this.get('name');
        },

        getNumber : function() {
            return this.get('number');
        },

        getVersion : function() {
            return this.get('version');
        },

        getDescription : function() {
            return this.get('description');
        },

        getFiles : function() {
            return this.get('files');
        },

        getComponents : function() {
            return this.get('components');
        },

        getAttributes: function() {
            return this.get('attributes')
        },

        getWorkspaceId : function() {
            return this.get('workspaceId');
        },

        getIteration : function() {
            return this.get('iteration');
        },

        isStandardPart : function() {
            return this.get('standardPart');
        },

        isNode: function() {
            return this.get('isNode');
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

    });

    return Part;

});