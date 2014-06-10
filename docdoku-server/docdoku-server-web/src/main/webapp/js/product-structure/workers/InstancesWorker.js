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

            // Send stats to main thread
            self.postMessage({fn: "stats", obj: stats});
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
    abort:function(directive){
        instances[directive.id].currentQuality=directive.quality;
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