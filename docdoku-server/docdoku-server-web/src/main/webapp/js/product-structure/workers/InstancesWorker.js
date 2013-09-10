importScripts(
    "/js/lib/underscore-1.4.2.min.js",
    "/js/lib/visualization/three.min.js"
);

var center = new THREE.Vector3();
var instanceWorker = null;
var data;
var frustum = new THREE.Frustum();
var cameraPosition ={x:0,y:0,z:0}, oldCameraPosition={x:0,y:0,z:0};
var i;

function InstanceWorker(){
    this.init();
};

InstanceWorker.prototype = {

    init:function(){
        this.maxInstancesToSend = 750;
        this.indexedInstances={};
        this.instances=[];
        this.instancesOnScene=[];
        this.instancesToUpdate=[];
    },

    insert:function(instance){
        this.addInstance(instance);
        instance.rating = getRating(instance);
        instance.fullQuality = instance.rating > 0.18;
        this.needsSend = true;
        return this;
    },

    addInstance:function(instance){
        this.indexedInstances[instance.id] = instance;
    },

    remove:function(instanceId){
        delete this.indexedInstances[instanceId];
        this.needsSend = true;
        return this;
    },

    sendInstances:function(){

        this.instances = _.values(this.indexedInstances);

        this.recomputeRatings();
        this.sort();

        if(this.needsSend){

            var instancesToAdd = _.difference(this.instances.slice(0,this.maxInstancesToSend),this.instancesOnScene);
            var instancesToRemove = _.intersection(this.instancesOnScene,this.instances.slice(this.maxInstancesToSend,this.instances.length));

            if(instancesToRemove.length){
                self.postMessage(JSON.stringify({
                    fn:"remove",
                    instances:instancesToRemove
                }));
            }

            if(instancesToAdd.length){
                self.postMessage(JSON.stringify({
                    fn:"show",
                    instances:instancesToAdd
                }));
            }

            if(this.instancesToUpdate.length){
                self.postMessage(JSON.stringify({
                    fn:"update",
                    instances:this.instancesToUpdate
                }));
                this.instancesToUpdate=[];
            }

            this.instancesOnScene = this.instances.slice(0,this.maxInstancesToSend);

            this.needsSend = false;
            this.setOldCameraPosition();
        }

        return this;
    },

    setFrustum:function(pFrustum){

        for(i = 0; i< 6 ; i++){
            frustum.planes[i].normal.x = pFrustum.planes[i].normal.x;
            frustum.planes[i].normal.y = pFrustum.planes[i].normal.y;
            frustum.planes[i].normal.z = pFrustum.planes[i].normal.z;
            frustum.planes[i].constant = pFrustum.planes[i].constant;
        }

        return this;
    },

    setCameraPosition:function(pCameraPosition){
        cameraPosition = pCameraPosition;
        return this;
    },

    setOldCameraPosition:function(){
        oldCameraPosition = {x:cameraPosition.x , y: cameraPosition.y,  z:cameraPosition.z };
        return this;
    },

    sort:function(){
        // Don't sort if the array didn't reach max size
        if(this.instances.length > this.maxInstancesToSend){
            this.instances.sort(function(a,b){
                return a.rating < b.rating ? 1  : a.rating > b.rating ? -1 : 0;
            });
            this.needsSend = true;
        }
    },

    recomputeRatings:function(){
        // Don't recompute if camera didn't changed
        if(cameraHasChanged()){
            this.instancesToUpdate=[];
            var that=this;
            _(this.instances).each(function(instance){

                var rating = instance.rating;
                instance.rating = getRating(instance);
                instance.fullQuality = instance.rating > 0.18;

                if(rating != undefined && rating>0.18 && instance.rating < 0.18 || rating<0.18 && instance.rating > 0.18){
                    that.instancesToUpdate.push(instance);
                }

            });
            this.needsSend = true;
        }
    },

    debug:function(){
        self.postMessage(JSON.stringify({
            fn:"debug",
            instances:this.instances,
            instancesToUpdate:this.instancesToUpdate,
            instancesOnScene:this.instancesOnScene,
            cameraPosition:cameraPosition
        }));
    }

};

function getRating (instance) {
    return isInFrustum(instance) ? instance.radius / getDistance(instance.position) : 0;
};

function isInFrustum (instance){
    center.x= instance.position.x;
    center.y= instance.position.y;
    center.z= instance.position.z;
    for (i = 0; i < 6; i ++ ) {
        if ( frustum.planes[ i ].distanceToPoint(center) < -instance.radius ) {
            return false;
        }
    }
    return true;
};

function getDistance(instancePosition) {
    return Math.sqrt(Math.pow(cameraPosition.x - instancePosition.x, 2)
        + Math.pow(cameraPosition.y - instancePosition.y, 2)
        + Math.pow(cameraPosition.z - instancePosition.z, 2));
};

function cameraHasChanged(){
    if(oldCameraPosition.x != cameraPosition.x) return true;
    if(oldCameraPosition.y != cameraPosition.y) return true;
    if(oldCameraPosition.z != cameraPosition.z) return true;
    return false;
}

instanceWorker = new InstanceWorker();

self.addEventListener('message', function(message) {

    data = JSON.parse(message.data);

    switch(data.fn){
        case 'insert' :
            instanceWorker.insert(data.instance);
            break ;

        case 'instances' :
            instanceWorker.sendInstances();
            break ;

        case 'remove' :
            instanceWorker.remove(data.instanceId);
            break ;

        case 'update' :
            instanceWorker.setFrustum(data.frustum).setCameraPosition(data.cameraPosition).sendInstances();
            break ;

        case 'init' :
            instanceWorker.setFrustum(data.frustum).setCameraPosition(data.cameraPosition);
            break ;

        case 'debug' :
            instanceWorker.debug();
            break ;

        default :
            break ;
    }

}, false);
