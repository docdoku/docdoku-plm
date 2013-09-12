importScripts(
    "/js/lib/underscore-1.4.2.min.js",
    "/js/lib/visualization/three.min.js"
);

var center = new THREE.Vector3();
var instanceWorker = null;
var data;
var frustum = new THREE.Frustum();
var cameraPosition ={x:0,y:0,z:0}, oldCameraPosition={x:0,y:0,z:0};
var switchRating = 0.3;
var maxInstancesToSend = 500;
var debug = true;

function InstanceWorker(){
    this.init();
};

InstanceWorker.prototype = {

    init:function(){
        this.indexedInstances={};
        this.instances=[];
        this.instancesOnScene=[];
        this.instancesToUpdate=[];
    },

    insert:function(instances){
        var that = this ;
        _(instances).each(function(instance){
             that.addInstance(instance);
        });
        return this;
    },

    addInstance:function(instance){
        this.indexedInstances[instance.id] = instance;
        instance.rating = getRating(instance);
        instance.oldRating = instance.rating;
        instance.fullQuality = instance.rating > switchRating;
        this.needsSend = true;
        return this;
    },

    remove:function(instancesId){
        var that = this;
        _(instancesId).each(function(instanceId){
            that.removeInstance(instanceId);
        });
        this.needsSend = true;
        return this;
    },

    removeInstance:function(instanceId){
        delete this.indexedInstances[instanceId];
    },

    sendInstances:function(){

        var instances = _.values(this.indexedInstances);

        this.recomputeRatings(instances);
        this.sort(instances);

        if(this.needsSend){

            var shouldBeOnScene = instances.slice(0,maxInstancesToSend);
            var shouldNotBeOnScene = instances.slice(maxInstancesToSend,instances.length);

            var instancesToShow = _.difference(shouldBeOnScene,this.instancesOnScene);
            var instancesToHide = _.intersection(shouldNotBeOnScene,this.instancesOnScene);

            var instancesToUpdate = _.difference(shouldBeOnScene,instancesToShow).filter(function(instance){
                if(instance.rating > switchRating && instance.oldRating < switchRating){
                    return true
                }
                if(instance.rating < switchRating && instance.oldRating > switchRating){
                    return true
                }
                return false;
            });



            if(instancesToHide.length){
                self.postMessage(JSON.stringify({
                    fn:"hide",
                    instances:instancesToHide
                }));
            }

            if(instancesToShow.length){
                self.postMessage(JSON.stringify({
                    fn:"show",
                    instances:instancesToShow
                }));
            }

            if(instancesToUpdate.length){
                self.postMessage(JSON.stringify({
                    fn:"update",
                    instances:instancesToUpdate
                }));
            }

            this.instancesOnScene = shouldBeOnScene;
            this.needsSend = false;
            this.setOldCameraPosition();
        }

        return this;
    },

    setFrustum:function(pFrustum){

        for(var i = 0; i< 6 ; i++){
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
        oldCameraPosition = {x:cameraPosition.x, y:cameraPosition.y, z:cameraPosition.z};
        return this;
    },

    recomputeRatings:function(instances){
        if(cameraHasChanged()){
            _(instances).each(function(instance){
                instance.oldRating = instance.rating;
                instance.rating = getRating(instance);
                instance.fullQuality = instance.rating > switchRating;
            });
        }
    },

    sort:function(instances){
        // Don't sort if the array didn't reach max size
        if(instances.length > maxInstancesToSend){
            instances.sort(function(a,b){
                return a.rating < b.rating ? 1  : a.rating > b.rating ? -1 : 0;
            });
            this.needsSend = true;
        }
    },

    debug:function(){
        self.postMessage(JSON.stringify({
            fn:"debug",
            instances: _.values(this.indexedInstances),
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
            instanceWorker.insert(data.instances).sendInstances();
            break ;

        case 'instances' :
            instanceWorker.sendInstances();
            break ;

        case 'remove' :
            instanceWorker.remove(data.instancesId).sendInstances();
            break ;

        case 'update' :
            instanceWorker.setFrustum(data.frustum).setCameraPosition(data.cameraPosition).sendInstances();
            break ;

        case 'init' :
            instanceWorker.setFrustum(data.frustum).setCameraPosition(data.cameraPosition).setOldCameraPosition();
            break ;

        case 'debug' :
            instanceWorker.debug();
            break ;

        default :
            break ;
    }

}, false);
