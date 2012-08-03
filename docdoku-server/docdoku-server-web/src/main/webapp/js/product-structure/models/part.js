window.Part = Backbone.Model.extend({

    defaults: {
        workspaceId: null,
        name: null,
        number: null,
        version: null,
        description: null,
        instances: [],
        files: null,
        components: [],
        isNode: false,
        loader: null,
        scoreCoeff: null,
        instancesOnScene: 0,
        geometryCached: null,
        idle: true,
        filenameLow: null,
        filenameHigh: null
    },

    idAttribute: "number",

    initialize : function() {

        if (this.getComponents().length > 0) {
            this.set('isNode', true);
        }

        var self = this;
        _.each(this.getFiles(), function(file) {
            switch (file.quality) {
                case 0:
                    self.set('filenameLow', file.fullName);
                    break;
                case 1:
                    self.set('filenameHigh', file.fullName);
                    break;
            }
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

    getInstances : function() {
        return this.get('instances');
    },

    getFiles : function() {
        return this.get('files');
    },

    getComponents : function() {
        return this.get('components');
    },

    getWorkspaceId : function() {
        return this.get('workspaceId');
    },

    getIteration : function() {
        return this.get('iteration');
    },

    getStandardPart : function() {
        return this.get('standardPart');
    },

    isNode: function() {
        return this.get('isNode');
    },

    getGeometryCached: function() {
        return this.get('geometryCached');
    },

    setGeometryCached: function(geometry) {
        this.set('geometryCached', geometry);
    },

    isIdle: function() {
        return this.get('idle');
    },

    setIdle: function(idle) {
        this.set('idle', idle);
    },

    getFilenameLow: function() {
        return this.get('filenameLow');
    },

    getFilenameHigh: function() {
        return this.get('filenameHigh');
    },

    getInstancesOnScene: function() {
        return this.get('instancesOnScene');
    },

    getGeometry: function(callback) {
        if (this.getGeometryCached() == null) {
            var self = this;
            this.setIdle(false);
            this.getLoader().load(this.getFilenameLow(), function(geometry) {
                geometry.computeVertexNormals();
                self.setGeometryCached(geometry);
                self.setIdle(true);
                callback(self.getGeometryCached());
            }, 'images');
        } else {
            callback(this.getGeometryCached());
        }
    },

    onAddInstanceOnScene: function() {
        this.set('instancesOnScene', this.getInstancesOnScene() + 1);
    },

    onRemoveInstanceFromScene: function() {
        this.set('instancesOnScene', this.getInstancesOnScene() - 1);
        if (this.getInstancesOnScene() == 0) {
            this.clear();
        }
    },

    clear: function() {
        this.setGeometryCached(null);
    },

    getLoader: function() {
        return this.get('loader');
    },

    getScoreCoeff: function() {
        return this.get('scoreCoeff');
    }

});