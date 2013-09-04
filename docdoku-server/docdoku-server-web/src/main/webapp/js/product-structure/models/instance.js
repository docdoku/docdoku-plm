/*global sceneManager*/
var Instance = function(id, partIteration, tx, ty, tz, rx, ry, rz) {

    this.id = id;

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
    this.partIteration = partIteration;
    this.idle = true;

};

Instance.prototype = {

    getRating: function(frustum) {
        var inFrustum = this.isInFrustum(frustum);
        return inFrustum ? this.partIteration.radius *5 / this.getDistance(sceneManager.cameraPosition) : 0;
    },

    getDistance: function(position) {
        return Math.sqrt(Math.pow(position.x - this.position.x, 2) + Math.pow(position.y - this.position.y, 2) + Math.pow(position.z - this.position.z, 2));
    },

    isInFrustum: function(frustum) {

        if (_.isUndefined(this.matrixWorld)) {
            return true;
        }

        var center = new THREE.Vector3();

        var matrix = this.matrixWorld;
        var planes = frustum.planes;
        var negRadius = - this.partIteration.radius * matrix.getMaxScaleOnAxis();

        center.getPositionFromMatrix( matrix );

        for ( var i = 0; i < 6; i ++ ) {
            var distance = planes[ i ].distanceToPoint( center );
            if ( distance < negRadius ) {
                return false;
            }
        }
        return true;
    },

    /**
     * Update instance 3d model if needed
     */
    update: function(frustum) {

        if (this.idle && this.partIteration.idle) {

            this.idle = false;
            this.partIteration.idle = false;
            var levelGeometry = null;

            if(_.isUndefined(this.partIteration.radius)) {
                levelGeometry = this.partIteration.getBestLevelGeometry();
            } else {
                var rating = this.getRating(frustum);
                levelGeometry = this.partIteration.getLevelGeometry(rating);
            }

            //if we need to switch geometry
            if (this.needSwitch(levelGeometry)) {

                var self = this;

                this.switchTo(levelGeometry, function() {
                    self.idle = true;
                    self.partIteration.idle = true;
                });

            } else {
                this.idle = true;
                this.partIteration.idle = true;
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
                sceneManager.addMesh(mesh);
                //notify that we have one instance at this level on the scene
                levelGeometry.onAdd();
                //clear previous state
                self.clearMeshAndLevelGeometry();
                //save level and mesh for further reuse
                self.levelGeometry = levelGeometry;


                callback();
            });
        } else {
            this.clearMeshAndLevelGeometry();
            callback();
        }

    },

    loadMeshFromLevelGeometry: function(levelGeometry, callback) {
        var self = this;
        levelGeometry.getMesh(function(mesh) {

            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);

            if(!mesh.initialPosition){
                mesh.initialPosition = {x:mesh.position.x,y:mesh.position.y,z:mesh.position.z};
            }

            if(!mesh.geometry.boundingBox){
                mesh.geometry.computeBoundingBox();
                mesh.geometry.computeBoundingSphere();
                mesh.geometry.boundingBox.centroid = new THREE.Vector3 (
                    (mesh.geometry.boundingBox.max.x + mesh.geometry.boundingBox.min.x) * 0.5,
                    (mesh.geometry.boundingBox.max.y + mesh.geometry.boundingBox.min.y) * 0.5,
                    (mesh.geometry.boundingBox.max.z + mesh.geometry.boundingBox.min.z) * 0.5
                );
            }
            // Set the radius calculated by THREE
            self.partIteration.radius = mesh.geometry.boundingSphere.radius;

            mesh.matrixAutoUpdate = false;
            mesh.updateMatrix();
            self.matrixWorld = mesh.matrixWorld;

            callback(mesh);
        });
    },

    clearMeshAndLevelGeometry: function() {
       //notify that we are not using this level anymore
       if (this.levelGeometry) {
           sceneManager.scene.remove(this.levelGeometry.mesh);
           this.levelGeometry.onRemove();
           this.levelGeometry = null;
       }
    }

};