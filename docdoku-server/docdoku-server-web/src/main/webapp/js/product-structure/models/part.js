window.Part = Backbone.Model.extend({

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
        this.geometryLow = null;
        this.geometryHigh = null;
        this.instancesLow = 0;
        this.instancesHigh = 0;
        this.instancesOnScene = 0;
        this.scoreCoeff = this.isStandardPart() ? 2 : 1;

        if (this.getComponents().length > 0) {
            this.set('isNode', true, {silent: true});
        }

        var self = this;

        _.each(this.getFiles(), function(file) {
            switch (file.quality) {
                case 0:
                    self.filenameHigh = '/files/' + file.fullName;
                    break;
                case 1:
                    self.filenameLow = '/files/' + file.fullName;
                    break;
            }
        });

        _.each(this.get('instances'), function(instanceRaw) {

            var instance = new Instance(
                sceneManager.material,
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

    getGeometryHigh: function(callback) {
        if (this.geometryHigh == null) {
            var self = this;
            this.idle = false;
            this.getLoader().load(this.filenameHigh, function(geometry) {
                self.geometryHigh = geometry;
                self.idle = true;
                callback(self.geometryHigh);
            }, 'images');
        } else {
            callback(this.geometryHigh);
        }

    },

    getGeometryLow: function(callback) {
        if (this.geometryLow == null) {
            var self = this;
            this.idle = false;
            this.getLoader().load(this.filenameLow, function(geometry) {
                geometry.computeVertexNormals();
                self.geometryLow = geometry;
                self.idle = true;
                callback(self.geometryLow);
            }, 'images');
        } else {
            callback(this.geometryLow);
        }
    },

    onAddHighInstance: function() {
        this.instancesHigh++;
    },

    onRemoveHighInstance: function() {
        if (--this.instancesHigh == 0) {
            this.clearHighInstance();
        }
    },

    onAddLowInstance: function() {
        this.instancesLow++;
    },

    onRemoveLowInstance: function() {
        if (--this.instancesLow == 0) {
            this.clearLowInstance();
        }
    },

    clearHighInstance: function() {
        this.geometryHigh = null;
    },

    clearLowInstance: function() {
        this.geometryLow = null;
    },

    onAddInstanceOnScene: function() {
        this.instancesOnScene++;
    },

    onRemoveInstanceFromScene: function() {
        if (--this.instancesOnScene == 0) {
            this.clear();
        }
    },

    clear: function() {
        this.geometryLow = null;
        this.geometryHigh = null;
    },

    getLoader: function() {
        return sceneManager.loader;
    }

});