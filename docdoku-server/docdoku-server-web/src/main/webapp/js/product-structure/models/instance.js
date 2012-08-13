window.Instance = function(material, part, tx, ty, tz, rx, ry, rz) {

    this.position = {
        x: tx,
        y: ty,
        z: tz
    };

    this.rotation = {
        x: rx,
        y: ry,
        z: rz
    };

    this.part = part;
    this.mesh = null;
    this.onScene = false;
    this.idle = true;
    this.material = material;
    this.isHigh = false;

}

Instance.prototype = {

    getScore: function(cameraPosition) {
        return this.part.scoreCoeff * this.getDistance(cameraPosition);
    },

    getDistance: function(cameraPosition) {
        return Math.sqrt(Math.pow(cameraPosition.x - this.position.x, 2) + Math.pow(cameraPosition.y - this.position.y, 2) + Math.pow(cameraPosition.z - this.position.z, 2));
    },

    getMeshHighForLoading: function(callback) {

        var self = this;
        this.part.getGeometryHigh(function(geometry) {
            var mesh = new THREE.Mesh(geometry, self.material);
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.doubleSided = false;
            self.mesh = mesh;
            callback(self.mesh);
        });

    },

    getMeshLowForLoading: function(callback) {

        var self = this;
        this.part.getGeometryLow(function(geometry) {
            var mesh = new THREE.Mesh(geometry, self.material);
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.doubleSided = false;
            self.mesh = mesh;
            callback(self.mesh);
        });

    },

    getMeshToRemove: function() {
        return this.mesh;
    },

    onAddHigh: function() {
        this.part.onAddHighInstance();
    },

    onRemoveHigh: function() {
        this.mesh = null;
        this.part.onRemoveHighInstance();
    },

    onAddLow: function() {
        this.part.onAddLowInstance();
    },

    onRemoveLow: function() {
        this.mesh = null;
        this.part.onRemoveLowInstance();
    },

    onRemoveInstanceFromScene: function() {
        this.mesh = null;
        this.part.onRemoveInstanceFromScene();
    },

    onAddInstanceOnScene: function() {
        this.part.onAddInstanceOnScene();
    }

};