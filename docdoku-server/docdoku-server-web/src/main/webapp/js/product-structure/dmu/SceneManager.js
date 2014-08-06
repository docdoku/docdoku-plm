/*global App,APP_CONFIG,Stats,Instance,requestAnimationFrame,TWEEN,ChannelMessagesType,mainChannel,ChannelStatus*/
'use strict';
define([
    "views/marker_create_modal_view",
    "views/blocker_view",
    "dmu/LayerManager"
], function (MarkerCreateModalView, BlockerView, LayerManager) {

    var SceneManager = function (pOptions) {
        var _this = this;

        var browserSupportPointerLock = 'pointerLockElement' in document ||
            'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;
        var options = pOptions || {};
        _.extend(this, options);
        var isMoving = false;
        var currentLayer = null;
        var explosionCoeff = 0;
        var wireframe = false;
        var projector = new THREE.Projector();
        var controlsObject = null;                                                                                      // Switching controls means different camera management
        var clock = new THREE.Clock();
        var needsRedraw = false;
        var meshesIndexed = {};
        var selectionBox = null;
        var meshMarkedForSelection = null;
        var controlChanged = false;
        var editedMeshesColoured = false;
        var transformControls = null;

        var materialEditedMesh = new THREE.MeshPhongMaterial({ transparent: false, color: new THREE.Color(0x08B000) });

        this.stateControl = null;
        this.STATECONTROL = { PLC: 0, TBC: 1, ORB: 2};
        this.scene = new THREE.Scene();
        this.renderer = null;
        this.cameraObject = null;                                                                                       // Represent the eye
        this.layerManager = null;
        this.editedMeshes = [];
        this.editedMeshesLeft = [];

        // Stat
        this.switches = 0;
        this.adds = 0;
        this.onScene = 0;

        function render() {
            _this.scene.updateMatrixWorld();
            _this.renderer.render(_this.scene, _this.cameraObject);
        }

        function initDOM() {
            _this.$container = $('div#container');
            _this.$container[0].setAttribute('tabindex', "-1");
            _this.$sceneContainer = $("#scene_container");
            _this.$blocker = new BlockerView().render().$el;
            _this.$container.append(_this.$blocker);
        }

        function initRenderer() {
            _this.renderer = new THREE.WebGLRenderer({preserveDrawingBuffer: true, alpha: true});
            _this.renderer.setSize(_this.$container.width(), _this.$container.height());
            _this.$container.append(_this.renderer.domElement);
        }

        function initLayerManager() {
            if (!_.isUndefined(APP_CONFIG.productId)) {
                _this.layerManager = new LayerManager();
                _this.layerManager.rescaleMarkers();
                _this.layerManager.renderList();
            }
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
            _this.$sceneContainer.append(_this.stats.domElement);
            _this.$stats = $(_this.stats.domElement);
            _this.$stats.attr('id', 'statsWin');
            _this.$stats.attr('class', 'statsWinMaximized');
            _this.$statsArrow = $("<i id=\"statsArrow\" class=\"fa fa-chevron-down\"></i>");
            _this.$stats.prepend(_this.$statsArrow);
            _this.$statsArrow.bind('click', function () {
                _this.$stats.toggleClass('statsWinMinimized statsWinMaximized');
            });
        }

        function initGrid() {
            var size = 500, step = 25;
            var geometry = new THREE.Geometry();
            var material = new THREE.LineBasicMaterial({ vertexColors: THREE.VertexColors });
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

        function initAmbientLight() {
            var ambient = new THREE.AmbientLight(0x101030);
            _this.scene.add(ambient);
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
            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            camera.add(dirLight);
            camera.add(dirLight.target);
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
            _this.trackBallControls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];
        }

        function initControls() {
            createPointerLockControls();
            createTrackBallControls();
            createOrbitControls();
        }

        function createTransformControls() {
            transformControls = new THREE.TransformControls(_this.$container[0]);

            /*window.addEventListener( 'keyup', function ( event ) {
             switch (event.keyCode) {
             case 90: // Z
             transformControls.setMode("translate");
             break;
             case 69: // E
             transformControls.setMode("rotate");
             break;
             case 82: // R
             transformControls.setMode("scale");
             break;
             case 187:
             case 107: // +,=,num+
             transformControls.setSize(transformControls.size + 0.1);
             break;
             case 189:
             case 109: // -,_,num-
             transformControls.setSize(Math.max(transformControls.size - 0.1, 0.1));
             break;
             }
             });*/
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
            _this.trackBallControls.removeEventListener("change");
            _this.trackBallControls.unbindEvents();
            _this.scene.remove(_this.trackBallCamera);
            _this.trackBallControls.enabled = false;

            _this.pointerLockControls.removeEventListener("change");
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

            _this.orbitControls.removeEventListener("change");
            _this.scene.remove(_this.orbitControls.getObject());
            _this.orbitControls.enabled = false;
            _this.orbitControls.unbindEvents();

            _this.deleteTransformControls();
        }

        // example arg : _this.pointerLockControls,_this.pointerLockCamera
        /*function updateControlsContext(controls, camera) {

            if (!controlsObject) {
                return;
            }
            //camera.up.copy(controlsObject.getCamUp());
            //camera.position.copy(controlsObject.getCamPos());
            controls.setCamPos(controlsObject.getCamPos());
            controls.setCamUp(controlsObject.getCamUp());
            controls.setTarget(controlsObject.getTarget());


        }*/

        /**
         * Scene options control
         */

        function onControlChange() {
            App.instancesManager.planNewEval();
            controlChanged = true;
            _this.reDraw();
        }

        function applyWireFrame(mesh) {
            mesh.material.wireframe = wireframe;
            if ((mesh.material.materials)) {
                _(mesh.material.materials).each(function (m) {
                    m.wireframe = mesh.material.wireframe;
                });
            }
        }

        function applyExplosionCoeff(mesh) {
            if (!mesh.geometry.boundingBox) {
                mesh.geometry.computeBoundingBox();
                mesh.geometry.computeBoundingSphere();
                mesh.geometry.boundingBox.centroid = new THREE.Vector3(
                        (mesh.geometry.boundingBox.max.x + mesh.geometry.boundingBox.min.x) * 0.5,
                        (mesh.geometry.boundingBox.max.y + mesh.geometry.boundingBox.min.y) * 0.5,
                        (mesh.geometry.boundingBox.max.z + mesh.geometry.boundingBox.min.z) * 0.5
                );
            }

            // Replace before translating
            mesh.position.x = mesh.initialPosition.x;
            mesh.position.y = mesh.initialPosition.y;
            mesh.position.z = mesh.initialPosition.z;
            // Translate instance
            if (explosionCoeff !== 0) {
                mesh.translateX(mesh.geometry.boundingBox.centroid.x * explosionCoeff);
                mesh.translateY(mesh.geometry.boundingBox.centroid.y * explosionCoeff);
                mesh.translateZ(mesh.geometry.boundingBox.centroid.z * explosionCoeff);
            }
            mesh.updateMatrix();
        }

        function applyMeasureStateOpacity(mesh) {
            if (_this.measureState) {
                mesh.material.opacity = _this.measureState ? 0.5 : 1;
            }
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

        function onSceneMouseMove() {
            isMoving = true;
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
            var intersects = ray.intersectObjects(_this.scene.children, false);

            if (intersects.length > 0 && intersects[0].object.partIterationId) {
                if (_this.markerCreationMode) {
                    var mcmv = new MarkerCreateModalView({model: currentLayer, intersectPoint: intersects[0].point});
                    $("body").append(mcmv.render().el);
                    mcmv.openModal();
                }
                else if (_this.measureState) {
                    _this.measureCallback(intersects[0].point.clone());
                }
                else {
                    meshMarkedForSelection = intersects[0].object.uuid;
                    setSelectionBoxOnMesh(intersects[0].object);

                    Backbone.Events.trigger("mesh:selected", intersects[0].object);
                }
            }
            else if (intersects.length > 0 && intersects[0].object.markerId) {
                _this.layerManager.onMarkerClicked(intersects[0].object.markerId);
            }
            else {
                if (!App.sceneManager.transformControlsEnabled()) {
                    Backbone.Events.trigger("selection:reset");
                    meshMarkedForSelection = null;
                    unsetSelectionBox();
                }

                if (_this.measureState) {
                    _this.measureCallback(-1);
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

        /**
         * Meshes
         */
        function createMeshFromLoadedStuff(stuff, matrix) {
            var mesh = new THREE.Mesh(stuff.geometry, stuff.materials);
            mesh.uuid = stuff.id;
            mesh.partIterationId = stuff.partIterationId;
            mesh.geometry.verticesNeedUpdate = true;
            mesh.applyMatrix(matrix);
            mesh.initialPosition = {x: mesh.position.x, y: mesh.position.y, z: mesh.position.z};
            mesh.initialRotation = {x: mesh.rotation.x, y: mesh.rotation.y, z: mesh.rotation.z};
            mesh.initialScale = {x: mesh.scale.x, y: mesh.scale.y, z: mesh.scale.z};
            mesh.initialMaterial = mesh.material;
            var positionMeshEdited = _.indexOf(_this.editedMeshes, mesh.uuid);
            if (positionMeshEdited !== -1) {
                var meshEdited = meshesIndexed[positionMeshEdited];
                if (meshEdited) {
                    mesh.position.copy(meshEdited.position);
                    mesh.rotation.copy(meshEdited.rotation);
                    mesh.scale.copy(meshEdited.scale);
                    console.log("restauration de la position de l'instance transformée.");
                    console.log(_this.mesh);
                }

            }
            return mesh;
        }

        function removeMesh(meshId) {
            var mesh = meshesIndexed[meshId];
            delete meshesIndexed[meshId];
            if(_this.editedMeshes.indexOf(meshId) !== -1){
                _this.editedMeshesLeft.push({
                    uuid:meshId,
                    position:mesh.position.clone(),
                    rotation:mesh.rotation.clone(),
                    scale:mesh.scale.clone()
                });
                _this.editedMeshes = _(_this.editedMeshes).without(meshId);
                if (transformControls !== null && transformControls.enabled && transformControls.getObject()===mesh) {
                    _this.deleteTransformControls();
                }
            }
            if (!mesh) {
                return;
            }
            if (meshMarkedForSelection === meshId) {
                Backbone.Events.trigger("selection:reset");
                meshMarkedForSelection = null;
                unsetSelectionBox();
            }
            _this.scene.remove(mesh);
            _this.reDraw();
        }

        function processLoadedStuff() {
            var loadedStuff = App.instancesManager.getLoadedGeometries(10);
            loadedStuff.forEach(function (stuff) {
                var instance = App.instancesManager.getInstance(stuff.id);
                if (instance) {
                    var oldMesh = meshesIndexed[stuff.id];
                    var newMesh = createMeshFromLoadedStuff(stuff, instance.matrix);
                    if (oldMesh) {
                        _this.switches++;
                        _this.scene.remove(oldMesh);
                    } else {
                        _this.adds++;
                        _this.onScene++;
                    }
                    meshesIndexed[newMesh.uuid] = newMesh;
                    _this.scene.add(newMesh);
                    if (meshMarkedForSelection === stuff.id) {
                        setSelectionBoxOnMesh(newMesh);
                    }
                    applyExplosionCoeff(newMesh);
                    applyWireFrame(newMesh);
                    applyMeasureStateOpacity(newMesh);

                    var potentiallyEdited = _(_this.editedMeshesLeft).where({uuid:newMesh.uuid});
                    if(potentiallyEdited.length){
                        newMesh.position.copy(potentiallyEdited[0].position);
                        newMesh.rotation.copy(potentiallyEdited[0].rotation);
                        newMesh.scale.copy(potentiallyEdited[0].scale);
                        if (editedMeshesColoured){
                            newMesh.material = materialEditedMesh;
                        }
                        _this.editedMeshesLeft = _(_this.editedMeshesLeft).without(potentiallyEdited[0]);
                        _this.editedMeshes.push(potentiallyEdited[0].uuid);
                    }

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
                .to({ x: endTarPos.x, y: endTarPos.y, z: endTarPos.z }, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                })
                .start();

            if (position) {
                var endCamPos = position;
                var camera = _this.cameraObject;
                var curCamPos = camera.position;

                new TWEEN.Tween(curCamPos)
                    .to({ x: endCamPos.x, y: endCamPos.y, z: endCamPos.z }, duration)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Quintic.InOut)
                    .onUpdate(function () {
                        _this.reDraw();
                    })
                    .start();
            }
        }

        function resetCameraAnimation(target, duration, position, camUp) {
            var curTar = controlsObject.target;
            var curCamUp = _this.cameraObject.up;
            var endTarPos = target;


            var endCamPos = position;
            var camera = _this.cameraObject;
            var curCamPos = camera.position;

            var tween1 = new TWEEN.Tween(curTar)
                    .to({ x: endTarPos.x, y: endTarPos.y, z: endTarPos.z }, duration)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Linear.None)
                    .onUpdate(function () {
                        _this.reDraw();
                    })
                ;


            var tween2 = new TWEEN.Tween(curCamPos)
                    .to({ x: endCamPos.x, y: endCamPos.y, z: endCamPos.z }, duration)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Linear.None)
                    .onUpdate(function () {
                        _this.reDraw();
                    })
                ;

            var tween3 = new TWEEN.Tween(curCamUp)
                .to({x: camUp.x, y: camUp.y, z: camUp.z}, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Linear.None)
                .onUpdate(function () {
                    _this.reDraw();
                });


            tween1.start();

            //tween2.chain(tween3);
            tween2.start();
            tween3.start();
        }

        /**
         * Colaborative Mode
         */
        this.sendCameraInfos = function () {
            if (App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        cameraInfos: _this.getControlsContext()
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };

        this.sendEditedMeshes = function () {
            if (App.collaborativeView.isMaster) {
                var arrayIds = [];
                _.each(this.editedMeshes, function (val) {
                        var mesh = meshesIndexed[val];
                        arrayIds.push({
                            uuid: val,
                            position: mesh.position,
                            scale: mesh.scale,
                            rotation: mesh.rotation
                        });
                });
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        editedMeshes: arrayIds
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };

        this.setEditedMeshes = function (editedMeshesInfos) {

            var arrayId = _.pluck(editedMeshesInfos, 'uuid');

            // cancel transformations for mesh wich are no longer edited
            var diff = _.difference(_this.editedMeshes, arrayId);
            console.log(diff);
            _.each(diff, function (uuid) {
                var mesh = meshesIndexed[uuid];
                if(_this.editedMeshes.lastIndexOf(uuid)!==-1) {
                    _this.cancelTransformation(mesh);
                }
            });

            // update the list
            _this.editedMeshes = arrayId;

            // update properties of edited Meshes
            _.each(editedMeshesInfos, function (val) {
                var mesh = meshesIndexed[val.uuid];
                if (!mesh) {
                    _this.editedMeshes = _.without(_this.editedMeshes, val.uuid);
                    _this.editedMeshesLeft.push(val);
                }else{
                    mesh.position.copy(val.position);
                    mesh.rotation.copy(val.rotation);
                    mesh.scale.copy(val.scale);
                    if (editedMeshesColoured){
                        mesh.material = materialEditedMesh;
                    }
                }
            });
            _this.reDraw();
        };

        this.sendColourEditedMeshes = function () {
            if (App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        colourEditedMeshes: editedMeshesColoured
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };

        this.setColourEditedMeshes = function (colour) {
            if (editedMeshesColoured !== colour){
                if(editedMeshesColoured){
                    _this.cancelColourEditedMeshes();
                } else {
                    _this.colourEditedMeshes();
                }
            }
        };

        this.sendExplodeValue = function (value) {
            if (App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        explode: value
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };

        /**
         * Animation loop :
         *  Update controls, scene objects and animations
         *  Render at the end
         * */
            //Main UI loop
        function animate() {
            requestAnimationFrame(animate, null);
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

                render();
            }
            if (controlChanged) {
                //sendControlsContextMessage();
                _this.sendCameraInfos();
                controlChanged = false;
            }
        }


        this.init = function () {
            _.bindAll(_this);
            initDOM();
            initControls();
            initLayerManager();
            initAxes();
            initGrid();
            initSelectionBox();
            initAmbientLight();
            initRenderer();
            initStats();
            window.addEventListener('resize', handleResize, false);
            bindMouseAndKeyEvents();
            // Choose here which controls are enabled at scene load
            createTransformControls();
            _this.setTrackBallControls();


            animate();
        };

        this.reDraw = function () {
            needsRedraw = true;
        };

        this.flyTo = function (mesh) {
            var boundingBox = mesh.geometry.boundingBox;
            var cog = new THREE.Vector3().copy(boundingBox.centroid).applyMatrix4(mesh.matrix);
            var size = boundingBox.size();
            var radius = Math.max(size.x, size.y, size.z);
            var camera = _this.cameraObject;
            var dir = new THREE.Vector3().copy(cog).sub(camera.position).normalize();
            var distance = radius ? radius * 2 : 1000;
            distance = distance < App.SceneOptions.cameraNear ? App.SceneOptions.cameraNear + 100 : distance;
            var endCamPos = new THREE.Vector3().copy(cog).sub(dir.multiplyScalar(distance));
            cameraAnimation(cog, 2000, endCamPos);
        };

        this.lookAt = function (mesh) {
            var boundingBox = mesh.geometry.boundingBox;
            var cog = new THREE.Vector3().copy(boundingBox.centroid).applyMatrix4(mesh.matrix);
            cameraAnimation(cog, 2000);
        };

        this.resetCameraPlace = function () {
            var camPos = App.SceneOptions.defaultCameraPosition;
            //cameraAnimation(new THREE.Vector3(0,0,0), 1000, camPos);
            resetCameraAnimation(new THREE.Vector3(0, 0, 0), 1000, camPos, new THREE.Vector3(0, 1, 0));

            //controlsObject.object.up = (0,1,0);
            //controlsObject.setCamUp(new THREE.Vector3(0,1,0));
        };
        /**
         * Context API
         */
        this.getSceneContext = function () {
            return {
                target: controlsObject.getTarget(),
                camPos: controlsObject.getCamPos()
            };
        };

        this.getControlsContext = function () {
            return {
                target: controlsObject.getTarget(),
                camPos: controlsObject.getCamPos(), // utiliser _this.cameraObject.position ??
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
            controlsObject.enabled = true;
        };

        this.disableControlsObject = function () {
            controlsObject.enabled = false;
        };

        /**
         * Scene option control
         */
        this.startMarkerCreationMode = function (layer) {
            _this.markerCreationMode = true;
            currentLayer = layer;
            _this.$sceneContainer.addClass("markersCreationMode");
        };
        this.stopMarkerCreationMode = function () {
            _this.markerCreationMode = false;
            currentLayer = null;
            _this.$sceneContainer.removeClass("markersCreationMode");
        };
        this.requestFullScreen = function () {
            _this.renderer.domElement.parentNode.requestFullscreen =
                (_this.renderer.domElement.parentNode.requestFullscreen) ||
                (_this.renderer.domElement.parentNode.mozRequestFullScreen) ||
                (_this.renderer.domElement.parentNode.webkitRequestFullScreen);
            _this.renderer.domElement.parentNode.requestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
        };
        this.explodeScene = function (v) {
            _this.sendExplodeValue(v);
            // this could be adjusted
            explosionCoeff = v * 0.1;
            _(_this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    applyExplosionCoeff(child);
                }
            });
            _this.reDraw();
        };
        this.setMeasureListener = function (callback) {
            _this.measureCallback = callback;
        };
        this.setMeasureState = function (state) {
            _this.$sceneContainer.toggleClass("measureMode", state);
            _this.measureState = state;
            var opacity = state ? 0.5 : 1;
            _(_this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    child.material.opacity = opacity;
                }
            });
        };
        this.takeScreenShot = function () {
            var now = new Date();
            var filename = APP_CONFIG.productId + "-" + now.getFullYear() + "-" + now.getMonth() + "-" + now.getDay();
            var pom = document.createElement('a');
            //pom.setAttribute('href', this.renderer.domElement.toDataURL('image/png'));
            pom.setAttribute('href', this.renderer.domElement.toDataURL('image/png'));
            pom.setAttribute('download', filename);
            pom.click();
        };
        this.setCameraNearFar = function (n, f) {
            _this.cameraObject.near = n;
            _this.cameraObject.far = f;
            _this.cameraObject.updateProjectionMatrix();
            _this.reDraw();

        };
        this.colourEditedMeshes = function () {
            editedMeshesColoured = true;
            _.each(_this.editedMeshes, function (y) {
                var mesh = meshesIndexed[y];
                mesh.material = materialEditedMesh;
            });
            _this.sendColourEditedMeshes();
            _this.reDraw();
        };
        this.cancelColourEditedMeshes = function () {
            editedMeshesColoured = false;
            _.each(_this.editedMeshes, function (y) {
                var mesh = meshesIndexed[y];
                mesh.material = mesh.initialMaterial;
            });
            _this.sendColourEditedMeshes();
            _this.reDraw();
        };
        this.setPointerLockControls = function () {
            if (_this.stateControl === _this.STATECONTROL.PLC || !browserSupportPointerLock) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.PLC;
            deleteAllControls();
            //updateControlsContext(_this.pointerLockControls,_this.pointerLockCamera);
            $('#flying_mode_view_btn').addClass("active");
            _this.$blocker.show();

            _this.cameraObject = _this.pointerLockCamera;
            controlsObject = _this.pointerLockControls;

            _this.pointerLockControls.addEventListener("change", onControlChange);

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

            $('#tracking_mode_view_btn').addClass("active");

            //updateControlsContext(_this.trackBallControls,_this.trackBallCamera);

            controlsObject = _this.trackBallControls;
            _this.cameraObject = _this.trackBallCamera;

            _this.trackBallControls.enabled = true;
            _this.trackBallControls.addEventListener("change", onControlChange);
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

            $('#orbit_mode_view_btn').addClass("active");

            //updateControlsContext(_this.orbitControls);

            controlsObject = _this.orbitControls;
            controlsObject.enabled = true;

            _this.cameraObject = _this.orbitCamera;

            controlsObject.addEventListener("change", onControlChange);
            controlsObject.bindEvents();
            _this.scene.add(_this.orbitCamera);

            handleResize();
            _this.reDraw();
        };
        this.setTransformControls = function (mesh, mode) {
            transformControls.setCamera(_this.cameraObject);
            controlsObject.enabled = false;
            transformControls.enabled = true;
            //transformControls.detach();
            transformControls.attach(mesh);
            if (!_.contains(_this.editedMeshes, mesh.uuid)) {
                _this.editedMeshes.push(mesh.uuid);
                if (editedMeshesColoured){
                    _this.colourEditedMeshes();
                }
                console.log("mesh added : ");
                console.log(this.editedMeshes);
                this.sendEditedMeshes();
            }
            transformControls.bindEvents();
            if (typeof(mode) !== 'undefined') {
                switch (mode) {
                    case "translate" :
                        transformControls.setMode("translate");
                        break;
                    case "rotate":
                        transformControls.setMode("rotate");
                        break;
                    case "scale":
                        transformControls.setMode("scale");
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
        this.cancelTransformation = function (mesh) {

            new TWEEN.Tween(mesh.position)
                .to({ x: mesh.initialPosition.x, y: mesh.initialPosition.y, z: mesh.initialPosition.z }, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            new TWEEN.Tween(mesh.rotation)
                .to({ x: mesh.initialRotation.x, y: mesh.initialRotation.y, z: mesh.initialRotation.z }, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            new TWEEN.Tween(mesh.scale)
                .to({ x: mesh.initialScale.x, y: mesh.initialScale.y, z: mesh.initialScale.z }, 2000)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reDraw();
                    transformControls.update();
                })
                .start();
            //_this.editedMeshes.splice(_.indexOf(_this.editedMeshes, mesh.uuid),1);
            _this.editedMeshes = _.without(_this.editedMeshes, mesh.uuid);
            mesh.material = mesh.initialMaterial;
            console.log("mesh removed");
            App.sceneManager.sendEditedMeshes();

            /*
             mesh.position.x = mesh.initialPosition.x;
             mesh.position.y = mesh.initialPosition.y;
             mesh.position.z = mesh.initialPosition.z;*/

            _this.reDraw();
            _this.leaveTransformMode();
        };

        /**
         * Collaborative mode
         */
        this.requestJoinRoom = function (key) {
            if (mainChannel.status !== ChannelStatus.OPENED) {
                // Retry to connect every 500ms
                console.log("Websocket is not yet connected. Retry in 500ms.");
                var _this = this;
                setTimeout(function () {
                    _this.requestJoinRoom(key);
                }, 500);
            } else {
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_JOIN,
                    key: key,
                    remoteUser: ""
                });
            }
        };

        this.joinRoom = function (key){
            App.collaborativeView.setRoomKey(key);
        };

        /**
         * Scene mouse events
         */
        this.setPathForIFrame = function (pathForIFrame) {
            _this.pathForIFrameLink = pathForIFrame;
        };
        this.clear = function () {
            App.instancesManager.clear();
        };

        this.removeMeshById = function (meshId) {
            removeMesh(meshId);
        };

    };

    return SceneManager;
});
