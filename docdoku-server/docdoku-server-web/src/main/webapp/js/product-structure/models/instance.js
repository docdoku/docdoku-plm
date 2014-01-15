/*global sceneManager,instancesManager*/
var Instance = function(id, partIterationId, matrix, radius) {

    this.id = id;
    this.partIterationId = partIterationId;

    this.position = {
        x: matrix[3],
        y: matrix[7],
        z: matrix[11]
    };
    this.matrix = matrix;

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
            var m = new THREE.Matrix4(self.matrix[0],self.matrix[1],self.matrix[2],self.matrix[3],self.matrix[4],self.matrix[5],self.matrix[6],self.matrix[7],self.matrix[8],self.matrix[9],self.matrix[10],self.matrix[11],self.matrix[12],self.matrix[13],self.matrix[14],self.matrix[15]);
            mesh.applyMatrix(m);
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
        };
    }

};