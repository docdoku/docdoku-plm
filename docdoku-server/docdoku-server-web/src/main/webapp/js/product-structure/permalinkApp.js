/*global requestAnimationFrame*/
'use strict';
var sceneManager;

// Global Namespace for the application
var App = {
    debug: false,
    instancesManager : null,
    sceneManager : null,

    setDebug:function(state){
        App.debug = state;
        if(state){
            $("body").addClass("debug");
        }else{
            $("body").removeClass("debug");
        }
    },

    SceneOptions: {
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 10,
        cameraFar: 10000,
        defaultCameraPosition: {x: 0, y: 50, z: 200}
    }
};

define(["dmu/SceneManager","dmu/LoaderManager"],
function(SceneManager,LoaderManager){

    function PermalinkApp(fileName, width, height){
        var loaderManager = new LoaderManager({progressBar: true});
        var container = $("#container");
        var scene = new THREE.Scene();
        var camera = new THREE.PerspectiveCamera(45, container.width() / container.height(), App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
        var control;
        var renderer = new THREE.WebGLRenderer({alpha: true});
        var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));

        function centerOn (mesh) {
            var boundingBox = mesh.geometry.boundingBox;
            var cog = new THREE.Vector3().copy(boundingBox.center()).applyMatrix4(mesh.matrix);
            var size = boundingBox.size();
            var radius = Math.max(size.x, size.y, size.z);
            var dir = new THREE.Vector3().copy(cog).sub(camera.position).normalize();
            var distance = radius ? radius * 2 : 1000;
            distance = distance < App.SceneOptions.cameraNear ? App.SceneOptions.cameraNear + 100 : distance;
            var endCamPos = new THREE.Vector3().copy(cog).sub(dir.multiplyScalar(distance));
            camera.position.copy(endCamPos);
        }

        function render() {
            scene.updateMatrixWorld();
            renderer.render(scene, camera);
        }

        function animate() {
            requestAnimationFrame(animate, null);
            control.update();
            render();
        }

        function addLightsToCamera(camera) {
            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            camera.add(dirLight);
            camera.add(dirLight.target);
        }


        camera.position.copy(App.SceneOptions.defaultCameraPosition);
        addLightsToCamera(camera);

        renderer.setSize(width, height);
        container.append(renderer.domElement);
        scene.add(camera);
        scene.updateMatrixWorld();
        control = new THREE.TrackballControls(camera, container[0]);

        loaderManager.parseFile(fileName,texturePath,{
            success: function(geometry, material){
                THREE.GeometryUtils.center(geometry);
                var mesh = new THREE.Mesh(geometry, material);
                scene.add(mesh);
                centerOn(mesh);
            }
        });

        control.addEventListener('change',function(){
            render();
        });

        animate();



    }
    return PermalinkApp;
});