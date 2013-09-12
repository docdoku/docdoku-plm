/*global isIpad,LevelGeometry,sceneManager*/
define(function() {

    var PartIterationVisualization = function(partIterationParams) {
        this.id = partIterationParams.partIterationId;
        this.files=partIterationParams.files;
        this.instances=[];
        this.radius=null;
        this.levelGeometries=[];
        this.setRadiusFromServer(partIterationParams.attributes);
        this.parseFiles();
    };

    PartIterationVisualization.prototype = {

        setRadiusFromServer:function(attributes){
            var radiusAttribute = _.find(attributes, function(attribute) {
                return attribute.name == 'radius';
            });
            this.radius = radiusAttribute ? radiusAttribute.value : 0;
        },

        hasGeometry: function() {
            return this.files.length > 0;
        },

        addInstance:function(instanceRaw){
            var self = this;
            if(!this.hasInstance(instanceRaw.id)){
                this.instances[instanceRaw.id] = new Instance(
                    instanceRaw.id,
                    self.id,
                    instanceRaw.tx,
                    instanceRaw.ty,
                    instanceRaw.tz,
                    instanceRaw.rx,
                    instanceRaw.ry,
                    instanceRaw.rz,
                    self.radius
                );
            }
            return this;
        },

        hasInstance:function(instanceId){
            return this.instances[instanceId] !== undefined;
        },

        getInstance:function(instanceId){
            return this.instances[instanceId];
        },

        hideInstance:function(instance){
            var instance = this.getInstance(instance.id);
            instance.clearMesh();
        },

        parseFiles:function(){
            var self = this;
            _.each(this.files, function(file) {
                self.addLevelGeometry( "/files/" + file.fullName, file.quality);
            });
        },

        addLevelGeometry:function(filename, quality){
            this.levelGeometries[parseInt(quality)] = new LevelGeometry(filename,quality,quality>0);
        },

        getLevelGeometry:function(rating){

            if(this.levelGeometries.length == 1){
                return this.getBestLevelGeometry();
            }

            // Do something with rating (ie get the size of levelGeometries and get right index), instead of returning the worst level geometry
            return this.levelGeometries[this.levelGeometries.length-1];

        },

        getBestLevelGeometry:function(){
            return this.levelGeometries[0];
        },


        showInstance:function(instance){
            this.getInstance(instance.id).addToScene(instance.rating,instance.fullQuality);
        },

        updateInstance:function(instance){
            this.getInstance(instance.id).updateMesh(instance.rating,instance.fullQuality);
        }

    };

   return PartIterationVisualization;

});