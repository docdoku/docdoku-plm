/*global sceneManager*/
define([
    "views/marker_create_modal_view",
    "views/progress_bar_view",
    "views/blocker_view",
    "LoaderManager"
], function (
    MarkerCreateModalView,
    ProgressBarView,
    BlockerView,
    LoaderManager
) {
    var SceneManager = function (pOptions) {

        var options = pOptions || {};

        var defaultsOptions = {
            typeLoader: 'binary',
            typeMaterial: 'face'
        };

        _.defaults(options, defaultsOptions);
        _.extend(this, options);

        this.isLoaded = false;
        this.isPaused = false;

        this.material = (this.typeMaterial == 'face') ? new THREE.MeshFaceMaterial() : (this.typeMaterial == 'lambert') ? new THREE.MeshLambertMaterial() : new THREE.MeshNormalMaterial();

        this.updateOffset = 0;
        this.updateCycleLength = 250;

        this.instances = [];
        this.instancesMap = {};
        this.partIterations = {};
        this.meshesBindedForMarkerCreation = [];

        this.defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
        this.cameraPosition = new THREE.Vector3(0, 10, 1000);

        this.STATECONTROL = { PLC : 0, TBC : 1};
        this.stateControl = this.STATECONTROL.TBC;
        this.time = Date.now();

        this.maxInstanceDisplayed = 1000;
        this.levelGeometryValues = [];
        this.wireframe = false;

        this.loaderManager = null;
    };

    SceneManager.prototype = {

        init: function() {
            _.bindAll(this);
            this.updateLevelGeometryValues(0);
            this.initScene();
            this.listenXHR();
            this.initCamera();
            this.initControls();
            this.initLights();
            this.initAxes();
            this.initRenderer();
            this.initStats();
            this.loadWindowResize();
            this.initLayerManager();
            this.animate();
            this.initIframeScene();
            this.initGrid();
            this.isLoaded = true;
            this.loaderManager = new LoaderManager();
        },

        listenXHR: function() {

            // override xhr open prototype

            var pbv = new ProgressBarView().render();

            var xhrCount = 0;

            var _xhrOpen = XMLHttpRequest.prototype.open;

            XMLHttpRequest.prototype.open = function() {

                if(arguments[1].indexOf("/files/") === 0) {

                    var totalAdded = false,
                        totalLoaded = 0 ,
                        xhrLength = 0;

                    this.addEventListener("loadstart", function(pe) {
                        xhrCount++;
                    }, false);

                    this.addEventListener("progress", function(pe){

                        if(xhrLength == 0) {
                            xhrLength = pe.total;
                        }

                        if(totalAdded == false) {
                            pbv.addTotal(xhrLength);
                            totalAdded = true;
                        }

                        pbv.addLoaded(pe.loaded - totalLoaded);
                        totalLoaded = pe.loaded;

                    }, false);

                    this.addEventListener("loadend", function(){
                        xhrCount--;
                        setTimeout(function() {
                            pbv.removeXHRData(xhrLength);
                        }, 100);
                    }, false);
                }

                return _xhrOpen.apply(this, arguments);
            };
        },

        initScene: function() {
            this.$container = $('div#container');
            this.$blocker = new BlockerView().render().$el;
            this.$container.append(this.$blocker);
            this.$instructions = $('div#instructions');

            // Init frame
            if (this.$container.length === 0) {
                this.$container = $('div#frameContainer');
            }

            this.scene = new THREE.Scene();
        },

        initCamera: function() {
            this.camera = new THREE.PerspectiveCamera(45, this.$container.width() / this.$container.height(), 1, 50000);
            if (!_.isUndefined(SCENE_INIT.camera)) {
                console.log(SCENE_INIT.camera.x + ' , ' + SCENE_INIT.camera.y + ' , ' + SCENE_INIT.camera.z);
                this.camera.position.set(SCENE_INIT.camera.x, SCENE_INIT.camera.y, SCENE_INIT.camera.z);
            }

        },

        initControls: function() {

            switch(this.stateControl) {
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

        setPointerLockControls: function() {
            if(this.controls != null) {
                this.controls.destroyControl();
                this.controls = null;
            }

            var havePointerLock = 'pointerLockElement' in document || 'mozPointerLockElement' in document || 'webkitPointerLockElement' in document;

            if ( havePointerLock ) {
                var self = this;
                var pointerlockchange = function ( event ) {
                    if ( document.pointerLockElement === self.$container[0] || document.mozPointerLockElement === self.$container[0] || document.webkitPointerLockElement === self.$container[0] ) {
                        self.controls.enabled = true;
                    } else {
                        self.controls.enabled = false;
                    }
                };

                // Hook pointer lock state change events
                document.addEventListener( 'pointerlockchange', pointerlockchange, false );
                document.addEventListener( 'mozpointerlockchange', pointerlockchange, false );
                document.addEventListener( 'webkitpointerlockchange', pointerlockchange, false );

                this.$container[0].addEventListener( 'dblclick',  this.bindPointerLock , false );
            }

            this.controls = new THREE.PointerLockControlsCustom(this.camera, this.$container[0]);

            this.controls.moveToPosition(this.defaultCameraPosition);

            this.scene.add( this.controls.getObject() );

            this.stateControl = this.STATECONTROL.PLC;
        },

        bindPointerLock : function ( event ) {

            this.$blocker.hide();

            // Ask the browser to lock the pointer
            this.$container[0].requestPointerLock = this.$container[0].requestPointerLock || this.$container[0].mozRequestPointerLock || this.$container[0].webkitRequestPointerLock;

            if ( /Firefox/i.test( navigator.userAgent ) ) {

                document.addEventListener( 'fullscreenchange', this.fullscreenchange, false );
                document.addEventListener( 'mozfullscreenchange', this.fullscreenchange, false );

                this.$container[0].requestFullscreen = this.$container[0].requestFullscreen || this.$container[0].mozRequestFullscreen || this.$container[0].mozRequestFullScreen || this.$container[0].webkitRequestFullscreen;

                this.$container[0].requestFullscreen();

            } else {
                this.$container[0].requestPointerLock();
            }
        },

        // FullScreenChange for the PointerLockControl
        fullscreenchange : function ( event ) {

            if ( document.fullscreenElement === this.$container[0] || document.mozFullscreenElement === this.$container[0] || document.mozFullScreenElement === this.$container[0] ) {

                document.removeEventListener( 'fullscreenchange', this.fullscreenchange );
                document.removeEventListener( 'mozfullscreenchange', this.fullscreenchange );

                this.$container[0].requestPointerLock();
            }

        },

        unbindPointerLock : function() {
            this.$container[0].removeEventListener( 'dblclick', this.bindPointerLock , false );
            document.removeEventListener( 'fullscreenchange', this.fullscreenchange, false );
            document.removeEventListener( 'mozfullscreenchange', this.fullscreenchange, false );
        },

        setTrackBallControls: function() {

            if(this.controls != null) {
                this.controls.destroyControl();
                this.controls = null;
            }

            this.controls = new THREE.TrackballControlsCustom(this.camera, this.$container[0]);

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

        updateNewCamera: function() {
            // Best solution but setting rotation does not work (waiting for bug fix in Threejs)
            //this.camera.position.set(0, 10, 10000);
            //this.camera.rotation.set(0, 0 , 0);

            // Remove camera from scene and save position
            if(this.stateControl == this.STATECONTROL.PLC) {
                this.cameraPosition = this.controls.getPosition();
                this.unbindPointerLock();
                this.scene.remove(this.controls.getObject());
            } else {
                this.cameraPosition = this.camera.position;
                this.scene.remove(this.camera);
            }

            this.initCamera();
            this.addLightsToCamera();
        },

        updateLayersManager: function() {
            if(this.stateControl == this.STATECONTROL.PLC) {
                this.layerManager.updateCamera(this.controls.getObject(), this.controls);
                this.layerManager.domEvent._isPLC = true;
            } else {
                this.layerManager.updateCamera(this.camera, this.controls);
                this.layerManager.domEvent._isPLC = false;
            }
        },

        startMarkerCreationMode: function(layer) {

            $("#scene_container").addClass("markersCreationMode");

            var self = this;

            if(self.stateControl == self.STATECONTROL.PLC) {
                this.domEventForMarkerCreation = new THREEx.DomEvent(this.controls.getObject(), this.$container);
                this.domEventForMarkerCreation._isPLC = true;
            } else {
                this.domEventForMarkerCreation = new THREEx.DomEvent(this.camera, this.$container);
                this.domEventForMarkerCreation._isPLC = false;
            }

            var filteredInstances = _.filter(self.instances, function(instance) {
                return instance.levelGeometry != null &&  instance.levelGeometry.mesh != null;
            });

            this.meshesBindedForMarkerCreation = _.map(filteredInstances, function(instance) {
                    return instance.levelGeometry.mesh;
            });

            var onIntersect = function(intersectPoint) {
                var mcmv = new MarkerCreateModalView({model:layer, intersectPoint:intersectPoint});
                $("body").append(mcmv.render().el);
                mcmv.openModal();
            };

            var numbersOfMeshes = this.meshesBindedForMarkerCreation.length;

            for (var j = 0; j < numbersOfMeshes; j++) {
                self.domEventForMarkerCreation.bind(this.meshesBindedForMarkerCreation[j], 'click', function(e) {
                    onIntersect(e.target.intersectPoint);
                });
            }

        },

        stopMarkerCreationMode: function() {

            $("#scene_container").removeClass("markersCreationMode");

            $("#creationMarkersModal .btn-primary").off('click');
            var numbersOfMeshes = this.meshesBindedForMarkerCreation.length;
            for (var j = 0; j < numbersOfMeshes; j++) {
                this.domEventForMarkerCreation.unbind(this.meshesBindedForMarkerCreation[j], 'click');
            }
        },

        initLayerManager: function() {
            var self = this;
            require(["LayerManager"], function(LayerManager) {

                if(self.stateControl == self.STATECONTROL.PLC) {
                    self.layerManager = new LayerManager(self.scene, self.controls.getObject(), self.renderer, self.controls, self.$container);
                    self.layerManager.domEvent._isPLC = true;
                } else {
                    self.layerManager = new LayerManager(self.scene, self.camera, self.renderer, self.controls, self.$container);
                    self.layerManager.domEvent._isPLC = false;
                }

                self.layerManager.rescaleMarkers();
                self.layerManager.renderList();
            });
        },

        initLights: function() {
            var ambient = new THREE.AmbientLight(0x101030);
            this.scene.add(ambient);

            this.addLightsToCamera();
        },

        addLightsToCamera: function() {
            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            this.camera.add(dirLight);
            this.camera.add(dirLight.target);
        },

        initAxes: function() {
            var axes = new THREE.AxisHelper(100);
            axes.position.set(-1000, 0, 0);
            this.scene.add(axes);

            var arrow = new THREE.ArrowHelper(new THREE.Vector3(0, 1, 0), new THREE.Vector3(0, 0, 0), 100);
            arrow.position.set(200, 0, 400);
            this.scene.add(arrow);
        },

        showStats:function(){
            this.$stats.show();
        },

        hideStats:function(){
            this.$stats.hide();
        },

        initStats: function() {
            this.stats = new Stats();
            $("#scene_container").append(this.stats.domElement);

            this.$stats = $(this.stats.domElement);
            this.$stats.attr('id','statsWin');
            this.$stats.attr('class', 'statsWinMaximized');

            this.$statsArrow = $("<i id=\"statsArrow\" class=\"icon-chevron-down\"></i>");
            this.$stats.prepend(this.$statsArrow);

            var that = this;
            this.$statsArrow.bind('click', function() {
                that.$stats.toggleClass('statsWinMinimized statsWinMaximized');
            });
        },

        initRenderer: function() {
            this.renderer = new THREE.WebGLRenderer();
            this.renderer.setSize(this.$container.width(), this.$container.height());
            this.$container.append(this.renderer.domElement);
        },

        loadWindowResize: function() {
            var windowResize = THREEx.WindowResize(this.renderer, this.camera, this.$container);
        },

        pause:function(){
            this.isPaused = true;
        },

        resume:function(){
            this.isPaused = false;
            this.animate();
        },

        requestFullScreen:function(){
            this.renderer.domElement.parentNode.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        },

        animate: function() {
            var self = this;

            if(!this.isPaused){

                window.requestAnimationFrame(function() {
                    self.animate();
                });

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

            }

            this.render();
            this.stats.update();
        },

        render: function() {
            this.updateInstances();
            this.scene.updateMatrixWorld();
            this.renderer.render(this.scene, this.camera);
        },

        updateInstances: function() {

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

        addPartIteration: function(partIteration) {
            this.partIterations[partIteration.partIterationId] = partIteration;
        },

        getPartIteration: function(partIterationId) {
            return this.partIterations[partIterationId];
        },

        hasPartIteration: function(partIterationId) {
            return _.has(this.partIterations, partIterationId);
        },

        addInstanceOnScene: function(instance) {
            this.instancesMap[instance.id] = instance;
            sceneManager.instances.push(instance);
        },

        removeInstanceFromScene: function(instanceId) {
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

        isOnScene: function(instanceId) {
            return _.has(this.instancesMap, instanceId);
        },

        setPathForIframe: function(pathForIframe) {
            this.pathForIframeLink = pathForIframe;
        },

        initIframeScene: function() {
            if (!_.isUndefined(SCENE_INIT.pathForIframe)) {
                var self = this;
                var instancesUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec="+window.config_spec+"&path=" + SCENE_INIT.pathForIframe;
                $.getJSON(instancesUrl, function(instances) {
                    _.each(instances, function(instanceRaw) {

                        //do something only if this instance is not on scene
                        if (!self.isOnScene(instanceRaw.id)) {

                            //if we deal with this partIteration for the fist time, we need to create it
                            if (!self.hasPartIteration(instanceRaw.partIterationId)) {
                                self.addPartIteration(new self.PartIteration(instanceRaw));
                            }

                            var partIteration = self.getPartIteration(instanceRaw.partIterationId);

                            //finally we create the instance and add it to the scene
                            self.addInstanceOnScene(new Instance(
                                instanceRaw.id,
                                partIteration,
                                instanceRaw.tx * 10,
                                instanceRaw.ty * 10,
                                instanceRaw.tz * 10,
                                instanceRaw.rx,
                                instanceRaw.ry,
                                instanceRaw.rz
                            ));
                        }

                    });
                });
            }
        },

        bind: function ( scope, fn ) {
            return function () {
                fn.apply( scope, arguments );
            };
        },

        initGrid:function(){

            var size = 500, step = 25;
            var geometry = new THREE.Geometry();
            var material = new THREE.LineBasicMaterial( { vertexColors: THREE.VertexColors } );
            var color1 = new THREE.Color( 0x444444 ), color2 = new THREE.Color( 0x888888 );

            for ( var i = - size; i <= size; i += step ) {
                geometry.vertices.push( new THREE.Vector3( -size, 0, i ) );
                geometry.vertices.push( new THREE.Vector3(  size, 0, i ) );
                geometry.vertices.push( new THREE.Vector3( i, 0, -size ) );
                geometry.vertices.push( new THREE.Vector3( i, 0,  size ) );
                var color = i === 0 ? color1 : color2;
                geometry.colors.push( color, color, color, color );
            }

            this.grid = new THREE.Line( geometry, material, THREE.LinePieces );
        },

        showGrid:function() {
            this.scene.add( this.grid );
        },

        removeGrid:function() {
            this.scene.remove( this.grid );
        },

        updateLevelGeometryValues:function( instanceNumber ){
            if(instanceNumber < this.maxInstanceDisplayed) {
                this.levelGeometryValues[0] = 0.5;
                this.levelGeometryValues[1] = 0;
            } else {
                this.levelGeometryValues[0] = 0.7;
                this.levelGeometryValues[1] = 0.4;
            }
        },

        switchWireframe:function( wireframe ) {
            if(wireframe) {
                // Set wireframe to futures parts
                this.wireframe = true;
            } else {
                // Remove wireframe to futures parts
                this.wireframe = false;

            }

            // Set/remove wireframe to current parts
            var self = this;
            _(this.instances).each(function(instance){
                if(instance.levelGeometry != null && instance.levelGeometry.mesh != null){
                    _(instance.levelGeometry.mesh.material.materials).each(function(material){
                        material.wireframe = self.wireframe;
                    })
                }
            });

        },

        clear:function(){
            for(var instance in this.instances){
                sceneManager.instances[instance].clearMeshAndLevelGeometry();
                delete this.instancesMap[instance];
            }
            this.instances=[];
            this.instancesMap={};

        }//,
    /*
        cleanRootId:function(instance){
            if(instance.id.match(/^0\-.*//*)){
                instance.id = instance.id.substr(2,instance.id.length);
            }
            return instance;
        }
*/
    };

    return SceneManager;
});