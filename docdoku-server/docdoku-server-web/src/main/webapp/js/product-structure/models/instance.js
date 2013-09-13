/*global sceneManager*/
var Instance = function(id, partIterationId, tx, ty, tz, rx, ry, rz , radius) {

    this.id = id;
    this.partIterationId = partIterationId;

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

    this.mesh = null;
    this.radius = radius;
    this.currentRating = null;
    this.currentFullQuality = null;
};

Instance.prototype = {

    getPartIteration:function(){
        return instancesManager.getPartIteration(this.partIterationId);
    },

    isOnScene:function(){
        return this.mesh != null && _(sceneManager.scene.children).contains(this.mesh);
    },

    addToScene:function(rating,fullQuality){
        if(this.mesh === null){
            this.loadLevelGeometry(rating,fullQuality);
        }
        // old rating
        this.currentRating = rating;
        this.currentFullQuality = fullQuality;
    },

    updateMesh:function(rating,fullQuality){

        if(this.mesh == null){
            return ; // WTF ? ... should not happen
        }

        this.clearMesh();
        if(this.currentFullQuality != fullQuality){
            this.loadLevelGeometry(rating,fullQuality);
        }

        // old rating
        this.currentRating = rating;
        this.currentFullQuality = fullQuality;
    },

    loadLevelGeometry:function(rating,fullQuality){

        var self = this ;

        var levelGeometryForGivenRating = fullQuality ?
            this.getPartIteration().getBestLevelGeometry(): this.getPartIteration().getLevelGeometry(rating);

        levelGeometryForGivenRating.loadMesh(function(mesh){
            self.mesh = mesh;
            mesh.instanceId = self.id;
            mesh.partIterationId = self.partIterationId;
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.initialPosition = {x:mesh.position.x,y:mesh.position.y,z:mesh.position.z};
            mesh.overdraw = true;
            sceneManager.addMesh(mesh);
        });
    },

    clearMesh:function(){
        if(this.mesh != null){
            sceneManager.removeMesh(this.mesh);
            this.mesh = null;
            this.rating = null;
        }
    },

    toCircularDataSafe:function(){
        return {
            id:this.id,
            position:this.position,
            partIterationId:this.partIterationId,
            radius:this.radius
        }
    }

};