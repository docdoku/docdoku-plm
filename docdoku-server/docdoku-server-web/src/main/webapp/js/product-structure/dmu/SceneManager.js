/*global sceneManager,Instance,instancesManager*/
define([
    "views/marker_create_modal_view",
    "views/blocker_view"
], function (MarkerCreateModalView, BlockerView) {

    var SceneManager = function (pOptions) {

        var options = pOptions || {};

        var defaultsOptions = {
        };

        _.defaults(options, defaultsOptions);
        _.extend(this, options);

        this.isLoaded = false;
        this.isPaused = false;
        this.isMoving = false;

        this.defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
        this.cameraPosition = new THREE.Vector3(0, 10, 1000);

        this.STATECONTROL = { PLC: 0, TBC: 1};
        this.stateControl = this.STATECONTROL.TBC;
        this.time = Date.now();

        this.currentLayer = null;
        this.explosionCoeff = 0;
        this.wireframe = false;

        this.projector = new THREE.Projector();

        this.nominalFPSValue = 60; // 60 Fps
        this.currentFPSValue = 1; // Start the scene at 1 fps
        this.fpsReducerTime = 1000; // reducing fps every 1 seconds

    };

    SceneManager.prototype = {

        init: function () {
            _.bindAll(this);
            this.initScene();
            this.initSceneUtils();
            this.initRendering();
            this.bindMouseAndKeyEvents();
        },

        initScene: function () {
            this.$container = $('div#container');
            this.$sceneContainer = $("#scene_container");
            this.$blocker = new BlockerView().render().$el;
            this.$container.append(this.$blocker);
            this.scene = new THREE.Scene();
            this.frustum = new THREE.Frustum();
        },

        initSceneUtils: function () {
            this.initCamera();
            this.initControls();
            this.initAxes();
            this.initGrid();
            this.initCutPlan();
            this.initSelectionBox();
            this.initLights();
            this.initLayerManager();
        },

        initRendering: function () {
            this.initRenderer();
            this.initStats();
            this.animate();
            this.loadWindowResize();
            this.startFPSReducer();
        },

        initRenderer: function () {
            this.renderer = new THREE.WebGLRenderer();
            this.renderer.setSize(this.$container.width(), this.$container.height());
            this.$container.append(this.renderer.domElement);
        },

        animate: function () {
            // Clear timeout if any
            clearTimeout(this.animateTimout);

            var self = this;

            if (!this.isPaused) {
                switch (this.stateControl) {
                    case this.STATECONTROL.PLC:
                        this.cameraPosition = this.controls.getPosition();
                        this.controls.update(Date.now() - this.time);
                        this.time = Date.now();
                        break;
                    case this.STATECONTROL.TBC:
                        this.cameraPosition = this.camera.position;
                        this.controls.update();
                        break;
                }

                this.animateTimout = setTimeout(function () {
                    window.requestAnimationFrame(function () {
                        self.animate();
                    });
                }, 1000 / this.currentFPSValue);
            }

            this.render();
            this.stats.update();
        },

        render: function () {
            this.scene.updateMatrixWorld();
            this.renderer.render(this.scene, this.camera);
        },

        initCamera: function () {
            this.camera = new THREE.PerspectiveCamera(45, this.$container.width() / this.$container.height(), 1, 50000);
            if (!_.isUndefined(SCENE_INIT.camera)) {
                this.camera.position.set(SCENE_INIT.camera.x, SCENE_INIT.camera.y, SCENE_INIT.camera.z);
            }
            instancesManager.initWorker();
        },

        updateNewCamera: function () {
            // Remove camera from scene and save position
            if (this.stateControl == this.STATECONTROL.PLC) {
                this.cameraPosition = this.controls.getPosition();
                this.unbindPointerLock();
                this.scene.remove(this.controls.getObject());
            } else {
                this.cameraPosition = this.camera.position;
                this.scene.remove(this.camera);
            }

            this.initCamera();
            this.layerManager.updateCamera(this.camera, this.controls);
            this.addLightsToCamera();
        },

        initLayerManager: function () {
            if (!_.isUndefined(APP_CONFIG.productId)) {
                var self = this;
                require(["dmu/LayerManager"], function (LayerManager) {

                    if (self.stateControl == self.STATECONTROL.PLC) {
                        self.layerManager = new LayerManager(self.scene, self.controls.getObject(), self.renderer, self.controls, self.$container);
                        self.layerManager.domEvent._isPLC = true;
                    } else {
                        self.layerManager = new LayerManager(self.scene, self.camera, self.renderer, self.controls, self.$container);
                        self.layerManager.domEvent._isPLC = false;
                    }

                    self.layerManager.rescaleMarkers();
                    self.layerManager.renderList();
                });
            }
        },

        updateLayersManager: function () {
            if (this.stateControl == this.STATECONTROL.PLC) {
                this.layerManager.updateCamera(this.controls.getObject(), this.controls);
                this.layerManager.domEvent._isPLC = true;
            } else {
                this.layerManager.updateCamera(this.camera, this.controls);
                this.layerManager.domEvent._isPLC = false;
            }
        },

        setMeasureListener:function(callback){
            this.measureCallback = callback;
        },

        initLights: function () {
            var ambient = new THREE.AmbientLight(0x101030);
            this.scene.add(ambient);
            this.addLightsToCamera();
        },

        addLightsToCamera:function(){
            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            this.camera.add(dirLight);
            this.camera.add(dirLight.target);
        },

        initAxes: function () {
            var axes = new THREE.AxisHelper(100);
            axes.position.set(-1000, 0, 0);
            this.scene.add(axes);
            var arrow = new THREE.ArrowHelper(new THREE.Vector3(0, 1, 0), new THREE.Vector3(0, 0, 0), 100);
            arrow.position.set(200, 0, 400);
            this.scene.add(arrow);
        },

        initSelectionBox: function () {
            this.selectionBox = new THREE.BoxHelper();
            this.selectionBox.material.depthTest = true;
            this.selectionBox.material.transparent = true;
            this.selectionBox.visible = false;
            this.selectionBox.overdraw = true;
            this.scene.add(this.selectionBox);
        },

        setSelectionBoxOnMesh: function (mesh) {
            this.selectionBox.update(mesh);
            this.selectionBox.visible = true;
        },

        unsetSelectionBox: function () {
            this.selectionBox.visible = false;
        },

        initStats: function () {
            this.stats = new Stats();
            this.$sceneContainer.append(this.stats.domElement);

            this.$stats = $(this.stats.domElement);
            this.$stats.attr('id', 'statsWin');
            this.$stats.attr('class', 'statsWinMaximized');

            this.$statsArrow = $("<i id=\"statsArrow\" class=\"icon-chevron-down\"></i>");
            this.$stats.prepend(this.$statsArrow);

            var that = this;
            this.$statsArrow.bind('click', function () {
                that.$stats.toggleClass('statsWinMinimized statsWinMaximized');
            });
        },

        initGrid: function () {
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
            this.grid = new THREE.Line(geometry, material, THREE.LinePieces);
        },

        showGrid: function () {
            this.scene.add(this.grid);
        },

        removeGrid: function () {
            this.scene.remove(this.grid);
        },

        initCutPlan: function () {
            // Would be better to calculate the whole model bounding box to get en adapted plan size
            var size = 5000;
            var plane1 = new THREE.PlaneGeometry(size, size,1,1);
            this.cutPlan = new THREE.Mesh(plane1, new THREE.MeshBasicMaterial( { transparent: true, opacity: 0.5 } ));
            this.cutPlan.onScene=false;
            this.cutPlan.overdraw = true;
            this.cutPlan.material.side = THREE.DoubleSide;
            this.cutPlan.material.color = new THREE.Color(0xf5f5f5);
            this.cutPlan.initialPosition = {x:this.cutPlan.position.x,y:this.cutPlan.position.y,z:this.cutPlan.position.z};
            this.cutPlan.initialRotation = {x:this.cutPlan.rotation.x,y:this.cutPlan.rotation.y,z:this.cutPlan.rotation.z};
        },

        showCutPlan: function () {
            this.cutPlan.onScene=true;
            this.scene.add(this.cutPlan);
        },

        removeCutPlan: function () {
            this.cutPlan.onScene=false;
            this.scene.remove(this.cutPlan);
        },

        loadWindowResize: function () {
            THREEx.WindowResize(this.renderer, this.camera, this.$container);
        },

        /*
         *
         * Meshes
         *
         * */

        addMesh: function (mesh) {
            this.resetFPSReducer();
            this.scene.add(mesh);
            this.applyExplosionCoeff(mesh);
            this.applyWireFrame(mesh);
            this.applyMeasureStateOpacity(mesh);
        },

        removeMesh:function(mesh){
            this.resetFPSReducer();
            this.scene.remove(mesh);
        },

        bind: function (scope, fn) {
            return function () {
                fn.apply(scope, arguments);
            };
        },

        /*
         *
         * Scene mouse / keyboard controls
         *
         * */

        initControls: function () {
            switch (this.stateControl) {
                case this.STATECONTROL.PLC:
                    this.$blocker.show();
                    this.setPointerLockControls();
                    $('#flying_mode_view_btn').addClass("active");
                    break;
                case this.STATECONTROL.TBC:
                    this.$blocker.hide();
                    this.setTrackBallControls();
                    $('#tracking_mode_view_btn').addClass("active");
                    break;
            }
        },

        setPointerLockControls: function () {
            if (this.controls != null) {
                this.controls.destroyControl();
                this.controls = null;
            }

            var havePointerLock = 'pointerLockElement' in document || 'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;

            if (havePointerLock) {
                var self = this;
                var pointerLockChange = function (event) {
                    if (document.pointerLockElement === self.$container[0] || document.mozPointerLockElement === self.$container[0] || document.webkitPointerLockElement === self.$container[0]) {
                        self.controls.enabled = true;
                    } else {
                        self.controls.enabled = false;
                    }
                };

                // Hook pointer lock state change events
                document.addEventListener('pointerlockchange', pointerLockChange, false);
                document.addEventListener('mozpointerlockchange', pointerLockChange, false);
                document.addEventListener('webkitpointerlockchange', pointerLockChange, false);

                this.$container[0].addEventListener('dblclick', this.bindPointerLock, false);
            }

            this.controls = new THREE.PointerLockControlsCustom(this.camera, this.$container[0]);
            this.controls.moveToPosition(this.defaultCameraPosition);
            this.scene.add(this.controls.getObject());
            this.stateControl = this.STATECONTROL.PLC;
        },

        bindPointerLock: function (event) {

            this.$blocker.hide();

            // Ask the browser to lock the pointer
            this.$container[0].requestPointerLock = this.$container[0].requestPointerLock || this.$container[0].mozRequestPointerLock || this.$container[0].webkitRequestPointerLock;

            if (/Firefox/i.test(navigator.userAgent)) {

                document.addEventListener('fullscreenchange', this.onFullScreenChange, false);
                document.addEventListener('mozfullscreenchange', this.onFullScreenChange, false);

                this.$container[0].requestFullscreen = this.$container[0].requestFullscreen || this.$container[0].mozRequestFullscreen || this.$container[0].mozRequestFullScreen || this.$container[0].webkitRequestFullscreen;
                this.$container[0].requestFullscreen();

            } else {
                this.$container[0].requestPointerLock();
            }
        },


        unbindPointerLock: function () {
            this.$container[0].removeEventListener('dblclick', this.bindPointerLock, false);
            document.removeEventListener('fullscreenchange', this.onFullScreenChange, false);
            document.removeEventListener('mozfullscreenchange', this.onFullScreenChange, false);
        },

        setTrackBallControls: function () {

            if (this.controls != null) {
                this.controls.destroyControl();
                this.controls = null;
            }

            this.controls = new THREE.TrackballControlsCustom(this.camera, this.$container[0]);
            this.controls.initDefaultControl();

            this.controls.rotateSpeed = 3.0;
            this.controls.zoomSpeed = 10;
            this.controls.panSpeed = 1;

            this.controls.noZoom = false;
            this.controls.noPan = false;

            this.controls.staticMoving = true;
            this.controls.dynamicDampingFactor = 0.3;

            this.controls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];

            this.camera.position.set(this.defaultCameraPosition.x, this.defaultCameraPosition.y, this.defaultCameraPosition.z);
            this.scene.add(this.camera);

            this.stateControl = this.STATECONTROL.TBC;
        },

        /*
         *
         * Scene options control
         * */

        startMarkerCreationMode: function (layer) {
            this.markerCreationMode = true;
            this.currentLayer = layer;
            this.$sceneContainer.addClass("markersCreationMode");
        },

        stopMarkerCreationMode: function () {
            this.markerCreationMode = false;
            this.currentLayer = null;
            this.$sceneContainer.removeClass("markersCreationMode");
        },

        requestFullScreen: function () {
            this.renderer.domElement.parentNode.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        },

        onFullScreenChange: function (event) {
            if (document.fullscreenElement === this.$container[0] || document.mozFullscreenElement === this.$container[0] || document.mozFullScreenElement === this.$container[0]) {

                document.removeEventListener('fullscreenchange', this.onFullScreenChange);
                document.removeEventListener('mozfullscreenchange', this.onFullScreenChange);

                this.$container[0].requestPointerLock();
            }
        },

        switchWireFrame: function (wireframe) {
            this.resetFPSReducer();
            this.wireframe = wireframe;
            var self = this;
            _(this.scene.children).each(function (child) {
                if(child instanceof THREE.Mesh && child.partIterationId){
                    self.applyWireFrame(child);
                }
            });
        },

        applyWireFrame:function(mesh){
            mesh.material.wireframe = this.wireframe;
            if((mesh.material.materials)){
                _(mesh.material.materials).each(function (m) {
                    m.wireframe = mesh.material.wireframe;
                });
            }
        },

        explodeScene: function (v) {
            this.resetFPSReducer();
            var self = this;
            // this could be adjusted
            this.explosionCoeff = v * 0.1;
            _(this.scene.children).each(function (child) {
                if(child instanceof THREE.Mesh && child.partIterationId){
                    self.applyExplosionCoeff(child);
                }
            });
        },

        applyExplosionCoeff:function(mesh){

            if(!mesh.geometry.boundingBox){
                mesh.geometry.computeBoundingBox();
                mesh.geometry.computeBoundingSphere();
                mesh.geometry.boundingBox.centroid = new THREE.Vector3 (
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
            if (this.explosionCoeff != 0) {
                mesh.translateX(mesh.geometry.boundingBox.centroid.x * this.explosionCoeff);
                mesh.translateY(mesh.geometry.boundingBox.centroid.y * this.explosionCoeff);
                mesh.translateZ(mesh.geometry.boundingBox.centroid.z * this.explosionCoeff);
            }
            mesh.updateMatrix();
        },

        moveCutPlan:function(axis, value){
            this.resetFPSReducer();
            this.cutPlan.position.x = this.cutPlan.initialPosition.x;
            this.cutPlan.position.y = this.cutPlan.initialPosition.y;
            this.cutPlan.position.z = this.cutPlan.initialPosition.z;
            this.cutPlan.translateZ(value);
            var self = this ;
            _(this.scene.children).each(function(child){
                if(child instanceof THREE.Mesh && child.partIterationId){
                    child.visible = child.position.z < self.cutPlan.position.z ;
                }
            });
        },


        setCutPlanAxis:function(axis){
            this.resetFPSReducer();
            this.cutPlan.position.x = this.cutPlan.initialPosition.x;
            this.cutPlan.position.y = this.cutPlan.initialPosition.y;
            this.cutPlan.position.z = this.cutPlan.initialPosition.z;

            switch(axis){
                case 'X' :
                    this.cutPlan.rotation.set(0, 0, 0);
                    break;
                case 'Y' :
                    this.cutPlan.rotation.set( Math.PI/2, Math.PI/2, Math.PI/2);
                    break;
                case 'Z' :
                    this.cutPlan.rotation.set( Math.PI/2, 0, 0);
                    break;
                default:break;
            }
        },

        fitView:function(){

            // compute center of gravity and place camera point.
            var combined = new THREE.Geometry();

            _(this.scene.children).each(function(child){
                if(child instanceof THREE.Mesh && child.partIterationId){
                    THREE.GeometryUtils.merge(combined, child.geometry);
                }
            });
            combined.computeBoundingSphere();
            this.controls.target = combined.boundingSphere.center;

        },

        setMeasureState:function(state){
            this.$sceneContainer.toggleClass("measureMode",state);
            this.measureState = state;
            var opacity =  state ? 0.5 : 1;
            _(this.scene.children).each(function (child) {
                if(child instanceof THREE.Mesh && child.partIterationId){
                    child.material.opacity = opacity;
                }
            });
        },

        applyMeasureStateOpacity:function(mesh){
            if(this.measureState){
                mesh.material.opacity = this.measureState ? 0.5 : 1;
            }
        },

        /*
         * FPS Reducer
         *
         * */

        pause: function () {
            this.isPaused = true;
        },

        resume: function () {
            this.isPaused = false;
            this.animate();
        },

        startFPSReducer: function () {
            this.needsReduceFPS = true;
            var self = this;
            this.fpsReducer = setInterval(function () {
                if (self.needsReduceFPS && self.currentFPSValue > 1) {
                    self.reduceFPS();
                }
            }, this.fpsReducerTime);
        },

        resetFPSReducer: function () {
            this.needsReduceFPS = true;
            clearInterval(this.fpsReducer);
            this.resetFPSValue();
            this.startFPSReducer();
        },

        stopFPSReducer: function () {
            this.currentFPSValue = this.nominalFPSValue;
            this.needsReduceFPS = false;
            this.animate();
        },

        reduceFPS: function () {
            this.currentFPSValue = Math.ceil(this.currentFPSValue / 2);
        },

        resetFPSValue: function () {
            this.currentFPSValue = this.nominalFPSValue;
        },

        /*
         *
         *  Scene mouse events
         *
         * */

        bindMouseAndKeyEvents: function () {
            this.$container[0].addEventListener('mousedown', this.onMouseDown, false);
            this.$container[0].addEventListener('mouseup', this.onSceneMouseUp, false);
            this.$container[0].addEventListener('mouseover', this.onMouseEnter, false);
            this.$container[0].addEventListener('mouseout', this.onMouseLeave, false);
            this.$container[0].addEventListener('keydown', this.onKeyDown, false);
            this.$container[0].addEventListener('keyup', this.onKeyUp, false);
            this.$container[0].addEventListener('mousemove', this.onSceneMouseMove, false);
            this.$container[0].addEventListener('mousewheel', this.onSceneMouseWheel, false);
        },

        onMouseEnter: function () {
            this.resetFPSReducer();
        },

        onMouseLeave: function () {
        },

        onKeyDown: function () {
            this.preventInstancesUpdate();
            this.resetFPSReducer();
        },

        onKeyUp: function () {
            if(this.isMoving){
                this.needsInstancesUpdate();
                this.resetFPSReducer();
            }
        },

        onMouseDown: function () {
            this.preventInstancesUpdate();
            this.resetFPSReducer();
            this.isMoving = false;
        },

        onSceneMouseWheel: function () {
            this.needsInstancesUpdate();
            this.resetFPSReducer();
        },

        onSceneMouseMove: function () {
            this.resetFPSReducer();
            this.isMoving = true;
        },

        onSceneMouseUp: function (event) {

            if (this.isMoving) {
                this.needsInstancesUpdate();
                this.resetFPSReducer();
                return false;
            }

            event.preventDefault();

            // RayCaster to get the clicked mesh
            var vector = new THREE.Vector3(
                ((event.clientX - this.$container.offset().left) / this.$container[0].offsetWidth ) * 2 - 1,
                -((event.clientY - this.$container.offset().top) / this.$container[0].offsetHeight ) * 2 + 1,
                0.5
            );

            var cameraPosition;
            if (this.stateControl == this.STATECONTROL.PLC) {
                this.projector.unprojectVector(vector, this.controls.getObject().children[0].children[0]);
                cameraPosition = this.controls.getObject().position;

            } else {
                this.projector.unprojectVector(vector, this.camera);
                cameraPosition = this.camera.position;
            }

            var ray = new THREE.Raycaster(cameraPosition, vector.sub(cameraPosition).normalize());
            var intersects = ray.intersectObjects(this.scene.children, false);

            if (intersects.length > 0 && intersects[0].object.partIterationId) {
                if (this.markerCreationMode) {
                    var mcmv = new MarkerCreateModalView({model: this.currentLayer, intersectPoint: intersects[0].point});
                    $("body").append(mcmv.render().el);
                    mcmv.openModal();
                }
                else if(this.measureState){
                    this.measureCallback(intersects[0].point.clone());
                }
                else {
                    this.setSelectionBoxOnMesh(intersects[0].object);
                    Backbone.Events.trigger("mesh:selected", intersects[0].object);
                }
            } else {
                this.unsetSelectionBox();
                if(this.measureState){
                    this.measureCallback(-1);
                }
            }

        },

        needsInstancesUpdate:function(){
            this.preventInstancesUpdate();
            this.updateTimer = setTimeout(function(){
                instancesManager.updateWorker();
            },500);
        },

        preventInstancesUpdate:function(){
            clearTimeout(this.updateTimer);
        },

        setPathForIFrame: function (pathForIFrame) {
            this.pathForIFrameLink = pathForIFrame;
        }

    };

    return SceneManager;
});
