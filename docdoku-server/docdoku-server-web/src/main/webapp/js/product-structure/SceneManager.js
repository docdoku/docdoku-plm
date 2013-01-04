function SceneManager(options) {

    var options = options || {};

    var defaultsOptions = {
        typeLoader: 'binary',
        typeMaterial: 'face'
    }

    _.defaults(options, defaultsOptions);
    _.extend(this, options);

    this.loader = (this.typeLoader == 'binary') ? new THREE.BinaryLoader() : new THREE.JSONLoader();
    this.material = (this.typeMaterial == 'face') ? new THREE.MeshFaceMaterial() : (this.typeMaterial == 'lambert') ? new THREE.MeshLambertMaterial() : new THREE.MeshNormalMaterial();

    this.updateOffset = 0;
    this.updateCycleLength = 250;

    this.instances = [];
    this.instancesMap = {};
    this.partIterations = {}
    this.meshesBindedForMarkerCreation = [];
    this.clock = new THREE.Clock();
}

SceneManager.prototype = {

    init: function() {
        this.initExportScene();
        this.initScene();
        this.initCamera();
        this.initControls();
        this.initLights();
        this.initAxes();
        this.initStats();
        this.initRenderer();
        this.loadWindowResize();
        this.initLayerManager();
        this.initMarkersModal();
        this.animate();
        this.initIframeScene();
    },

    initExportScene: function() {

        var self = this;

        $("#export_scene_btn").click(function() {
            $("#exportSceneModal").modal('show');
        });

        $("#exportSceneModal").on("shown", function() {
            var iframeTextarea = $("#exportSceneModal .modal-body textarea");

            var splitUrl = window.location.href.split("/");
            var urlRoot = splitUrl[0] + "//" + splitUrl[2];

            var paths = self.rootCollection

            var iframeSrc = urlRoot + '/visualization/' + APP_CONFIG.workspaceId + '/' + APP_CONFIG.productId
                + '?cameraX=' + self.camera.position.x
                + '&cameraY=' + self.camera.position.y
                + '&cameraZ=' + self.camera.position.z
                + '&pathToLoad=' + self.pathForIframeLink;

            iframeTextarea.text('<iframe width="640" height="480" src="' + iframeSrc + '" frameborder="0"></iframe>');

            iframeTextarea.select();
        });

    },

    initScene: function() {
        this.container = $('div#container');
        // Init frame
        if (this.container.length === 0) {
            this.container = $('div#frameContainer');
        }
        this.scene = new THREE.Scene();
    },

    initCamera: function(position) {
        this.camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 1, 50000);
        if (!_.isUndefined(SCENE_INIT.camera)) {
            console.log(SCENE_INIT.camera.x + ' , ' + SCENE_INIT.camera.y + ' , ' + SCENE_INIT.camera.z)
            this.camera.position.set(SCENE_INIT.camera.x, SCENE_INIT.camera.y, SCENE_INIT.camera.z);
        } else
            this.camera.position.set(0, 10, 10000);
        this.scene.add(this.camera);
    },

    initControls: function() {
        this.controls = new THREE.FirstPersonControlsCustom(this.camera, this.container[0]);
        //this.controls = new THREE.TrackballControlsCustom(this.camera, this.container[0]);


        // FPS
        this.controls.movementSpeed = 1000;
        this.controls.lookSpeed = 0.075;
        this.controls.lookVertical = false;

        // Trackball
        /*this.controls.rotateSpeed = 3.0;
        this.controls.zoomSpeed = 50;
        this.controls.panSpeed = 1;

        this.controls.noZoom = false;
        this.controls.noPan = false;

        this.controls.staticMoving = true;
        this.controls.dynamicDampingFactor = 0.3;*/

        //this.controls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];

        if (Modernizr.touch) {
            $('#side_controls_container').hide();
            $('#scene_container').width(90 + '%');
            $('#center_container').height(83 + '%');
        }
    },

    initMarkersModal: function() {

        var self = this;

        this.markersModal = $('#creationMarkersModal');
        this.markersModalInputName = this.markersModal.find('input');
        this.markersModalInputDescription = this.markersModal.find('textarea')
        this.createMarkerButton = this.markersModal.find('.btn-primary');

        this.closeMarkersModal = function() {
            self.markersModal.modal('hide');
            self.markersModalInputName.val("");
            self.markersModalInputDescription.val("");
            self.createMarkerButton.off('click');
        };

        this.markersModal.find('.cancel').on('click', function() {
            self.closeMarkersModal();
        });

    },

    startMarkerCreationMode: function(layer) {

        $("#scene_container").addClass("markersCreationMode");

        var self = this;

        this.domEventForMarkerCreation = new THREEx.DomEvent(this.camera, this.container[0]);

        this.meshesBindedForMarkerCreation = _.pluck(_.filter(self.instances, function(instance) {
            return instance.mesh != null
        }), 'mesh');


        var onIntersect = function(intersectPoint) {

            self.createMarkerButton.on('click', function() {
                layer.createMarker(self.markersModalInputName.val(), self.markersModalInputDescription.val(), intersectPoint.x, intersectPoint.y, intersectPoint.z);
                self.closeMarkersModal();
            });

            self.markersModal.modal('show');
            self.markersModalInputName.focus();

        }

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
            self.layerManager = new LayerManager(self.scene, self.camera, self.renderer, self.controls, self.container[0]);
            self.layerManager.bindControlEvents();
            self.layerManager.rescaleMarkers(0);
            self.layerManager.renderList();
        });
    },

    initLights: function() {
        var ambient = new THREE.AmbientLight(0x101030);
        this.scene.add(ambient);

        var dirLight = new THREE.DirectionalLight(0xffffff);
        dirLight.position.set(200, 200, 1000).normalize();
        this.camera.add(dirLight);
        this.camera.add(dirLight.target);
    },

    initAxes: function() {
        var axes = new THREE.AxisHelper();
        axes.position.set(-1000, 0, 0);
        axes.scale.x = axes.scale.y = axes.scale.z = 2;
        this.scene.add(axes);

        var arrow = new THREE.ArrowHelper(new THREE.Vector3(0, 1, 0), new THREE.Vector3(0, 0, 0), 50);
        arrow.position.set(200, 0, 400);
        this.scene.add(arrow);
    },

    initStats: function() {
        this.stats = new Stats();
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.bottom = '0px';
        document.body.appendChild(this.stats.domElement);
    },

    initRenderer: function() {
        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setSize(this.container.width(), this.container.height());
        this.container.append(this.renderer.domElement);
    },

    loadWindowResize: function() {
        var windowResize = THREEx.WindowResize(this.renderer, this.camera, this.container);
    },

    animate: function() {
        var self = this;
        window.requestAnimationFrame(function() {
            self.animate();
        });
        this.render();
        //this.controls.update();
        // FPS
        this.controls.update(this.clock.getDelta());

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
        projScreenMatrix.multiply(this.camera.projectionMatrix, this.camera.matrixWorldInverse);
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
            var instancesUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=latest&path=" + SCENE_INIT.pathForIframe
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
    }
}