/*global sceneManager,Instance,instancesManager*/
define([
    "views/marker_create_modal_view",
    "views/blocker_view",
    "dmu/LayerManager"
], function (MarkerCreateModalView, BlockerView, LayerManager) {

    var havePointerLock = 'pointerLockElement' in document || 'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;
    var needsReframe = false;

    var SceneManager = function (pOptions) {

        var options = pOptions || {};
        _.extend(this, options);

        this.isMoving = false;
        this.defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
        this.cameraPosition = new THREE.Vector3(0, 10, 1000);
        this.STATECONTROL = { PLC: 0, TBC: 1};
        this.stateControl = null;
        this.time = Date.now();
        this.currentLayer = null;
        this.explosionCoeff = 0;
        this.wireframe = false;
        this.scene = new THREE.Scene();
        this.projector = new THREE.Projector();
        // Switching controls means different camera management
        this.controlsObject = null;
        // Represent the eye
        this.cameraObject = null;

    };

    SceneManager.prototype = {

        init: function () {

            var self = this;

            _.bindAll(this);

            this.initDOM();
            this.initControls();
            this.initLayerManager();
            this.initAxes();
            this.initGrid();
            this.initSelectionBox();
            this.initAmbientLight();
            this.initRenderer();
            this.initStats();
            this.loadWindowResize();
            this.bindMouseAndKeyEvents();

            // Choose here which controls are enabled at scene load
            this.setTrackBallControls();

            var t = Date.now();

            var animate = function () {

                requestAnimationFrame(animate);
                self.controlsObject.update(Date.now() - t);
                t = Date.now();
                self.stats.update();
                instancesManager.dequeue();
                // Sometimes needs a reFrame
                if(needsReframe){
                    self.render();
                    needsReframe = false;
                }

            };

            setInterval(function () {
                var target =  self.controlsObject.getTarget();
                var camPos =  self.controlsObject.getCamPos();
                instancesManager.updateWorker(camPos, target);
            }, 200);

            animate();

        },

        reFrame:function(){
            needsReframe = true;
        },

        render: function () {
            this.scene.updateMatrixWorld();
            this.renderer.render(this.scene, this.cameraObject);
        },

        initDOM: function () {
            this.$container = $('div#container');
            this.$container[0].setAttribute( 'tabindex', -1 );
            this.$sceneContainer = $("#scene_container");
            this.$blocker = new BlockerView().render().$el;
            this.$container.append(this.$blocker);
        },

        initRenderer: function () {
            this.renderer = new THREE.WebGLRenderer({preserveDrawingBuffer: true, alpha: true});
            this.renderer.setSize(this.$container.width(), this.$container.height());
            this.$container.append(this.renderer.domElement);
        },

        initLayerManager: function () {
            if (!_.isUndefined(APP_CONFIG.productId)) {
                this.layerManager = new LayerManager();
                this.layerManager.rescaleMarkers();
                this.layerManager.renderList();
            }
        },

        setMeasureListener: function (callback) {
            this.measureCallback = callback;
        },

        initAmbientLight: function () {
            var ambient = new THREE.AmbientLight(0x101030);
            this.scene.add(ambient);
        },

        addLightsToCamera: function (camera) {
            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            camera.add(dirLight);
            camera.add(dirLight.target);
        },

        initAxes: function () {
            var axes = new THREE.AxisHelper(100);
            axes.position.set(0, 0, 0);
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
            this.reFrame();
        },

        removeGrid: function () {
            this.scene.remove(this.grid);
            this.reFrame();
        },

        loadWindowResize: function () {
            THREEx.WindowResize(this.renderer, this.cameraObject, this.$container);
        },

        /*
         *
         * Meshes
         *
         * */

        addMesh: function (mesh) {
            mesh.initialPosition = {x:mesh.position.x,y:mesh.position.y,z:mesh.position.z};
            this.scene.add(mesh);
            this.applyExplosionCoeff(mesh);
            this.applyWireFrame(mesh);
            this.applyMeasureStateOpacity(mesh);
            this.reFrame();
        },

        removeMesh: function (mesh) {
            this.scene.remove(mesh);
            this.reFrame();
        },

        bind: function (scope, fn) {
            return function () {
                fn.apply(scope, arguments);
            };
        },

        /*
         *
         * Controls management
         *
         * */

        initControls: function () {
            this.createPointerLockControls();
            this.createTrackBallControls();
        },

        setPointerLockControls: function () {

            if (this.stateControl == this.STATECONTROL.PLC) {
                return;
            }

            this.stateControl = this.STATECONTROL.PLC;

            // Remove trackball controls
            this.trackBallControls.removeEventListener("change", this.render);
            this.scene.remove(this.trackBallCamera);
            this.trackBallControls.enabled = false;

            this.scene.remove(this.camera);

            $('#flying_mode_view_btn').addClass("active");
            this.$blocker.show();

            this.cameraObject = this.pointerLockCamera;
            this.controlsObject = this.pointerLockControls;

            this.pointerLockControls.addEventListener("change", this.render);
            this.scene.add(this.pointerLockControls.getObject());

        },

        setTrackBallControls: function () {

            if (this.stateControl == this.STATECONTROL.TBC) {
                return;
            }

            this.stateControl = this.STATECONTROL.TBC;

            // Remove pointerLock controls
            this.pointerLockControls.removeEventListener("change", this.render);
            this.scene.remove(this.pointerLockControls.getObject());
            this.pointerLockControls.enabled = false;

            $('#tracking_mode_view_btn').addClass("active");
            this.$blocker.hide();

            this.controlsObject = this.trackBallControls;
            this.cameraObject = this.trackBallCamera;

            this.trackBallControls.enabled = true;
            this.trackBallControls.addEventListener("change", this.render);

            this.scene.add(this.trackBallCamera);

        },

        createPointerLockControls: function () {

            if (havePointerLock) {
                var self = this;
                var pointerLockChange = function (event) {
                    if (document.pointerLockElement === self.$container[0] || document.mozPointerLockElement === self.$container[0] || document.webkitPointerLockElement === self.$container[0]) {
                        self.pointerLockControls.enabled = true;
                    } else {
                        self.pointerLockControls.enabled = false;
                    }
                };
                // Hook pointer lock state change events
                document.addEventListener('pointerlockchange', pointerLockChange, false);
                document.addEventListener('mozpointerlockchange', pointerLockChange, false);
                document.addEventListener('webkitpointerlockchange', pointerLockChange, false);
                this.$container[0].addEventListener('dblclick', this.bindPointerLock, false);
            }

            this.pointerLockCamera = new THREE.PerspectiveCamera(45, this.$container.width() / this.$container.height(), 1, 50000);
            this.pointerLockControls = new THREE.PointerLockControls(this.pointerLockCamera);
            this.addLightsToCamera(this.pointerLockCamera);
        },

        bindPointerLock: function (event) {

            if (this.stateControl != this.STATECONTROL.PLC) {
                return;
            }

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

        createTrackBallControls: function () {
            this.trackBallCamera = new THREE.PerspectiveCamera(45, this.$container.width() / this.$container.height(), 1, 50000);
            this.trackBallCamera.position.set(this.defaultCameraPosition.x, this.defaultCameraPosition.y, this.defaultCameraPosition.z);
            this.addLightsToCamera(this.trackBallCamera);
            this.trackBallControls = new THREE.TrackballControls(this.trackBallCamera, this.$container[0]);
            this.trackBallControls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];
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
            this.wireframe = wireframe;
            var self = this;
            _(this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    self.applyWireFrame(child);
                }
            });
            this.reFrame();
        },

        applyWireFrame: function (mesh) {
            mesh.material.wireframe = this.wireframe;
            if ((mesh.material.materials)) {
                _(mesh.material.materials).each(function (m) {
                    m.wireframe = mesh.material.wireframe;
                });
            }
        },

        explodeScene: function (v) {
            var self = this;
            // this could be adjusted
            this.explosionCoeff = v * 0.1;
            _(this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    self.applyExplosionCoeff(child);
                }
            });
            this.reFrame()
        },

        applyExplosionCoeff: function (mesh) {

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
            if (this.explosionCoeff != 0) {
                mesh.translateX(mesh.geometry.boundingBox.centroid.x * this.explosionCoeff);
                mesh.translateY(mesh.geometry.boundingBox.centroid.y * this.explosionCoeff);
                mesh.translateZ(mesh.geometry.boundingBox.centroid.z * this.explosionCoeff);
            }
            mesh.updateMatrix();
        },

        setMeasureState: function (state) {
            this.$sceneContainer.toggleClass("measureMode", state);
            this.measureState = state;
            var opacity = state ? 0.5 : 1;
            _(this.scene.children).each(function (child) {
                if (child instanceof THREE.Mesh && child.partIterationId) {
                    child.material.opacity = opacity;
                }
            });
        },

        applyMeasureStateOpacity: function (mesh) {
            if (this.measureState) {
                mesh.material.opacity = this.measureState ? 0.5 : 1;
            }
        },

        takeScreenShot: function () {
            var now = new Date();
            var filename = APP_CONFIG.productId + "-" + now.getFullYear() + "-" + now.getMonth() + "-" + now.getDay();
            var pom = document.createElement('a');
            //pom.setAttribute('href', this.renderer.domElement.toDataURL('image/png'));
            pom.setAttribute('href', this.renderer.domElement.toDataURL('image/png'));
            pom.setAttribute('download', filename);
            pom.click();
        },

        /*
         *
         *  Scene mouse events
         *
         * */

        bindMouseAndKeyEvents: function () {
            this.$container[0].addEventListener('mousedown', this.onMouseDown, false);
            this.$container[0].addEventListener('mouseup', this.onMouseUp, false);
            this.$container[0].addEventListener('mouseover', this.onMouseEnter, false);
            this.$container[0].addEventListener('mouseout', this.onMouseLeave, false);
            this.$container[0].addEventListener('keydown', this.onKeyDown, false);
            this.$container[0].addEventListener('keyup', this.onKeyUp, false);
            this.$container[0].addEventListener('mousemove', this.onSceneMouseMove, false);
            this.$container[0].addEventListener('mousewheel', this.onSceneMouseWheel, false);
        },

        onMouseEnter: function () {
        },

        onMouseLeave: function () {
        },

        onKeyDown: function () {
        },

        onKeyUp: function () {
        },

        onMouseDown: function () {
            this.isMoving = false;
        },

        onSceneMouseWheel: function () {
        },

        onSceneMouseMove: function () {
            this.isMoving = true;
        },

        onMouseUp: function (event) {

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

            var cameraPosition = this.controlsObject.getObject().position;
            var object = this.cameraObject;

            this.projector.unprojectVector(vector, object);

            var ray = new THREE.Raycaster(cameraPosition, vector.sub(cameraPosition).normalize());
            var intersects = ray.intersectObjects(this.scene.children, false);

            if (intersects.length > 0 && intersects[0].object.partIterationId) {
                if (this.markerCreationMode) {
                    var mcmv = new MarkerCreateModalView({model: this.currentLayer, intersectPoint: intersects[0].point});
                    $("body").append(mcmv.render().el);
                    mcmv.openModal();
                }
                else if (this.measureState) {
                    this.measureCallback(intersects[0].point.clone());
                }
                else {
                    this.setSelectionBoxOnMesh(intersects[0].object);
                    Backbone.Events.trigger("mesh:selected", intersects[0].object);
                }
            }
            else if (intersects.length > 0 && intersects[0].object.markerId) {
                this.layerManager.onMarkerClicked(intersects[0].object.markerId);
            }
            else {
                Backbone.Events.trigger("selection:reset");
                this.unsetSelectionBox();

                if (this.measureState) {
                    this.measureCallback(-1);
                }
            }

            this.reFrame();
        },

        setPathForIFrame: function (pathForIFrame) {
            this.pathForIFrameLink = pathForIFrame;
        },

        clear: function () {
            instancesManager.clear();
        }

    };

    return SceneManager;
});
