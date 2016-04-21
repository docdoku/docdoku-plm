/*global _,define,App,THREE,TWEEN,Stats,requestAnimationFrame,Element*/
define([
    'backbone',
    'views/marker_create_modal_view',
    'views/blocker_view',
    'dmu/LayerManager',
    'dmu/MeasureTool',
    'common-objects/utils/date'
], function (Backbone, MarkerCreateModalView, BlockerView, LayerManager, MeasureTool, date) {
    'use strict';
    var SceneManager = function (pOptions) {
        var _this = this;

        var browserSupportPointerLock = 'pointerLockElement' in document ||
            'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;
        var options = pOptions || {};
        _.extend(this, options);
        var isMoving = false;
        var currentLayer = null;
        var explosionValue = 0;
        var projector = new THREE.Projector();
        var controlsObject = null;                                                                                      // Switching controls means different camera management
        var clock = new THREE.Clock();
        var needsRedraw = false;
        var objects = {};
        var selectionBox = null;
        var objectMarkedForSelection = null;
        var controlChanged = false;
        var editedObjectsColoured = false;
        var transformControls = null;

        var measureTool = null;
        var measures = [];
        var measuresPoints = [];
        var measureTexts = [];

        var materialEditedObject = new THREE.MeshPhongMaterial({transparent: false, color: new THREE.Color(0x08B000)});

        this.stateControl = null;
        this.STATECONTROL = {PLC: 0, TBC: 1, ORB: 2};
        this.scene = new THREE.Scene();
        this.renderer = null;
        this.cameraObject = null;                                                                                       // Represent the eye
        this.layerManager = null;
        this.editedObjects = [];
        this.editedObjectsLeft = [];

        // Stat
        this.switches = 0;
        this.adds = 0;
        this.onScene = 0;

        function render() {
            _this.scene.updateMatrixWorld();
            _this.renderer.render(_this.scene, _this.cameraObject);
        }

        function initDOM() {
            _this.$container = App.$SceneContainer.find('#container');
            _this.$container[0].setAttribute('tabindex', '-1');
            _this.$blocker = new BlockerView().render().$el;
            _this.$container.append(_this.$blocker);
        }

        function initRenderer() {
            _this.renderer = new THREE.WebGLRenderer({preserveDrawingBuffer: true, alpha: true});
            _this.renderer.setSize(_this.$container.width(), _this.$container.height());
            _this.$container.append(_this.renderer.domElement);
        }

        function initLayerManager() {
            if (!_.isUndefined(App.config.productId)) {
                _this.layerManager = new LayerManager();
                _this.layerManager.rescaleMarkers();
                _this.layerManager.renderList();
            }
        }

        function initMeasureTool() {
            measureTool = new MeasureTool({
                onFirstPoint: function () {
                    _this.scene.add(measureTool.line);
                },
                onSecondPoint: function () {
                    _this.scene.remove(measureTool.line);
                },
                onCancelled: function () {
                    _this.scene.remove(measureTool.line);
                }
            });
        }

        function initAxes() {
            var axes = new THREE.AxisHelper(100);
            axes.position.set(0, 0, 0);
            _this.scene.add(axes);
            var arrow = new THREE.ArrowHelper(new THREE.Vector3(0, 1, 0), new THREE.Vector3(0, 0, 0), 100);
            arrow.position.set(200, 0, 400);
            _this.scene.add(arrow);
        }

        function initStats() {
            _this.stats = new Stats();
            var statsElement = _this.stats.domElement;
            App.$SceneContainer.append(statsElement);
            statsElement.id = 'statsWin';
            statsElement.className = 'statsWinMaximized';
            var statsArrow = document.createElement('i');
            statsArrow.id = 'statsArrow';
            statsArrow.className = 'fa fa-chevron-down';
            statsElement.insertBefore(statsArrow, statsElement.firstChild);
            statsArrow.onclick = function () {
                var statsClass = _this.stats.domElement.classList;
                statsClass.toggle('statsWinMinimized');
                statsClass.toggle('statsWinMaximized');
            };
        }

        function initGrid() {
            var size = 500000, step = 2500;
            var geometry = new THREE.Geometry();
            var material = new THREE.LineBasicMaterial({vertexColors: THREE.VertexColors});
            var color1 = new THREE.Color(0x444444), color2 = new THREE.Color(0x888888);
            for (var i = -size; i <= size; i += step) {
                geometry.vertices.push(new THREE.Vector3(-size, 0, i));
                geometry.vertices.push(new THREE.Vector3(size, 0, i));
                geometry.vertices.push(new THREE.Vector3(i, 0, -size));
                geometry.vertices.push(new THREE.Vector3(i, 0, size));
                var color = i === 0 ? color1 : color2;
                geometry.colors.push(color, color, color, color);
            }
            _this.grid = new THREE.Line(geometry, material, THREE.LinePieces);
        }


        function initSelectionBox() {
            selectionBox = new THREE.BoxHelper();
            selectionBox.material.depthTest = true;
            selectionBox.material.transparent = true;
            selectionBox.visible = false;
            selectionBox.overdraw = true;
            _this.scene.add(selectionBox);
        }

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

        function handleResize() {
            _this.cameraObject.aspect = _this.$container.innerWidth() / _this.$container.innerHeight();
            _this.cameraObject.updateProjectionMatrix();
            _this.renderer.setSize(_this.$container.innerWidth(), _this.$container.innerHeight());
            controlsObject.handleResize();
            _this.reDraw();
        }

        function setSelectionBoxOnMesh(mesh) {
            selectionBox.update(mesh);
            selectionBox.visible = true;
        }

        function unsetSelectionBox() {
            selectionBox.visible = false;
        }

        /**
         * Controls management
         */
        function createPointerLockControls() {
            _this.pointerLockCamera = new THREE.PerspectiveCamera(45, _this.$container.width() / _this.$container.height(), App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
            _this.pointerLockControls = new THREE.PointerLockControls(_this.pointerLockCamera);
            addLightsToCamera(_this.pointerLockCamera);
        }

        function createOrbitControls() {
            _this.orbitCamera = new THREE.PerspectiveCamera(45, _this.$container.width() / _this.$container.height(), App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
            _this.orbitCamera.position.copy(App.SceneOptions.defaultCameraPosition);
            addLightsToCamera(_this.orbitCamera);
            _this.orbitControls = new THREE.OrbitControls(_this.orbitCamera, _this.$container[0]);
        }

        function createTrackBallControls() {
            _this.trackBallCamera = new THREE.PerspectiveCamera(45, _this.$container.width() / _this.$container.height(), App.SceneOptions.cameraNear, App.SceneOptions.cameraFar);
            _this.trackBallCamera.position.copy(App.SceneOptions.defaultCameraPosition);
            addLightsToCamera(_this.trackBallCamera);
            _this.trackBallControls = new THREE.TrackballControls(_this.trackBallCamera, _this.$container[0]);
            _this.trackBallControls.keys = [65 /*A*/, 83 /*S*/, 68 /*D*/];
        }

        function initControls() {
            createPointerLockControls();
            createTrackBallControls();
            createOrbitControls();
        }

        function createTransformControls() {
            transformControls = new THREE.TransformControls(_this.$container[0]);
            transformControls.addEventListener('change', _this.reDraw);
        }

        function isPointerLock() {
            return (document.pointerLockElement === _this.$container[0] ||
            document.mozPointerLockElement === _this.$container[0] ||
            document.webkitPointerLockElement === _this.$container[0]);
        }

        function pointerLockChange() {
            _this.pointerLockControls.enabled = isPointerLock();
            if (_this.pointerLockControls.enabled) {
                _this.$blocker.hide();
                _this.pointerLockControls.bindEvents();
            } else {
                _this.$blocker.show();
                _this.pointerLockControls.unbindEvents();
            }
        }

        /**
         * Lock the pointer (when you click on container) and Hide the gray screen
         */
        function bindPointerLock() {
            if (_this.stateControl !== _this.STATECONTROL.PLC || _this.pointerLockControls.enabled || isPointerLock()) {
                return;
            }
            _this.$blocker.hide();

            // Ask the browser to lock the pointer
            _this.$container[0].requestPointerLock = (_this.$container[0].requestPointerLock) ||
            (_this.$container[0].mozRequestPointerLock) ||
            (_this.$container[0].webkitRequestPointerLock);

            _this.$container[0].requestPointerLock();
        }

        function deleteAllControls() {
            _this.trackBallControls.removeEventListener('change');
            _this.trackBallControls.unbindEvents();
            _this.scene.remove(_this.trackBallCamera);
            _this.trackBallControls.enabled = false;

            _this.pointerLockControls.removeEventListener('change');
            _this.scene.remove(_this.pointerLockControls.getObject());
            _this.pointerLockControls.enabled = false;
            _this.pointerLockControls.unbindEvents();
            _this.$blocker.hide();
            if (browserSupportPointerLock) {
                // Hook pointer lock state change events
                document.removeEventListener('pointerlockchange', pointerLockChange, false);
                document.removeEventListener('mozpointerlockchange', pointerLockChange, false);
                document.removeEventListener('webkitpointerlockchange', pointerLockChange, false);
                _this.$container[0].removeEventListener('click', bindPointerLock, false);
            }

            _this.orbitControls.removeEventListener('change');
            _this.scene.remove(_this.orbitControls.getObject());
            _this.orbitControls.enabled = false;
            _this.orbitControls.unbindEvents();

            _this.deleteTransformControls();
        }

        /**
         * Scene options control
         */
        function onControlChange() {
            App.instancesManager.planNewEval();
            controlChanged = true;
            _this.reDraw();
        }

        function applyExplosionValue(object) {
            if (!object.absoluteCentroid) {
                var mesh = object.children[0];
                mesh.geometry.computeBoundingBox();
                mesh.geometry.computeBoundingSphere();
                var instance = App.instancesManager.getInstance(object.uuid);
                object.absoluteCentroid = mesh.geometry.boundingBox.center().clone().applyMatrix4(instance.matrix);
            }

            // Replace before translating
            object.position.x = object.initialPosition.x;
            object.position.y = object.initialPosition.y;
            object.position.z = object.initialPosition.z;
            // Translate instance
            if (explosionValue !== 0) {
                object.translateX(object.absoluteCentroid.x * explosionValue);
                object.translateY(object.absoluteCentroid.y * explosionValue);
                object.translateZ(object.absoluteCentroid.z * explosionValue);
            }
            object.updateMatrix();
        }

        function showGrid() {
            if (_this.grid.added) {
                return;
            }
            _this.grid.added = true;
            _this.scene.add(_this.grid);
            _this.reDraw();
        }

        function removeGrid() {
            if (!_this.grid.added) {
                return;
            }
            _this.grid.added = false;
            _this.scene.remove(_this.grid);
            _this.reDraw();
        }

        function watchSceneOptions() {
            if (App.SceneOptions.grid) {
                showGrid();
            } else {
                removeGrid();
            }
        }

        function updateMeasures() {
            _.each(measureTexts, function (text) {
                text.rotation.copy(_this.cameraObject.rotation);
            });
        }

        /**
         * Scene mouse events
         */
        function onMouseEnter() {
        }

        function onMouseLeave() {
            isMoving = false;
        }

        function onKeyDown() {
        }

        function onKeyUp() {

        }

        function onMouseDown() {
            isMoving = false;
        }

        function onSceneMouseWheel() {
        }

        function onSceneMouseMove(e) {

            if (_this.measureState && measureTool.hasOnlyFirstPoint()) {
                var vector = new THREE.Vector3(
                    ((e.clientX - _this.$container.offset().left) / _this.$container[0].offsetWidth ) * 2 - 1,
                    -((e.clientY - _this.$container.offset().top) / _this.$container[0].offsetHeight ) * 2 + 1,
                    0.5
                );
                projector.unprojectVector(vector, _this.cameraObject);
                measureTool.setVirtualPoint(vector);
                _this.reDraw();
            }

            isMoving = true;

        }

        function getMeshes() {
            var meshes = [];
            _this.scene.traverse(function (object) {
                if (object instanceof THREE.Object3D) {
                    _.each(object.children, function (child) {
                        if (child instanceof THREE.Mesh) {
                            meshes.push(child);
                        }
                    });
                }
            });
            return meshes;
        }

        function onMouseUp(event) {

            event.preventDefault();

            if (isMoving) {
                return false;
            }

            // RayCaster to get the clicked mesh
            var vector = new THREE.Vector3(
                ((event.clientX - _this.$container.offset().left) / _this.$container[0].offsetWidth ) * 2 - 1,
                -((event.clientY - _this.$container.offset().top) / _this.$container[0].offsetHeight ) * 2 + 1,
                0.5
            );

            var cameraPosition = controlsObject.getObject().position;

            var object = _this.cameraObject;
            projector.unprojectVector(vector, object);

            var ray = new THREE.Raycaster(cameraPosition, vector.sub(cameraPosition).normalize());
            var intersects = ray.intersectObjects(getMeshes(), false);

            if (intersects.length > 0) {

                var clickedObject = intersects[0];
                var mesh = clickedObject.object;
                var object3D = mesh.parent;
                if (object3D.partIterationId) {
                    if (_this.markerCreationMode) {
                        var mcmv = new MarkerCreateModalView({
                            model: currentLayer,
                            intersectPoint: intersects[0].point
                        });
                        document.body.appendChild(mcmv.render().el);
                        mcmv.openModal();
                    }
                    else if (_this.measureState) {
                        measureTool.onClick(clickedObject.point.clone());
                    }
                    else {
                        objectMarkedForSelection = object3D.uuid;
                        setSelectionBoxOnMesh(mesh);
                        Backbone.Events.trigger('object:selected', object3D);
                    }
                } else if (mesh.markerId) {
                    _this.layerManager.onMarkerClicked(mesh.markerId);
                }
            }
            else {
                if (!App.sceneManager.transformControlsEnabled()) {
                    Backbone.Events.trigger('selection:reset');
                    objectMarkedForSelection = null;
                    unsetSelectionBox();
                }
            }

            _this.reDraw();
        }

        function bindMouseAndKeyEvents() {
            _this.$container[0].addEventListener('mousedown', onMouseDown, false);
            _this.$container[0].addEventListener('mouseup', onMouseUp, false);
            _this.$container[0].addEventListener('mouseover', onMouseEnter, false);
            _this.$container[0].addEventListener('mouseout', onMouseLeave, false);
            _this.$container[0].addEventListener('keydown', onKeyDown, false);
            _this.$container[0].addEventListener('keyup', onKeyUp, false);
            _this.$container[0].addEventListener('mousemove', onSceneMouseMove, false);
            _this.$container[0].addEventListener('mousewheel', onSceneMouseWheel, false);
        }

        function removeObject(objectId) {
            var object = objects[objectId];
            delete objects[objectId];

            if (_this.editedObjects.indexOf(objectId) !== -1) {
                _this.editedObjectsLeft.push({
                    uuid: objectId,
                    position: object.position.clone(),
                    rotation: object.rotation.clone(),
                    scale: object.scale.clone()
                });
                _this.editedObjects = _(_this.editedObjects).without(objectId);
                if (transformControls !== null && transformControls.enabled && transformControls.getObject() === object) {
                    _this.deleteTransformControls();
                }
            }

            if (!object) {
                return;
            }

            if (objectMarkedForSelection === objectId) {
                Backbone.Events.trigger('selection:reset');
                objectMarkedForSelection = null;
                unsetSelectionBox();
            }
            _this.scene.remove(object);

            App.log('%c object removed', 'SM');

            _this.reDraw();
        }

        function saveMaterials(object){
            _.each(object.children,function(o){
                if(o instanceof THREE.Mesh){
                    o.initialMaterial = o.material;
                    saveMaterials(o);
                }
            });
        }

        function setEditedMaterials(object){
            _.each(object.children,function(o){
                if(o instanceof THREE.Mesh){
                    o.material = materialEditedObject;
                    setEditedMaterials(o);
                }
            });
        }

        function restoreInitialMaterials(object){
            _.each(object.children,function(o){
                if(o instanceof THREE.Mesh){
                    o.material = o.initialMaterial;
                    restoreInitialMaterials(o);
                }
            });
        }

        function processLoadedStuff() {

            var loadedStuff = App.instancesManager.getLoadedGeometries(10);

            loadedStuff.forEach(function (stuff) {

                var instance = App.instancesManager.getInstance(stuff.id);

                if (instance) {

                    var oldObject = objects[stuff.id];
                    var newObject = stuff.object3d;

                    if (oldObject) {
                        _this.switches++;
                        _this.scene.remove(oldObject);
                    } else {
                        _this.adds++;
                        _this.onScene++;
                    }

                    objects[stuff.id] = newObject;

                    newObject.uuid = stuff.id;
                    newObject.partIterationId = stuff.partIterationId;
                    newObject.path = stuff.path;

                    newObject.applyMatrix(instance.matrix);

                    newObject.initialPosition = {
                        x: newObject.position.x,
                        y: newObject.position.y,
                        z: newObject.position.z
                    };
                    newObject.initialRotation = {
                        x: newObject.rotation.x,
                        y: newObject.rotation.y,
                        z: newObject.rotation.z
                    };

                    newObject.initialScale = {
                        x: newObject.scale.x,
                        y: newObject.scale.y,
                        z: newObject.scale.z
                    };

                    if (objectMarkedForSelection === newObject.uuid) {
                        setSelectionBoxOnMesh(newObject.children[0]);
                    }

                    applyExplosionValue(newObject);

                    saveMaterials(newObject);

                    var potentiallyEdited = _(_this.editedObjectsLeft).findWhere({uuid: newObject.uuid});

                    if (potentiallyEdited) {

                        newObject.position.copy(potentiallyEdited.position);
                        newObject.rotation.copy(potentiallyEdited.rotation);
                        newObject.scale.copy(potentiallyEdited.scale);

                        if (editedObjectsColoured) {
                            setEditedMaterials(newObject);
                        }

                        _this.editedObjectsLeft = _(_this.editedObjectsLeft).without(potentiallyEdited);
                        _this.editedObjects.push(potentiallyEdited.uuid);

                    }

                    _this.scene.add(newObject);

                }
                _this.reDraw();
            });
        }

        /**
         *  Animation
         */
        function cameraAnimation(target, duration, position) {
            var curTar = controlsObject.target;
            var endTarPos = target;

            new TWEEN.Tween(curTar)
                .to({x: endTarPos.x, y: endTarPos.y, z: endTarPos.z}, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(_this.reDraw)
                .start();

            if (position) {
                var endCamPos = position;
                var camera = _this.cameraObject;
                var curCamPos = camera.position;

                new TWEEN.Tween(curCamPos)
                    .to({x: endCamPos.x, y: endCamPos.y, z: endCamPos.z}, duration)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Quintic.InOut)
                    .onUpdate(_this.reDraw)
                    .start();
            }
        }

        function resetCameraAnimation(target, duration, position, camUp) {

            // Not working with pointer lock controls, pointer lock doesn't have a target
            // TODO : We must reset it an other way
            if (controlsObject instanceof THREE.PointerLockControls) {
                return;
            }

            var curTar = controlsObject.target;
            var curCamUp = _this.cameraObject.up;
            var endTarPos = target;


            var endCamPos = position;
            var camera = _this.cameraObject;
            var curCamPos = camera.position;

            var tween1 = new TWEEN.Tween(curTar)
                    .to({x: endTarPos.x, y: endTarPos.y, z: endTarPos.z}, duration)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Linear.None)
                    .onUpdate(_this.reDraw);


            var tween2 = new TWEEN.Tween(curCamPos)
                .to({x: endCamPos.x, y: endCamPos.y, z: endCamPos.z}, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Linear.None)
                .onUpdate(_this.reDraw);

            var tween3 = new TWEEN.Tween(curCamUp)
                .to({x: camUp.x, y: camUp.y, z: camUp.z}, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Linear.None)
                .onUpdate(_this.reDraw);

            tween1.start();
            tween2.start();
            tween3.start();
        }

        /**
         * Collaborative Mode
         */
        this.setEditedObjects = function (editedObjectsInfos) {

            var arrayId = _.pluck(editedObjectsInfos, 'uuid');

            // cancel transformations for objects which are no longer edited

            var diff = _.difference(_this.editedObjects, arrayId);

            _.each(diff, function (uuid) {
                var object = objects[uuid];
                if (_this.editedObjects.lastIndexOf(uuid) !== -1) {
                    _this.cancelTransformation(object);
                }
            });

            // update the list
            _this.editedObjects = arrayId;

            // update properties of edited objects
            _.each(editedObjectsInfos, function (val) {
                var object = objects[val.uuid];
                if (!object) {
                    _this.editedObjects = _.without(_this.editedObjects, val.uuid);
                    _this.editedObjectsLeft.push(val);
                } else {
                    object.position.copy(val.position);
                    object.rotation.copy(val.rotation);
                    object.scale.copy(val.scale);

                    if (editedObjectsColoured) {
                        setEditedMaterials(object);
                    }
                }

            });
            _this.reDraw();
        };
        this.setEditedObjectsColor = function (colour) {
            if (editedObjectsColoured !== colour) {
                if (editedObjectsColoured) {
                    _this.cancelColourEditedObjects();
                } else {
                    _this.colourEditedObjects();
                }
            }
        };

        /**
         * Animation loop :
         *  Update controls, scene objects and animations
         *  Render at the end
         * */
        //Main UI loop
        function animate() {
            requestAnimationFrame(animate);
            // Update controls
            controlsObject.update(clock.getDelta());

            processLoadedStuff();


            // Update with SceneOptions
            watchSceneOptions();

            // Update potential animation
            TWEEN.update();
            // Sometimes needs a reFrame
            if (needsRedraw) {
                needsRedraw = false;
                if (_this.transformControlsEnabled()) {
                    transformControls.update();
                }
                _this.stats.update();
                updateMeasures();
                render();
            }
            if (controlChanged && App.collaborativeController) {
                App.collaborativeController.sendCameraInfos();
                controlChanged = false;
            }
        }

        this.init = function () {
            _.bindAll(_this);
            initDOM();
            initControls();
            initLayerManager();
            initMeasureTool();
            initAxes();
            initGrid();
            initSelectionBox();
            initRenderer();
            initStats();
            window.addEventListener('resize', handleResize, false);
            bindMouseAndKeyEvents();

            if (App.SceneOptions.transformControls) {
                createTransformControls();
            }

            _this.setTrackBallControls();

            animate();
        };

        this.reDraw = function () {
            needsRedraw = true;
        };

        this.flyTo = function (object) {

            var mesh = object;

            if (object instanceof THREE.Object3D) {
                mesh = object.children[0];
            }

            var boundingBox = mesh.geometry.boundingBox;
            var cog = boundingBox.center().clone().applyMatrix4(object.matrix);
            var size = boundingBox.size();
            var radius = Math.max(size.x, size.y, size.z);
            var camera = _this.cameraObject;
            var dir = new THREE.Vector3().copy(cog).sub(camera.position).normalize();
            var distance = radius ? radius * 2 : 1000;
            distance = distance < App.SceneOptions.cameraNear ? App.SceneOptions.cameraNear + 100 : distance;
            var endCamPos = new THREE.Vector3().copy(cog).sub(dir.multiplyScalar(distance));
            cameraAnimation(cog, 2000, endCamPos);
        };

        this.lookAt = function (object) {
            var mesh = object.children[0];
            var boundingBox = mesh.geometry.boundingBox;
            var cog = boundingBox.center().clone().applyMatrix4(object.matrix);
            cameraAnimation(cog, 2000);
        };

        this.resetCameraPlace = function () {
            var camPos = App.SceneOptions.defaultCameraPosition;
            resetCameraAnimation(new THREE.Vector3(0, 0, 0), 1000, camPos, new THREE.Vector3(0, 1, 0));
        };

        /**
         * Context API
         */

        this.getControlsContext = function () {
            return {
                target: controlsObject.getTarget(),
                camPos: controlsObject.getCamPos(),
                camOrientation: _this.cameraObject.up
            };
        };

        this.setControlsContext = function (context) {
            _this.cameraObject.position.copy(context.camPos);
            controlsObject.target.copy(context.target);
            _this.cameraObject.up.copy(context.camOrientation);
            _this.reDraw();
        };

        // If transformControls are enabled return the mode (translation, rotation, scaling), null otherwise
        this.getTransformControlsMode = function () {
            if (transformControls.enabled) {
                return transformControls.getMode();
            } else {
                return null;
            }
        };

        this.transformControlsEnabled = function () {
            var enabled = false;
            if (transformControls !== null && transformControls.enabled) {
                enabled = true;
            }
            return enabled;
        };

        this.enableControlsObject = function () {
            if (controlsObject) {
                controlsObject.enabled = true;
            }
        };

        this.disableControlsObject = function () {
            if (controlsObject) {
                controlsObject.enabled = false;
            }
        };

        /**
         * Scene option control
         */
        this.startMarkerCreationMode = function (layer) {
            _this.markerCreationMode = true;
            currentLayer = layer;
            App.$SceneContainer.addClass('markersCreationMode');
        };

        this.stopMarkerCreationMode = function () {
            _this.markerCreationMode = false;
            currentLayer = null;
            App.$SceneContainer.removeClass('markersCreationMode');
        };

        this.requestFullScreen = function () {
            _this.renderer.domElement.parentNode.requestFullscreen =
                (_this.renderer.domElement.parentNode.requestFullscreen) ||
                (_this.renderer.domElement.parentNode.mozRequestFullScreen) ||
                (_this.renderer.domElement.parentNode.webkitRequestFullScreen);
            _this.renderer.domElement.parentNode.requestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
        };

        this.explodeScene = function (v) {
            App.collaborativeController.sendExplodeValue(v);
            // this could be adjusted
            explosionValue = v * 0.1;
            _(_this.scene.children).each(function (object) {
                if (object instanceof THREE.Object3D && object.partIterationId) {
                    applyExplosionValue(object);
                }
            });
            _this.reDraw();
        };

        this.drawMeasure = function (points) {
            var material = new THREE.LineBasicMaterial({
                color: 0xf47922
            });
            var geometry = new THREE.Geometry();
            geometry.vertices.push(points[0]);
            geometry.vertices.push(points[1]);
            var line = new THREE.Line(geometry, material);

            var dist = points[0].distanceTo(points[1]);
            var distText = (dist / 1000).toFixed(3) + ' m';

            var size = dist / 100 + 10;

            var textGeo = new THREE.TextGeometry(distText, {size: size, height: 2, font: 'helvetiker'});
            var textMaterial = new THREE.MeshBasicMaterial({color: 0xf47922});
            var text = new THREE.Mesh(textGeo, textMaterial);

            text.position.copy(new THREE.Vector3().addVectors(points[0], points[1]).multiplyScalar(0.5));
            text.rotation.copy(_this.cameraObject.rotation);

            measures.push(line);
            measureTexts.push(text);
            _this.scene.add(line);
            _this.scene.add(text);

            measuresPoints.push(points);
            App.collaborativeController.sendMeasure();
            Backbone.Events.trigger('measure:drawn');
            _this.reDraw();
        };

        this.clearMeasures = function () {
            _.each(measures, function (line) {
                _this.scene.remove(line);
            });
            _.each(measureTexts, function (text) {
                _this.scene.remove(text);
            });
            measures = [];
            measureTexts = [];
            measuresPoints = [];
            App.collaborativeController.sendMeasure();
            _this.reDraw();
        };

        this.setMeasureState = function (state) {
            App.$SceneContainer.toggleClass('measureMode', state);
            _this.measureState = state;

            if (!state) {
                measureTool.callbacks.onCancelled();
            }
        };

        this.takeScreenShot = function () {

            var imageSource = _this.renderer.domElement.toDataURL('image/png');
            var filename = App.config.productId + '-' + date.formatTimestamp(App.config.i18n._DATE_SHORT_FORMAT, Date.now());

            var save = document.createElement('a');
            save.href = imageSource;
            save.download = filename;
            var event = document.createEvent('MouseEvents');
            event.initMouseEvent(
                'click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null
            );
            save.dispatchEvent(event);

        };

        this.setCameraNear = function (n) {
            _this.cameraObject.near = n;
            _this.cameraObject.updateProjectionMatrix();
            _this.reDraw();

        };

        this.colourEditedObjects = function () {
            editedObjectsColoured = true;
            _.each(_this.editedObjects, function (uuid) {
                setEditedMaterials(objects[uuid]);
            });
            App.collaborativeController.sendColourEditedObjects();
            _this.reDraw();
        };

        this.cancelColourEditedObjects = function () {

            editedObjectsColoured = false;

            _.each(_this.editedObjects, function (uuid) {
                restoreInitialMaterials(objects[uuid]);
            });

            App.collaborativeController.sendColourEditedObjects();

            _this.reDraw();
        };

        this.setPointerLockControls = function () {
            if (_this.stateControl === _this.STATECONTROL.PLC || !browserSupportPointerLock) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.PLC;
            deleteAllControls();
            _this.$blocker.show();

            _this.cameraObject = _this.pointerLockCamera;
            controlsObject = _this.pointerLockControls;

            _this.pointerLockControls.addEventListener('change', onControlChange);

            // Hook pointer lock state change events
            document.addEventListener('pointerlockchange', pointerLockChange, false);
            document.addEventListener('mozpointerlockchange', pointerLockChange, false);
            document.addEventListener('webkitpointerlockchange', pointerLockChange, false);
            _this.$container[0].addEventListener('click', bindPointerLock, false);

            _this.scene.add(_this.pointerLockControls.getObject());
            _this.reDraw();
        };

        this.setTrackBallControls = function () {
            if (_this.stateControl === _this.STATECONTROL.TBC && controlsObject.enabled) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.TBC;
            deleteAllControls();

            controlsObject = _this.trackBallControls;
            _this.cameraObject = _this.trackBallCamera;

            _this.trackBallControls.enabled = true;
            _this.trackBallControls.addEventListener('change', onControlChange);
            _this.trackBallControls.bindEvents();
            _this.scene.add(_this.trackBallCamera);

            handleResize();
            _this.reDraw();
        };

        this.setOrbitControls = function () {

            if (_this.stateControl === _this.STATECONTROL.ORB && controlsObject.enabled) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.ORB;
            deleteAllControls();

            controlsObject = _this.orbitControls;
            controlsObject.enabled = true;

            _this.cameraObject = _this.orbitCamera;

            controlsObject.addEventListener('change', onControlChange);
            controlsObject.bindEvents();
            _this.scene.add(_this.orbitCamera);

            handleResize();
            _this.reDraw();
        };

        this.setTransformControls = function (mesh, mode) {
            transformControls.setCamera(_this.cameraObject);
            controlsObject.enabled = false;
            transformControls.enabled = true;
            transformControls.attach(mesh);
            if (!_.contains(_this.editedObjects, mesh.uuid)) {
                _this.editedObjects.push(mesh.uuid);
                if (editedObjectsColoured) {
                    _this.colourEditedObjects();
                }
                App.log('%c Mesh added : \n\t' + this.editedObjects, 'SM');
                App.collaborativeController.sendEditedObjects();
            }
            transformControls.bindEvents();
            if (typeof(mode) !== 'undefined') {
                switch (mode) {
                    case 'translate' :
                        transformControls.setMode('translate');
                        break;
                    case 'rotate':
                        transformControls.setMode('rotate');
                        break;
                    case 'scale':
                        transformControls.setMode('scale');
                }
            }
            App.appView.transformControlMode();
            _this.scene.add(transformControls);
            _this.reDraw();
        };

        this.leaveTransformMode = function () {
            if (_this.stateControl === _this.STATECONTROL.TBC) {
                _this.setTrackBallControls();
            } else if (_this.stateControl === _this.STATECONTROL.ORB) {
                _this.setOrbitControls();
            }
        };

        this.deleteTransformControls = function () {
            if (transformControls !== null && transformControls.enabled) {
                _this.scene.remove(transformControls);
                transformControls.unbindEvents();
                transformControls.detach();
                transformControls.enabled = false;
                App.appView.leaveTransformControlMode();
                _this.reDraw();
            }
        };

        this.cancelTransformation = function (object) {

            new TWEEN.Tween(object.position)
                .to({x: object.initialPosition.x, y: object.initialPosition.y, z: object.initialPosition.z}, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            new TWEEN.Tween(object.rotation)
                .to({x: object.initialRotation.x, y: object.initialRotation.y, z: object.initialRotation.z}, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            new TWEEN.Tween(object.scale)
                .to({x: object.initialScale.x, y: object.initialScale.y, z: object.initialScale.z}, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            _this.editedObjects = _.without(_this.editedObjects, object.uuid);
            restoreInitialMaterials(object);
            App.log('%c Mesh removed', 'SM');
            App.collaborativeController.sendEditedObjects();

            _this.reDraw();
            _this.leaveTransformMode();
        };

        /**
         * Scene mouse events
         */

        this.setPathForIFrame = function (pathForIFrame) {
            _this.pathForIFrameLink = pathForIFrame;
        };

        this.clear = function () {

        };

        this.removeObjectById = function (objectId) {
            removeObject(objectId);
        };

        this.getObject = function (uuid) {
            return objects[uuid];
        };

        this.getEditedObjectsColoured = function () {
            return editedObjectsColoured;
        };

        this.updateAmbientLight = function (color) {
            _this.cameraObject.getObjectByName('AmbientLight').color.set(color);
            _this.reDraw();
        };

        this.updateCameraLight1 = function (color) {
            _this.cameraObject.getObjectByName('CameraLight1').color.set(color);
            _this.reDraw();
        };

        this.updateCameraLight2 = function (color) {
            _this.cameraObject.getObjectByName('CameraLight2').color.set(color);
            _this.reDraw();
        };

        this.createLayerMaterial = function (color) {
            return new THREE.MeshLambertMaterial({
                color: color,
                transparent: true
            });
        };

        this.onContainerShown = function () {
            handleResize();
        };

        this.getMeasures = function () {
            return measuresPoints;
        };

        this.setMeasures = function (measuresPoints) {
            _this.clearMeasures();
            _.each(measuresPoints, function (points) {
                var point0 = new THREE.Vector3(points[0].x, points[0].y, points[0].z);
                var point1 = new THREE.Vector3(points[1].x, points[1].y, points[1].z);

                _this.drawMeasure([point0, point1]);
            });
        };

        this.bestFitView = function(){

            var box = App.instancesManager.computeGlobalBBox();
            var size = box.size();

            if(size.length()){
                var cog = box.center().clone();
                var radius = Math.max(size.x, size.y, size.z);
                var camera = _this.cameraObject;
                var dir = new THREE.Vector3().copy(cog).sub(camera.position).normalize();
                var distance = radius ? radius / 2  : 1000;
                distance = distance < App.SceneOptions.cameraNear ? App.SceneOptions.cameraNear + 100 : distance;
                var endCamPos = new THREE.Vector3().copy(cog).sub(dir.multiplyScalar(distance));
                cameraAnimation(cog, 2000, endCamPos);
                _this.cameraObject.far = radius * 2;

                _this.cameraObject.updateProjectionMatrix();
            }

        };

    };

    return SceneManager;
});
