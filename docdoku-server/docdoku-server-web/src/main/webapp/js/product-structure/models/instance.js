window.Instance = function(part, tx, ty, tz, rx, ry, rz) {

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

    this.levelGeometry = null;
    this.part = part;
    this.mesh = null;
    this.idle = true;

}

Instance.prototype = {

    getRating: function() {
        return this.part.radius / this.getDistance(sceneManager.camera.position);
    },

    getDistance: function(position) {
        return Math.sqrt(Math.pow(position.x - this.position.x, 2) + Math.pow(position.y - this.position.y, 2) + Math.pow(position.z - this.position.z, 2));
    },

    /**
     * Update instance 3d model if needed
     */
    update: function() {

        if (this.idle && this.part.idle) {

            this.idle = false;
            this.part.idle = false;

            var rating = this.getRating();
            //get the level corresponding of this rating
            var levelGeometry = this.part.getLevelGeometry(rating);

            //if we need to switch geometry
            if (this.needSwitch(levelGeometry)) {

                var self = this;

                this.switchTo(levelGeometry, function() {
                    self.idle = true;
                    self.part.idle = true;
                });

            } else {
                this.idle = true;
                this.part.idle = true;
            }

        }

    },

    needSwitch: function(levelGeometry) {
        return this.levelGeometry != levelGeometry;
    },

    switchTo: function(levelGeometry, callback) {

        //if we had a new level geometry to load on the scene
        if (levelGeometry != null) {
            var self = this;
            //load the mesh corresponding to the new level
            this.loadMeshFromLevelGeometry(levelGeometry, function(mesh) {
                //add new mesh to the scene
                sceneManager.scene.add(mesh);
                //notify that we have one instance at this level on the scene
                levelGeometry.onAdd();

                //clear previous state
                self.clearMeshAndLevelGeometry();

                //save level and mesh for further reuse
                self.levelGeometry = levelGeometry;
                self.mesh = mesh;
                callback();
            });
        } else {
            this.clearMeshAndLevelGeometry();
            callback();
        }

    },

    loadMeshFromLevelGeometry: function(levelGeometry, callback) {
        var self = this;
        levelGeometry.getGeometry(function(geometry) {
            var mesh = new THREE.Mesh(geometry, sceneManager.material);
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.doubleSided = false;
            callback(mesh);
        });
    },

    clearMeshAndLevelGeometry: function() {
        //remove previous mesh from scene if any
        if (this.mesh) {
            sceneManager.scene.remove(this.mesh);
            this.mesh = null;
        }

        //notify that we are not using this level anymore
        if (this.levelGeometry) {
            this.levelGeometry.onRemove();
            this.levelGeometry = null;
        }
    }

};