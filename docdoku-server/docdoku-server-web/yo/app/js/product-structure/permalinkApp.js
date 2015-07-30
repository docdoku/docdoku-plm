/*global define,THREE,requestAnimationFrame,_*/

// Global Namespace for the application
var App = {
    SceneOptions: {
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 0.1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: 0, y: 50, z: 200},
        ambientLightColor: 0x101030,
        cameraLightColor: 0xbcbcbc
    }
};

define(function () {

    'use strict';

    function PermalinkApp(filename, width, height) {

        var container = document.getElementById('container');
        var $container = $(container);
        var scene = new THREE.Scene();
        var camera = new THREE.PerspectiveCamera(45, container.clientWidth / container.clientHeight, App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
        var control;
        var renderer = new THREE.WebGLRenderer({alpha: true});
        var texturePath = filename.substring(0, filename.lastIndexOf('/'));
        var extension = filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();

        function addLightsToCamera(camera) {
            var dirLight = new THREE.DirectionalLight(App.SceneOptions.cameraLightColor);
            dirLight.position.set(200, 200, 1000).normalize();
            camera.add(dirLight);
            camera.add(dirLight.target);
        }

        function initAmbientLight() {
            var ambient = new THREE.AmbientLight(App.SceneOptions.ambientLightColor);
            ambient.name = 'AmbientLight';
            scene.add(ambient);
            var hemiLight = new THREE.HemisphereLight(App.SceneOptions.ambientLightColor, App.SceneOptions.ambientLightColor, 0.4);
            hemiLight.position.set(0, 500, 0);
            scene.add(hemiLight);
        }

        camera.position.copy(App.SceneOptions.defaultCameraPosition);
        addLightsToCamera(camera);


        initAmbientLight();
        renderer.setSize(width, height);
        container.appendChild(renderer.domElement);
        scene.add(camera);
        scene.updateMatrixWorld();
        control = new THREE.TrackballControls(camera, container);

        function centerOn(mesh) {
            mesh.geometry.computeBoundingBox();
            var boundingBox = mesh.geometry.boundingBox;
            var cog = new THREE.Vector3().copy(boundingBox.center());
            var size = boundingBox.size();
            var radius = Math.max(size.x, size.y, size.z);
            var distance = radius ? radius * 2 : 1000;
            distance = (distance < App.SceneOptions.cameraNear) ? App.SceneOptions.cameraNear + 100 : distance;
            camera.position.set(cog.x + distance, cog.y, cog.z + distance);
            control.target.copy(cog);
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

        function getMeshGeometries(collada, geometries) {
            if (collada) {
                _.each(collada.children, function (child) {
                    if (child instanceof THREE.Mesh && child.geometry) {
                        geometries.push(child.geometry);
                    }
                    getMeshGeometries(child, geometries);
                });
            }
        }

        function onParseSuccess(object) {
            scene.add(object);
            centerOn(object.children[0]);
        }

        function handleResize() {
            camera.aspect = $container.innerWidth() / $container.innerHeight();
            camera.updateProjectionMatrix();
            renderer.setSize($container.innerWidth(), $container.innerHeight());
            control.handleResize();
        }

        window.addEventListener('resize', handleResize, false);

        switch (extension) {

            case 'dae':

                var colladaLoader = new THREE.ColladaLoader();

                colladaLoader.load(filename, function (collada) {

                    var geometries = [], combined = new THREE.Geometry();
                    getMeshGeometries(collada.scene, geometries);

                    // Merge all sub meshes into one
                    _.each(geometries, function (geometry) {
                        THREE.GeometryUtils.merge(combined, geometry);
                    });

                    combined.dynamic = false;
                    combined.mergeVertices();

                    combined.computeBoundingSphere();

                    onParseSuccess(new THREE.Mesh(combined), null);

                });

                break;

            case 'stl':
                var stlLoader = new THREE.STLLoader();

                stlLoader.load(filename, function (geometry) {
                    onParseSuccess(new THREE.Mesh(geometry), null);
                });

                break;

            case 'js':
            case 'json':

                var binaryLoader = new THREE.BinaryLoader();

                binaryLoader.load(filename, function (geometry, materials) {
                    var _material = new THREE.MeshPhongMaterial({color: materials[0].color, overdraw: true});
                    geometry.dynamic = false;
                    onParseSuccess(new THREE.Mesh(geometry, _material));
                }, texturePath);

                break;

            case 'obj' :

                var OBJLoader = new THREE.OBJLoader();

                OBJLoader.load(filename, texturePath + '/attachedfiles/', function (object) {
                    onParseSuccess(object);
                });

                break;

            default:
                break;

        }

        control.addEventListener('change', function () {
            render();
        });

        animate();
        handleResize();
    }

    return PermalinkApp;
});
