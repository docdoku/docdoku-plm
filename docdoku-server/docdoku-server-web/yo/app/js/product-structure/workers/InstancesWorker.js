/*global _,self,THREE,InstancesSorter,DegradationLevelBalancer*/
importScripts(
    '../../../bower_components/underscore/underscore-min.js',
    '../../../bower_components/threejs/build/three.min.js',
    'InstancesSorter.js',
    'DegradationLevelBalancer.js'
);

// Index instances
var newData = false;
var instances = {};
var instancesCount = 0;
var WorkerManagedValues = {};
var debug = null;

function fixPrecision(v) {
	'use strict';
    v.x = parseFloat(v.x).toFixed(2);
    v.y = parseFloat(v.y).toFixed(2);
    v.z = parseFloat(v.z).toFixed(2);
}

var Context = {
    // Previous camera
    _camera: new THREE.Vector3(),
    _target: new THREE.Vector3(),
    // Latest camera
    camera: new THREE.Vector3(),
    target: new THREE.Vector3(),
    // Vector on camera -> target
    ct: new THREE.Vector3(),

    clear: function () {
	    'use strict';
        instances = {};
        instancesCount = 0;
        newData = true;
        if (debug) {
            console.log('[Worker] CLEARED');
        }

    },
    addInstance: function (instance) {
	    'use strict';
        if (typeof(instances[instance.id]) === 'undefined') {
            instancesCount++;
        }
        instances[instance.id] = instance;
        newData = true;
    },
    unCheckInstance: function (instanceId) {
	    'use strict';
	    if(instances[instanceId]){
		    instances[instanceId].checked = false;
	    }
        newData = true;
    },
    checkInstance: function (instanceId) {
	    'use strict';
	    if(instances[instanceId]){
		    instances[instanceId].checked = true;
	    }
        newData = true;
    },
    hasChanged: function (newContext) {
	    'use strict';
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
        if (newData || cameraMoved) {
            // Set newContext as current
            Context.camera.copy(newContext.camera);
            Context.target.copy(newContext.target);
            // Set the new direction of camera (looking a virtual point)
            Context.ct.subVectors(Context.target, Context.camera).normalize();
            newData = false;
            return true;
        }
        return false;
    },

    setQuality: function (instance) {
	    'use strict';
        instances[instance.id].qualityLoaded = instance.quality;
    },

    cameraDist: function (instance) {
	    'use strict';
        return new THREE.Vector3().subVectors(instance.cog, Context.camera).length();
    },
    cameraAngle: function (instance) {
	    'use strict';
        return new THREE.Vector3().subVectors(instance.cog, Context.camera).normalize().angleTo(Context.ct);
    },
    evalContext: function (context) {
	    'use strict';
        if (Context.hasChanged(context)) {

            if (debug) {
                console.log('[Worker] Start a cycle');
            }
            var start = Date.now();
            // Apply ratings, determine which instances must be displayed in this context
            var sorterResult = InstancesSorter.sort(instances);

            // Balancer will spread qualities and determine which instances will need to display first
            var dlbResult = DegradationLevelBalancer.apply(sorterResult);
            var directives = [];

            _.each(dlbResult.directives, function (directive) {
                var instance = instances[directive.instance.id];
                if (instance.qualityLoaded !== directive.quality) {
                    directives.push({
                        id: instance.id,
                        quality: directive.quality,
                        nowait: directive.quality === undefined && !instance.checked
                    });
                }
            });

            // Send directives
            self.postMessage({fn: 'directives', obj: directives});

            if (debug) {
                console.log('[Worker] Cycle duration : ' + (Date.now() - start) + ' ms');
            }
        } else {
            if (debug) {
                console.log('[Worker] Context didn\'t changed since last call');
            }
            self.postMessage({fn: 'directives', obj: []});
        }
    }
};

// Lookup table for parent messages
var ParentMessages = {
    context: function (context) {
	    'use strict';
        Context.evalContext(context);
    },
    unCheck: function (nodeId) {
	    'use strict';
        Context.unCheckInstance(nodeId);
    },
    check: function (nodeId) {
	    'use strict';
        Context.checkInstance(nodeId);
    },
    clear: function () {
	    'use strict';
        Context.clear();
    },
    setQuality: function (instance) {
	    'use strict';
        Context.setQuality(instance);
    },
    addInstance: function (instance) {
	    'use strict';
        Context.addInstance(instance);
    }
};

self.addEventListener('message', function (message) {
	'use strict';
    if (typeof  ParentMessages[message.data.fn] === 'function') {
        ParentMessages[message.data.fn](message.data.obj);
    } else {
        if (debug) {
            console.log('[Worker] Unrecognized command  : ');
            console.log(message.data);
        }
    }
}, false);