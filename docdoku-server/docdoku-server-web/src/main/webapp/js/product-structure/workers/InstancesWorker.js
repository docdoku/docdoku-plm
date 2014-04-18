/*global self*/
importScripts(
    "/js/lib/underscore-1.4.2.min.js",
    "/js/lib/visualization/three.min.js",
    "InstancesSorter.js",
    "DegradationLevelBalancer.js"
);

// Index instances
var newData = false;
var instances = {};
var totalChecked = 0;
var nChecked = 0;
var messagesCount = 0;
var instancesCount = 0;
var WorkerManagedValues = {};
var debug = null;

function fixPrecision(v) {
    v.x = parseFloat(v.x).toFixed(2);
    v.y = parseFloat(v.y).toFixed(2);
    v.z = parseFloat(v.z).toFixed(2);
}
// Each cycle has these stats
var stats = {
    errors: 0,
    cycles: 0,
    onScene: 0
};
var Context = {
    // Previous camera
    _camera:new THREE.Vector3(),
    _target: new THREE.Vector3(),
    // Latest camera
    camera: new THREE.Vector3(),
    target: new THREE.Vector3(),
    // Vector on camera -> target
    ct: new THREE.Vector3(),

    clear:function(){
        nChecked = 0;
        instances = {};
        messagesCount = 0;
        instancesCount = 0;
    },
    addInstance: function (instance) {
        if (instances[instance.id] == undefined) {
            instancesCount++;
            totalChecked += instance.checked ? 1 : 0;
        }
        instances[instance.id] = instance;
        newData = true;
    },
    unCheckInstance: function (instanceId) {
        if (instances[instanceId].checked) {
            totalChecked--;
        }
        instances[instanceId].checked = false;
        nChecked++;

    },
    checkInstance: function (instanceId) {
        if (!instances[instanceId].checked) {
            totalChecked++;
        }
        instances[instanceId].checked = true;
        nChecked++;
    },
    hasChanged: function (newContext) {
        debug = newContext.debug;
        WorkerManagedValues = newContext.WorkerManagedValues;
        //Lower precision, sometimes camera is moving by 1E-6 and triggers calculations
        //Avoid this effect by fixing precision
        fixPrecision(newContext.camera);
        fixPrecision(newContext.target);
        // Copy current context in previous context
        Context._camera.copy(Context.camera);
        Context._target.copy(Context.target);
        // Detect camera move
        var cameraMoved = !Context.camera.equals(newContext.camera) || !Context.target.equals(newContext.target);
        if (cameraMoved) {
            // Set newContext as current
            Context.camera.copy(newContext.camera);
            Context.target.copy(newContext.target);

            // Set the new direction of camera (looking a virtual point)
            Context.ct.subVectors(Context.target, Context.camera).normalize();
            return true;
        }
        if (nChecked > 0) {
            if(debug){console.log("[Worker] Check changed");}
            nChecked = 0;
            return true;
        }
        if(newData){
            newData = false;
            return true;
        }
        if(_target.x!=target.x){
            return true;
        }
        if(_target.y!=target.y){
            return true;
        }
        if(_target.z!=target.z){
            return true;
        }

        if(_meshCount != _oldMeshCount){
            return true;
        }

        return false;
    },

    cameraDist: function (instance) {
        return new THREE.Vector3().subVectors(instance.cog, Context.camera).length();
    },
    cameraAngle: function (instance) {
        return new THREE.Vector3().subVectors(instance.cog, Context.camera).normalize().angleTo(Context.ct);
    },
    evalContext:function(context){
        if (Context.hasChanged(context)) {
            if(debug){console.log("[Worker] Start a cycle");}
            var start = Date.now();
            // Apply ratings, determine which instances must be displayed in this context
            var sorterResult = InstancesSorter.sort(instances);
            // Balancer will spread qualities and determine which instances will need to display first
            var dlbResult = DegradationLevelBalancer.apply(sorterResult);
            var directives=[];
            _.each(dlbResult.directives, function (directive) {
                var instance = instances[directive.instance.id];
                if (instance.currentQuality != directive.quality) {
                    instance.currentQuality=directive.quality;
                    directives.push({
                        id:instance.id,
                        quality:directive.quality
                    });
                }
            });

            stats.cycles++;
            //stats.faces = dlbResult.faces;
            stats.totalChecked = totalChecked;
            //stats.onScene = dlbResult.onScene;

};


function ComputeMeshesRatings(){

    var totalRating = 0;

    _(meshes).each(function(mesh){

        if(mesh.checked){

            var dist = Context.cameraDist(mesh);
            var angle = Context.cameraAngle(mesh);
            var radius = mesh.radius;

            mesh.dist=dist;
            mesh.angle=angle;

            var maxAngle = Math.PI/4;

            var distanceRating = radius/dist;
            var angleRating =  angle > maxAngle ?  0 : maxAngle/angle ;

            mesh.globalRating = distanceRating * angleRating;

            // console.log("------- dist / angle / rating --- " + dist  + "   " + angle + "   " + distanceRating);
        }else{
            mesh.globalRating = 0;
        }
    }
};

}

function SendMeshes(){

    // Take out 500 first
    var bestRatingsMeshes = sortedMeshes.splice(0,maxMeshesOnScene);

    bestRatingsMeshes.sort(function(a,b){
        return a.dist>b.dist?1:a.dist< b.dist?-1:0;
    });

    // Need to be sure to remove all other meshes
    sendMeshesWithQuality(sortedMeshes,null);

    // split into arrays - spread qualities
    var arrays = splitArrayIntoArrays(bestRatingsMeshes,maxQualities);

    // Load the bestRatingMeshes
    for(var i = 0; i < maxQualities; i++){
        sendMeshesWithQuality(arrays[i],i);
    }

}

// Tells the scene to load a mesh in given quality
function sendMesh(mesh,quality){
    // If quality has change, tell the main thread to change it
    if(mesh.currentQuality != quality){
        mesh.currentQuality = quality;
        self.postMessage({uuid:mesh.uuid,quality:quality,overall:mesh.globalRating/bestRating});
    }
}

function sendMeshesWithQuality(_meshes,quality){
    _(_meshes).each(function(mesh){
        if(mesh.checked){
            if(quality === null){
                sendMesh(mesh,null);
            }
            else if(mesh.qualities[quality]){
                sendMesh(mesh,mesh.qualities[quality]);
            }else{
                sendMesh(mesh,null);
            }
        }else{
            sendMesh(mesh,null);
        }
    });
}


function splitArrayIntoArrays(a, n) {
    var len = a.length,out = [], i = 0;
    while (i < len) {
        var size = Math.ceil((len - i) / n--);
        out.push(a.slice(i, i += size));
    }
    return out;
}


self.addEventListener('message', function(message) {

    if(message.data.context){
        Context.setFromMessage(message.data.context);
        if(Context.changed()){
            ComputeMeshesRatings();
            SortMeshes();
            SendMeshes();
        }
        _oldMeshCount=_meshCount;
    }
    else if(message.data.mesh){
        Context.addMesh(message.data.mesh);
    }
    else if(message.data.unCheck){
        Context.unCheckMesh(message.data.unCheck);
    }
    else if(message.data.check){
        Context.checkMesh(message.data.check);
    }
    else if(message.data.clear){
        Context.clear();
    }

}, false);
