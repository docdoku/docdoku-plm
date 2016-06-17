/*global define,App,THREE,requestAnimationFrame,_,$*/
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
            var dirLight1 = new THREE.DirectionalLight(App.SceneOptions.cameraLight1Color);
            dirLight1.position.set(200, 200, 1000).normalize();
            dirLight1.name = 'CameraLight1';
            camera.add(dirLight1);
            camera.add(dirLight1.target);

            var dirLight2 = new THREE.DirectionalLight( App.SceneOptions.cameraLight2Color, 1 );
            dirLight2.color.setHSL( 0.1, 1, 0.95 );
            dirLight2.position.set( -1, 1.75, 1 );
            dirLight2.position.multiplyScalar( 50 );
            dirLight2.name='CameraLight2';
            camera.add( dirLight2 );

            dirLight2.castShadow = true;

            dirLight2.shadowMapWidth = 2048;
            dirLight2.shadowMapHeight = 2048;

            var d = 50;

            dirLight2.shadowCameraLeft = -d;
            dirLight2.shadowCameraRight = d;
            dirLight2.shadowCameraTop = d;
            dirLight2.shadowCameraBottom = -d;

            dirLight2.shadowCameraFar = 3500;
            dirLight2.shadowBias = -0.0001;
            dirLight2.shadowDarkness = 0.35;

            var hemiLight = new THREE.HemisphereLight( App.SceneOptions.ambientLightColor, App.SceneOptions.ambientLightColor, 0.6 );
            hemiLight.color.setHSL( 0.6, 1, 0.6 );
            hemiLight.groundColor.setHSL( 0.095, 1, 0.75 );
            hemiLight.position.set( 0, 0, 500 );
            hemiLight.name='AmbientLight';
            camera.add( hemiLight );

        }

        camera.position.copy(App.SceneOptions.defaultCameraPosition);
        addLightsToCamera(camera);

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
            requestAnimationFrame(animate);
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

        var defaultMaterial = new THREE.MeshLambertMaterial({color:new THREE.Color(0x62697B)});

        function setShadows(object){
            object.traverse( function ( o ) {
                if ( o instanceof THREE.Mesh) {
                    o.castShadow = true;
                    o.receiveShadow = true;
                }
            });
        }

        function updateMaterial(object){
            object.traverse( function ( o ) {
                if ( o instanceof THREE.Mesh && !o.material.name) {
                    o.material = defaultMaterial;
                }
            });
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
                    var object = new THREE.Object3D();
                    object.add(new THREE.Mesh(combined));
                    setShadows(object);
                    updateMaterial(object);
                    onParseSuccess(object);

                });

                break;

            case 'stl':
                var stlLoader = new THREE.STLLoader();

                stlLoader.load(filename, function (geometry) {
                    var object = new THREE.Object3D();
                    object.add(new THREE.Mesh(geometry));
                    setShadows(object);
                    updateMaterial(object);
                    onParseSuccess(object);
                });

                break;

            // Used for json files only (no referenced buffers)
            case 'json':
                var jsonLoader = new THREE.JSONLoader();

                jsonLoader.load(filename, function (geometry, materials) {
                    geometry.dynamic = false;
                    var object = new THREE.Object3D();
                    object.add(new THREE.Mesh(geometry,new THREE.MeshFaceMaterial(materials)));
                    setShadows(object);
                    onParseSuccess(object);
                }, texturePath+'/attachedfiles/');

                break;

            // Used for binary json files only (referenced buffers - bin file)
            case 'js':
                var binaryLoader = new THREE.BinaryLoader();

                binaryLoader.load(filename, function (geometry, materials) {
                    var _material = new THREE.MeshPhongMaterial({color: materials[0].color, overdraw: true});
                    geometry.dynamic = false;
                    var object = new THREE.Object3D();
                    object.add(new THREE.Mesh(geometry,_material));
                    setShadows(object);
                    onParseSuccess(object);
                }, texturePath);

                break;

            case 'obj' :

                var OBJLoader = new THREE.OBJLoader();

                OBJLoader.load(filename, texturePath + '/attachedfiles/', function (object) {
                    setShadows(object);
                    updateMaterial(object);
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
