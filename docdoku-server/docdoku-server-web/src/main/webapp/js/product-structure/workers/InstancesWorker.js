/*global self,InstancesSorter,DegradationLevelBalancer*/
importScripts(
    "/js/lib/underscore-1.4.2.min.js",
    "/js/lib/visualization/three.min.js",
    "InstancesSorter.js",
    "DegradationLevelBalancer.js"
);

// Index instances
var newData = false;
var instances = {};
var instancesCount = 0;
var WorkerManagedValues = {};
var debug = null;

function fixPrecision(v) {
    v.x = parseFloat(v.x).toFixed(2);
    v.y = parseFloat(v.y).toFixed(2);
    v.z = parseFloat(v.z).toFixed(2);
}

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
        instances = {};
        instancesCount = 0;
    },
    addInstance: function (instance) {
        if (instances[instance.id] == undefined) {
            instancesCount++;
        }
        instances[instance.id] = instance;
        newData = true;
    },
    unCheckInstance: function (instanceId) {
        instances[instanceId].checked = false;
        newData = true;
    },
    checkInstance: function (instanceId) {
        instances[instanceId].checked = true;
        newData = true;
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
        if(newData){
            newData = false;
            return true;
        }
        return false;
    },

    setQuality:function(instance){
        instances[instance.id].qualityLoaded = instance.quality;
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
                if (instance.qualityLoaded != directive.quality) {
                    directives.push({
                        id:instance.id,
                        quality:directive.quality
                    });
                }
            });

            // Send directives
            self.postMessage({fn:"directives",obj:directives});

            if(debug){console.log("[Worker] Cycle duration : " + (Date.now() - start) + " ms");}
        } else {
            if(debug){console.log("[Worker] Context didn't changed since last call");}
            self.postMessage({fn:"directives",obj:[]});
        }
    }
};

// Lookup table for parent messages
var ParentMessages = {
    context: function (context) {
        Context.evalContext(context);
    },
    unCheck: function (nodeId) {
        Context.unCheckInstance(nodeId);
    },
    check: function (nodeId) {
        Context.checkInstance(nodeId);
    },
    clear: function () {
        Context.clear();
    },
    setQuality: function (instance) {
        Context.setQuality(instance);
    },
    addInstance: function (instance) {
        Context.addInstance(instance);
    }
};

self.addEventListener('message', function (message) {
    if (typeof  ParentMessages[message.data.fn] == "function") {
        ParentMessages[message.data.fn](message.data.obj);
    } else {
        if(debug){
            console.log("[Worker] Unrecognized command  : ");
            console.log(message.data);
        }
    }
}, false);