/*global sceneManager,Instance*/
define([
    "views/marker_create_modal_view",
    "views/blocker_view",
    "dmu/LoaderManager"
], function (MarkerCreateModalView, BlockerView, LoaderManager) {

    var SceneManager = function (pOptions) {

        var options = pOptions || {};

        var defaultsOptions = {
        };

        _.defaults(options, defaultsOptions);
        _.extend(this, options);

        this.isLoaded = false;
        this.isPaused = false;
        this.isMoving = false;

        this.updateOffset = 0;
        this.updateCycleLength = 250;

        this.instances = [];
        this.instancesMap = {};
        this.partIterations = {};

        this.defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
        this.cameraPosition = new THREE.Vector3(0, 10, 1000);

        this.STATECONTROL = { PLC: 0, TBC: 1};
        this.stateControl = this.STATECONTROL.TBC;
        this.time = Date.now();

        this.maxInstanceDisplayed = 1000;
        this.levelGeometryValues = [];

        this.loaderManager = null;
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
            this.updateLevelGeometryValues(0);
            this.initScene();
            this.initSceneUtils();
            this.initRendering();
            this.bindMouseAndKeyEvents();
        },

        initScene: function () {
            this.$container = $('div#container');
            this.$sceneContainer = $("#scene_container")
            this.$blocker = new BlockerView().render().$el;
            this.$container.append(this.$blocker);
            this.scene = new THREE.Scene();
        },

        initSceneUtils: function () {
            this.initCamera();
            this.initControls();
            this.initAxes();
            this.initGrid();
            this.initSelectionBox();
            this.initLights();
            this.initLayerManager();
            this.initLoaderManager();
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

        initLoaderManager: function () {
            this.loaderManager = new LoaderManager();
            this.loaderManager.listenXHR();
        },

        animate: function () {
            var self = this;
            // Clear timeout if any
            clearTimeout(this.animateTimout);
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
            this.updateInstances();
            this.scene.updateMatrixWorld();
            this.renderer.render(this.scene, this.camera);
        },

        initCamera: function () {
            this.camera = new THREE.PerspectiveCamera(45, this.$container.width() / this.$container.height(), 1, 50000);
            if (!_.isUndefined(SCENE_INIT.camera)) {
                console.log(SCENE_INIT.camera.x + ' , ' + SCENE_INIT.camera.y + ' , ' + SCENE_INIT.camera.z);
                this.camera.position.set(SCENE_INIT.camera.x, SCENE_INIT.camera.y, SCENE_INIT.camera.z);
            }
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
            if (APP_CONFIG.productId) {
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

        initLights: function () {
            var ambient = new THREE.AmbientLight(0x101030);
            this.scene.add(ambient);

            this.addLightsToCamera();
        },

        addLightsToCamera: function () {
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
            this.selectionBox.material.depthTest = false;
            this.selectionBox.material.transparent = true;
            this.selectionBox.visible = false;
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

        showStats: function () {
            this.$stats.show();
        },

        hideStats: function () {
            this.$stats.hide();
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

        loadWindowResize: function () {
            var windowResize = THREEx.WindowResize(this.renderer, this.camera, this.$container);
        },

        /*
         *
         * Mesh, instances, partIterations
         *
         * */

        addMesh: function (mesh) {

            this.scene.add(mesh);

            if (this.explosionCoeff != 0) {
                mesh.translateX(mesh.geometry.boundingBox.centroid.x * this.explosionCoeff);
                mesh.translateY(mesh.geometry.boundingBox.centroid.y * this.explosionCoeff);
                mesh.translateZ(mesh.geometry.boundingBox.centroid.z * this.explosionCoeff);
            }

            mesh.updateMatrix();

            this.resetFPSReducer();
        },

        clear: function () {
            for (var instance in this.instances) {
                sceneManager.instances[instance].clearMeshAndLevelGeometry();
                delete this.instancesMap[instance];
            }
            this.instances = [];
            this.instancesMap = {};
        },

        updateInstances: function () {

            var frustum = new THREE.Frustum();
            var projScreenMatrix = new THREE.Matrix4();
            projScreenMatrix.multiplyMatrices(this.camera.projectionMatrix, this.camera.matrixWorldInverse);
            frustum.setFromMatrix(projScreenMatrix);

            var updateIndex = Math.min((this.updateOffset + this.updateCycleLength), this.instances.length);
            for (var j = this.updateOffset; j < updateIndex; j++) {
                this.instances[j].update(frustum);
            }
            if (updateIndex < this.instances.length) {
                this.updateOffset = updateIndex;
            } else {
                this.updateOffset = 0;
            }
        },

        addPartIteration: function (partIteration) {
            this.partIterations[partIteration.partIterationId] = partIteration;
        },

        getPartIteration: function (partIterationId) {
            return this.partIterations[partIterationId];
        },

        hasPartIteration: function (partIterationId) {
            return _.has(this.partIterations, partIterationId);
        },

        addInstanceOnScene: function (instance) {
            this.instancesMap[instance.id] = instance;
            sceneManager.instances.push(instance);
        },

        removeInstanceFromScene: function (instanceId) {
            var numbersOfInstances = sceneManager.instances.length;

            var index = null;

            for (var j = 0; j < numbersOfInstances; j++) {
                if (sceneManager.instances[j].id == instanceId) {
                    index = j;
                    break;
                }
            }

            if (index != null) {
                sceneManager.instances[j].clearMeshAndLevelGeometry();
                sceneManager.instances.splice(index, 1);
                delete this.instancesMap[instanceId];
            }
        },

        isOnScene: function (instanceId) {
            return _.has(this.instancesMap, instanceId);
        },


        bind: function (scope, fn) {
            return function () {
                fn.apply(scope, arguments);
            };
        },


        updateLevelGeometryValues: function (instanceNumber) {
            if (instanceNumber < this.maxInstanceDisplayed) {
                this.levelGeometryValues[0] = 0.1;
                this.levelGeometryValues[1] = 0;
            } else {
                this.levelGeometryValues[0] = 0.15;
                this.levelGeometryValues[1] = 0.08;
            }
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
            // Set/remove wireFrame to current parts
            var self = this;
            _(this.instances).each(function (instance) {
                if (instance.levelGeometry != null && instance.levelGeometry.mesh != null) {
                    if (instance.levelGeometry.mesh.material) {
                        instance.levelGeometry.mesh.material.wireframe = self.wireframe;
                        _(instance.levelGeometry.mesh.material.materials).each(function (material) {
                            material.wireframe = self.wireframe;
                        });
                    }
                }
            });
        },

        explodeScene: function (v) {
            this.resetFPSReducer();
            var self = this;
            // this could be adjusted
            this.explosionCoeff = v * 0.1;
            _(this.instances).each(function (instance) {
                if (instance.levelGeometry != null && instance.levelGeometry.mesh != null) {
                    var mesh = instance.levelGeometry.mesh;
                    // Replace before translating
                    mesh.position.x = mesh.initialPosition.x;
                    mesh.position.y = mesh.initialPosition.y;
                    mesh.position.z = mesh.initialPosition.z;
                    // Translate instance
                    if (self.explosionCoeff != 0) {
                        mesh.translateX(mesh.geometry.boundingBox.centroid.x * self.explosionCoeff);
                        mesh.translateY(mesh.geometry.boundingBox.centroid.y * self.explosionCoeff);
                        mesh.translateZ(mesh.geometry.boundingBox.centroid.z * self.explosionCoeff);
                    }
                    mesh.updateMatrix();
                }
            });
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
            console.log("Reducing fps at " + Math.ceil(this.currentFPSValue / 2))
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
            this.$container[0].addEventListener('mousemove', this.onSceneMouseMove, false);
            this.$container[0].addEventListener('mousewheel', this.onSceneMouseWheel, false);
        },

        onMouseEnter: function () {
            this.resetFPSReducer();
        },

        onMouseLeave: function () {
            //this.resetFPSReducer();
        },

        onKeyDown: function () {
            this.resetFPSReducer();
        },

        onMouseDown: function () {
            this.resetFPSReducer();
            this.isMoving = false;
        },

        onSceneMouseWheel: function () {
            this.resetFPSReducer();
        },

        onSceneMouseMove: function () {
            this.resetFPSReducer();
            this.isMoving = true;
        },

        onSceneMouseUp: function (event) {
            this.resetFPSReducer();

            if (this.isMoving) {
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

            var intersectList = [];

            function buildIntersectList(sceneChild, list) {
                for (var i = 0, il = sceneChild.length; i < il; ++i) {
                    var obj = sceneChild[i];
                    list.push(obj);

                    if (obj.children.length > 0) {
                        buildIntersectList(obj.children, list);
                    }
                }
            }

            buildIntersectList(this.scene.children, intersectList);

            var intersects = ray.intersectObjects(intersectList, false);

            if (intersects.length > 0) {

                var intersectInstances = _.select(this.instancesMap, function (instance) {
                    return instance.levelGeometry == null ? false : instance.levelGeometry.mesh == intersects[0].object;
                });

                if (intersectInstances.length) {
                    if (this.markerCreationMode) {
                        // Marker creation
                        var intersectPoint = intersects[0].point;
                        var mcmv = new MarkerCreateModalView({model: this.currentLayer, intersectPoint: intersectPoint});
                        $("body").append(mcmv.render().el);
                        mcmv.openModal();
                    } else {
                        var instance = intersectInstances[0];
                        var mesh = instance.levelGeometry.mesh;
                        this.setSelectionBoxOnMesh(mesh);

                        // Part inspection
                        Backbone.Events.trigger("instance:selected", intersectInstances[0].partIteration);
                    }
                } else {
                    Backbone.Events.trigger("selection:reset");
                    this.unsetSelectionBox();
                }
            } else {
                Backbone.Events.trigger("selection:reset");
                this.unsetSelectionBox();
            }
        },

        setPathForIFrame: function (pathForIFrame) {
            this.pathForIFrameLink = pathForIFrame;
        }

    };

    return SceneManager;
});