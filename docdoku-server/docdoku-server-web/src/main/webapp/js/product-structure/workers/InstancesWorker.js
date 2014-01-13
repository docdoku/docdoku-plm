/*global self*/
importScripts(
    "/js/lib/underscore-1.4.2.min.js",
    "/js/lib/visualization/three.min.js"
);

// Index meshes
var meshes ={};
var sortedMeshes ;
var nMeshes=0;
// Previous camera
var _camera=new THREE.Vector3();
var _target=new THREE.Vector3();
// Latest camera
var camera=new THREE.Vector3();
var target=new THREE.Vector3();
// Vector on camera -> target
var ct=new THREE.Vector3();
var maxQualities=0;
var bestRating = 0;

var _meshCount = 0, _oldMeshCount = 0;

var Context = {

    clear:function(){
        _meshCount = _oldMeshCount = 0;
        meshes ={};
    },

    addMesh:function(mesh){
        if(!meshes[mesh.uuid]){
            meshes[mesh.uuid] = mesh;
            mesh.currentQuality = null;
            maxQualities = Math.max(maxQualities,mesh.qualities.length);
            nMeshes++;
            Context.checkMesh(mesh.uuid);
        }
    },

    unCheckMesh:function(meshId){
        meshes[meshId].checked = false;
        _meshCount--;
    },
    checkMesh:function(meshId){
        meshes[meshId].checked = true;
        _meshCount++;
    },

    setFromMessage:function(context){
        // Backup previous state

        _camera.x= camera.x;
        _camera.y= camera.y;
        _camera.z= camera.z;

        _target.x= target.x;
        _target.y= target.y;
        _target.z= target.z;

        // New scene context
        var c = context.camera, t = context.target;

        camera.x = c.x;
        camera.y = c.y;
        camera.z = c.z;

        target.x = t.x;
        target.y = t.y;
        target.z = t.z;

        ct.x = camera.x - target.x;
        ct.y = camera.y - target.y;
        ct.z  =camera.z - target.z;

    },

    changed:function(){
        return Context.cameraChanged();
    },

    cameraChanged:function() {
        if(_camera.x!=camera.x) return true;
        if(_camera.y!=camera.y) return true;
        if(_camera.z!=camera.z) return true;
        if(_target.x!=target.x) return true;
        if(_target.y!=target.y) return true;
        if(_target.z!=target.z) return true;

        if(_meshCount != _oldMeshCount) return true;

        return false;
    },

    cameraDist:function(mesh){
        var mc = new THREE.Vector3(mesh.cog.x - camera.x, mesh.cog.y - camera.y, mesh.cog.z - camera.z);
        return mc.length();
    },

    cameraAngle:function(mesh){
        var mc = new THREE.Vector3(camera.x-mesh.cog.x, camera.y-mesh.cog.y, camera.z-mesh.cog.z);
        return mc.angleTo(ct);
    },

    hasData:function(){
        return nMeshes>0;
    }

};

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


    });

}

function SortMeshes(){
    sortedMeshes = _.values(meshes);
    sortedMeshes.sort( function(a,b){
        return a.globalRating < b.globalRating ? 1 : a.globalRating > b.globalRating ? -1 : 0;
    });

    if(sortedMeshes.length){
        bestRating = sortedMeshes[0].globalRating;
    }
    else{
        bestRating = 1;
    }

}

function SendMeshes(){

    // Take out 500 first
    var bestRatingsMeshes = sortedMeshes.splice(0,500);

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

// Tells the scene to load a mesh in given quality
function sendMesh(mesh,quality){
    // If quality has change, tell the main thread to change it
    if(mesh.currentQuality != quality){
        mesh.currentQuality = quality;
        self.postMessage({uuid:mesh.uuid,quality:quality,overall:mesh.globalRating/bestRating});
    }
}

function splitArrayIntoArrays(a, n) {
    var len = a.length,out = [], i = 0;
    while (i < len) {
        var size = Math.ceil((len - i) / n--);
        out.push(a.slice(i, i += size));
    }
    return out;
}