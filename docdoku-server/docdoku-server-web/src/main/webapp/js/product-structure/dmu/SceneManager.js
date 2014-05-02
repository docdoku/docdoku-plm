/*global App,sceneManager,Instance,instancesManager,requestAnimationFrame*/
define([
    "views/marker_create_modal_view",
    "views/blocker_view",
    "dmu/LayerManager"
], function (MarkerCreateModalView, BlockerView, LayerManager) {

    var SceneManager = function (pOptions) {
        var _this = this;
        
        var havePointerLock = 'pointerLockElement' in document || 'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;
        var options = pOptions || {};
        _.extend(this, options);
        var isMoving = false;
        var defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
        var currentLayer = null;
        var explosionCoeff = 0;
        var wireframe = false;
        var projector = new THREE.Projector();
        var controlsObject = null;                                                                                      // Switching controls means different camera management
        var clock = new THREE.Clock();
        var needsReframe = false;
        var meshesIndexed={};
        var selectionBox = null;
        var meshMarkedForSelection = null;

        this.stateControl = null;
        this.STATECONTROL = { PLC: 0, TBC: 1};
        this.scene = new THREE.Scene();
        this.cameraObject = null;                                                                                       // Represent the eye
        this.layerManager = null;

        // Stat
        this.switches = 0;
        this.adds = 0;
        this.onScene = 0;

        function render(){
            _this.scene.updateMatrixWorld();
            _this.renderer.render(_this.scene, _this.cameraObject);
        }

        function initDOM() {
            _this.$container = $('div#container');
            _this.$container[0].setAttribute( 'tabindex', -1 );
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
            _this.$statsArrow = $("<i id=\"statsArrow\" class=\"icon-chevron-down\"></i>");
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
        function handleResize(){
            _this.cameraObject.aspect = _this.$container.innerWidth() / _this.$container.innerHeight();
            _this.cameraObject.updateProjectionMatrix();
            _this.renderer.setSize(_this.$container.innerWidth(),_this.$container.innerHeight());
            controlsObject.handleResize();
            _this.reFrame();
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
        function initControls() {
            createPointerLockControls();
            createTrackBallControls();
        }
        function pointerLockChange(){
            _this.pointerLockControls.enabled = (document.pointerLockElement === _this.$container[0]) ||
                                                (document.mozPointerLockElement === _this.$container[0]) ||
                                                (document.webkitPointerLockElement === _this.$container[0]);
        }
        function createPointerLockControls() {
            if (havePointerLock) {
                // Hook pointer lock state change events
                document.addEventListener('pointerlockchange', pointerLockChange, false);
                document.addEventListener('mozpointerlockchange', pointerLockChange, false);
                document.addEventListener('webkitpointerlockchange', pointerLockChange, false);
                _this.$container[0].addEventListener('dblclick', bindPointerLock, false);
            }

            _this.pointerLockCamera = new THREE.PerspectiveCamera(45, _this.$container.width() / _this.$container.height(), 1, 50000);
            _this.pointerLockControls = new THREE.PointerLockControls(_this.pointerLockCamera);
            addLightsToCamera(_this.pointerLockCamera);
        }
        function createTrackBallControls() {
            _this.trackBallCamera = new THREE.PerspectiveCamera(45, _this.$container.width() / _this.$container.height(), 1, 50000);
            _this.trackBallCamera.position.set(defaultCameraPosition.x, defaultCameraPosition.y, defaultCameraPosition.z);
            addLightsToCamera(_this.trackBallCamera);
            _this.trackBallControls = new THREE.TrackballControls(_this.trackBallCamera, _this.$container[0]);
            _this.trackBallControls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];
        }
        function bindPointerLock(){
            if (_this.stateControl != _this.STATECONTROL.PLC) {
                return;
            }
            _this.$blocker.hide();

            // Ask the browser to lock the pointer
            _this.$container[0].requestPointerLock = (_this.$container[0].requestPointerLock) ||
                                                     (_this.$container[0].mozRequestPointerLock) ||
                                                     (_this.$container[0].webkitRequestPointerLock);

            if (/Firefox/i.test(navigator.userAgent)) {
                document.addEventListener('fullscreenchange', onFullScreenChange, false);
                document.addEventListener('mozfullscreenchange', onFullScreenChange, false);

                _this.$container[0].requestFullscreen = (_this.$container[0].requestFullscreen) ||
                                                        (_this.$container[0].mozRequestFullscreen) ||
                                                        (_this.$container[0].mozRequestFullScreen) ||
                                                        (_this.$container[0].webkitRequestFullscreen);
                _this.$container[0].requestFullscreen();

            } else {
                _this.$container[0].requestPointerLock();
            }
        }

        /**
         * Scene options control
         */
        function onControlChange(){
            if(instancesManager){
                instancesManager.planNewEval();
            }
            _this.reFrame();
        }
        function onFullScreenChange(){
            if (document.fullscreenElement === _this.$container[0] ||
                document.mozFullscreenElement === _this.$container[0] ||
                document.mozFullScreenElement === _this.$container[0]) {

                document.removeEventListener('fullscreenchange', onFullScreenChange);
                document.removeEventListener('mozfullscreenchange', onFullScreenChange);

                _this.$container[0].requestPointerLock();
            }
        }
        function applyWireFrame(mesh) {
            mesh.material.wireframe = wireframe;
            if ((mesh.material.materials)) {
                _(mesh.material.materials).each(function (m) {
                    m.wireframe = mesh.material.wireframe;
                });
            }
        }
        function applyExplosionCoeff(mesh){
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
            if (explosionCoeff != 0) {
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
        function showGrid(){
            if (_this.grid.added) {
                return;
            }
            _this.grid.added = true;
            _this.scene.add(_this.grid);
            _this.reFrame();
        }
        function removeGrid () {
            if (!_this.grid.added) {
                return;
            }
            _this.grid.added = false;
            _this.scene.remove(_this.grid);
            _this.reFrame();
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
        function onMouseEnter() {}
        function onMouseLeave() {
            isMoving = false;
        }
        function onKeyDown() {}
        function onKeyUp () {}
        function onMouseDown () {
            isMoving = false;
        }
        function onSceneMouseWheel() {}
        function onSceneMouseMove () {
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
                Backbone.Events.trigger("selection:reset");
                meshMarkedForSelection=null;
                unsetSelectionBox();

                if (_this.measureState) {
                    _this.measureCallback(-1);
                }
            }
            _this.reFrame();
        }

        /**
         * Meshes
         */
        function removeMesh(meshId){
            var mesh = meshesIndexed[meshId];
            delete meshesIndexed[meshId];
            if(!mesh){return;}
            _this.scene.remove(mesh);
            mesh.geometry.dispose();
            mesh.material.dispose();
            mesh = null;
            _this.reFrame();
        }
        function processTrash () {
            var instanceList = instancesManager.getTrash();
            instanceList.forEach(function(instance){
                removeMesh(instance.id);
                instance.qualityLoaded=undefined;
            });
            instancesManager.clearTrash();
        }
        function processLoadedStuff () {
            var loadedStuff =instancesManager.getLoadedGeometries(10);
            loadedStuff.forEach(function(stuff){
                var instance = instancesManager.getInstance(stuff.id);
                var oldMesh = meshesIndexed[stuff.id];
                var newMesh = stuff.mesh;
                if(oldMesh){
                    _this.switches++;
                    _this.scene.remove(oldMesh);
                    oldMesh.geometry.dispose();
                    oldMesh.material.dispose();
                }else{
                    _this.adds++;
                    _this.onScene++;
                }
                meshesIndexed[newMesh.uuid]=newMesh;
                newMesh.initialPosition = {x:newMesh.position.x,y:newMesh.position.y,z:newMesh.position.z};
                _this.scene.add(newMesh);
                instance.qualityLoaded=stuff.quality;
                if(meshMarkedForSelection == stuff.id){
                    setSelectionBoxOnMesh(newMesh);
                }
                applyExplosionCoeff(newMesh);
                applyWireFrame(newMesh);
                applyMeasureStateOpacity(newMesh);
                _this.reFrame();
            });
        }

        /**
         *  Animation
         */
        /*function cameraAnimation(position,target,duration){
            var controls = controlsObject;
            var camera = controls.object;
            var curCamPos = camera.position;
            var curTar = controls.target;
            var endCamPos = position;
            var endTarPos = target;

            new TWEEN.Tween(curTar)
                .to({ x: endTarPos.x, y: endTarPos.y, z: endTarPos.z }, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reFrame();
                })
                .start();

            new TWEEN.Tween(curCamPos)
                .to({ x: endCamPos.x, y: endCamPos.y, z: endCamPos.z }, duration)
                .interpolation(TWEEN.Interpolation.CatmullRom)
                .easing(TWEEN.Easing.Quintic.InOut)
                .onUpdate(function () {
                    _this.reFrame();
                })
                .start();
        }*/

        /**
         * Animation loop :
         *  Update controls, scene objects and animations
         *  Render at the end
         * */
        //Main UI loop
        function animate() {
            requestAnimationFrame(animate,null);
            // Update controls
            controlsObject.update(clock.getDelta());                                                                // Update controls

            // Update scene elements
            if(instancesManager){
                processTrash();
                processLoadedStuff();
            }

            // Update with SceneOptions
            watchSceneOptions();

            // Sometimes needs a reFrame
            if(needsReframe){
                needsReframe = false;
                _this.stats.update();
                render();
            }
        }


        this.init = function (){
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
            _this.setTrackBallControls();
            animate();
        };
        this.setTrackBallControls = function(){
            if (_this.stateControl == _this.STATECONTROL.TBC) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.TBC;

            // Remove pointerLock controls
            _this.pointerLockControls.removeEventListener("change", onControlChange);
            _this.scene.remove(_this.pointerLockControls.getObject());
            _this.pointerLockControls.enabled = false;

            $('#tracking_mode_view_btn').addClass("active");
            _this.$blocker.hide();

            controlsObject = _this.trackBallControls;
            _this.cameraObject = _this.trackBallCamera;

            _this.trackBallControls.enabled = true;
            _this.trackBallControls.addEventListener("change", onControlChange);

            _this.scene.add(_this.trackBallCamera);
        };
        this.reFrame = function(){
            needsReframe = true;
        };
        /*this.placeCamera = function(cog,diameter) {
            var controls = controlsObject;
            var camera = controls.object;
            var dir = new THREE.Vector3().copy(cog).sub(camera.position).normalize();
            var distance = diameter ? diameter*2 : 1000;
            distance = distance < App.SceneOptions.cameraNear ? App.SceneOptions.cameraNear + 100 : distance;
            var endCamPos = new THREE.Vector3().copy(cog).sub(dir.multiplyScalar(distance));
            cameraAnimation(endCamPos,cog, 2000);
        };*/
        /**
         * Context API
         */
        this.getSceneContext = function(){
            return {
                target: controlsObject.getTarget(),
                camPos: controlsObject.getCamPos()
            };
        };

        /**
         * Controls management
         */
        this.setPointerLockControls = function() {
            if (_this.stateControl == _this.STATECONTROL.PLC) {
                return;
            }

            _this.stateControl = _this.STATECONTROL.PLC;

            // Remove trackball controls
            _this.trackBallControls.removeEventListener("change", _this.render);
            _this.scene.remove(this.trackBallCamera);
            _this.trackBallControls.enabled = false;

            _this.scene.remove(this.camera);

            $('#flying_mode_view_btn').addClass("active");
            _this.$blocker.show();

            _this.cameraObject = _this.pointerLockCamera;
            controlsObject = _this.pointerLockControls;

            _this.pointerLockControls.addEventListener("change", _this.render);
            _this.scene.add(_this.pointerLockControls.getObject());
        };

        /**
         * Scene option control
         */
        this.startMarkerCreationMode = function(layer){
            _this.markerCreationMode = true;
            currentLayer = layer;
            _this.$sceneContainer.addClass("markersCreationMode");
        };
        this.stopMarkerCreationMode = function() {
            _this.markerCreationMode = false;
            currentLayer = null;
            _this.$sceneContainer.removeClass("markersCreationMode");
        };
        this.requestFullScreen = function() {
            _this.renderer.domElement.parentNode.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        };
        this.switchWireFrame = function(pWireframe) {
            wireframe = pWireframe;
            _(_this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    applyWireFrame(child);
                }
            });
            _this.reFrame();
        };
        this.explodeScene = function(v) {
            // this could be adjusted
            explosionCoeff = v * 0.1;
            _(_this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    applyExplosionCoeff(child);
                }
            });
            _this.reFrame();
        };
        this.setMeasureListener = function(callback) {
            _this.measureCallback = callback;
        };
        this.setMeasureState = function (state){
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

        /**
         * Scene mouse events
         */
        this.setPathForIFrame = function(pathForIFrame){
            _this.pathForIFrameLink = pathForIFrame;
        };
        this.clear = function (){
            instancesManager.clear();
        };

    };

    return SceneManager;
});
