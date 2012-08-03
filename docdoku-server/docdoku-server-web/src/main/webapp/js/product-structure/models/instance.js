window.Instance = Backbone.Model.extend({

    defaults: {
        position: {
            x: 0,
            y: 0,
            z: 0
        },
        rotation: {
            x: 0,
            y: 0,
            z: 0
        },
        part: null,
        mesh: null,
        onScene: false,
        idle: true,
        material: null
    },

    getPart: function() {
        return this.get('part');
    },

    getScore: function(cameraPosition) {
        return this.getPart().getScoreCoeff() * this.getDistance(cameraPosition);
    },

    getPosition: function() {
        return this.get('position');
    },

    getRotation: function() {
        return this.get('rotation');
    },

    getMesh: function() {
        return this.get('mesh');
    },

    getMaterial: function() {
        return this.get('material');
    },

    getDistance: function(cameraPosition) {
        return Math.sqrt(Math.pow(cameraPosition.x - this.getPosition().x, 2) + Math.pow(cameraPosition.y - this.getPosition().y, 2) + Math.pow(cameraPosition.z - this.getPosition().z, 2));
    },

    getMeshForLoading: function(callback) {

        var self = this;
        this.getPart().getGeometry(function(geometry) {
            var mesh = new THREE.Mesh(geometry, self.getMaterial());
            mesh.position.set(self.getPosition().x, self.getPosition().y, self.getPosition().z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.getRotation().x, self.getRotation().y, self.getRotation().z);
            mesh.doubleSided = false;
            self.set('mesh', mesh);
            callback(self.getMesh());
        });

    },

    getMeshToRemove: function() {
        return this.getMesh();
    },

    onAdd: function() {
        this.getPart().onAddInstanceOnScene();
    },

    onRemove: function() {
        this.set('mesh', null);
        this.getPart().onRemoveInstanceFromScene();
    }

});