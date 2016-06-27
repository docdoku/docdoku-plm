/*global define,App,THREE,requestAnimationFrame,_*/
define([
    'backbone',
    'mustache',
    'text!templates/cad-file.html'
], function (Backbone, Mustache, template) {
    'use strict';

    var CADFileView = Backbone.View.extend({

        id:'cad-file-view',

        resize:function(){
            setTimeout(this.handleResize.bind(this),50);
        },

        render:function(nativeCADFile, fileName, uuid){

            var extension = fileName.substr(fileName.lastIndexOf('.') + 1).toLowerCase();
            var texturePath = fileName.substring(0, fileName.lastIndexOf('/'));
            var width, height;

            if(uuid){
                fileName += '/uuid/'+uuid;
                nativeCADFile += '/uuid/'+uuid;
            }

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath,
                nativeCADFile:nativeCADFile
            }));

            var $container = this.$('#cad-file');

            function calculateWith(){
                var max = $container.innerWidth() - 20;
                return max > 10 ? max : 10;
            }

            function calculateHeight(){
                var fit = width / 16 * 9;
                var max = window.innerHeight - 340;
                if(max <= 10){
                    max = 10;
                }
                return fit < max ? fit : max;
            }

            width = calculateWith();
            height = calculateHeight();

            var scene = new THREE.Scene();
            var camera = new THREE.PerspectiveCamera(45, width / height, App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
            var control;
            var renderer = new THREE.WebGLRenderer({alpha: true});

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
            $container.append(renderer.domElement);
            scene.add(camera);
            scene.updateMatrixWorld();
            control = new THREE.TrackballControls(camera, $container[0]);

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
                width = calculateWith();
                height = calculateHeight();
                camera.aspect = width / height;
                camera.updateProjectionMatrix();
                renderer.setSize(width, height);
                control.handleResize();
            }

            window.addEventListener('resize', handleResize, false);

            switch (extension) {

                case 'dae':

                    var colladaLoader = new THREE.ColladaLoader();

                    colladaLoader.load(fileName, function (collada) {

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

                    stlLoader.load(fileName, function (geometry) {
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

                    jsonLoader.load(fileName, function (geometry, materials) {
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

                    binaryLoader.load(fileName, function (geometry, materials) {
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

                    OBJLoader.load(fileName, texturePath + '/attachedfiles/', function (object) {
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

            this.handleResize = handleResize;

            animate();
            handleResize();

            return this;
        }
    });

    return CADFileView;
});
