/*global LevelGeometry,sceneManager,Instance*/
define(function() {

    var PartIterationVisualization = function(partIterationParams) {
        this.id = partIterationParams.partIterationId;
        this.files=partIterationParams.files;
        this.attributes=partIterationParams.attributes;
        this.instances=[];
        this.radius=null;
        this.levelGeometries=[];
        this.findRadius();
        this.parseFiles();
    };

    PartIterationVisualization.prototype = {

        findRadius:function(){
            // check in files if radius available (new way)
            this.radius = this.findRadiusInFiles(this.files);
            // check in attributes if not found (old way)
            if(!this.radius){
                this.radius = this.findRadiusInAttributes(this.attributes);
            }
        },

        findRadiusInFiles:function(files){
            if(files.length){
               return files[0].radius;
            }
            return 0;
        },

        findRadiusInAttributes:function(attributes){
            var radiusAttribute = _.find(attributes, function(attribute) {
                return attribute.name == 'radius';
            }) || {};
            return radiusAttribute.value || 0;
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

        hideInstance:function(instanceRaw){
            var instance = this.getInstance(instanceRaw.id);
            instance.clearMesh();
        },

        parseFiles:function(){
            var self = this;
            _.each(this.files, function(file) {
                self.addLevelGeometry( "/files/" + file.fullName, file.quality);
            });
        },

        addLevelGeometry:function(filename, quality){
            this.levelGeometries[parseInt(quality,10)] = new LevelGeometry(filename,quality,quality>0);
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