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

        var levelGeometryForGivenRating = fullQuality ?
            this.getPartIteration().getBestLevelGeometry(): this.getPartIteration().getLevelGeometry(rating);

        if(this.mesh === null){
            // Check if partiteration has already an instance loaded with mesh not null, if so, clone this mesh.
            // If no mesh already exists, load it.
            this.loadLevelGeometry(levelGeometryForGivenRating);
        }
        else if(this.currentFullQuality != fullQuality){
            this.loadLevelGeometry(levelGeometryForGivenRating);
        }
        else if(this.currentRating != rating){
            this.loadLevelGeometry(levelGeometryForGivenRating);
        }else{
            return ;
        }

        // old rating
        this.currentRating = rating;
        this.currentFullQuality = fullQuality;
    },

    loadLevelGeometry:function(levelGeometry){
        var self = this ;
        levelGeometry.loadMesh(function(mesh){

            mesh.instanceId = self.id;
            mesh.partIterationId = self.partIterationId;
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.initialPosition = {x:mesh.position.x,y:mesh.position.y,z:mesh.position.z};
            self.clearMesh();
            sceneManager.addMesh(mesh);
            self.mesh = mesh;

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