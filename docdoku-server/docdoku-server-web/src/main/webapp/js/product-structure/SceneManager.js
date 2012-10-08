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

    this.instances = [];
    this.meshesBindedForMarkerCreation = [];
}

SceneManager.prototype = {

    init: function() {
        this.initScene();
        this.initCamera();
        this.initControls();
        this.initLights();
        this.initAxes();
        this.initStats();
        this.initRenderer();
        this.loadWindowResize();
        this.initLayerManager();
        this.animate();
    },

    initScene: function() {
        this.container = $('div#container');
        // Init frame
        if (this.container.length === 0) {
            this.container = $('div#frameContainer');
        }
        this.scene = new THREE.Scene();
    },

    initCamera : function() {
        this.camera = new THREE.PerspectiveCamera( 45, window.innerWidth / window.innerHeight, 1, 50000 );
        this.camera.position.set(0, 10, 10000);
        this.scene.add( this.camera );
    },

    initControls: function() {
        this.controls = new THREE.TrackballControlsCustom( this.camera, this.container[0] );

        this.controls.rotateSpeed = 3.0;
        this.controls.zoomSpeed = 5;
        this.controls.panSpeed = 1;

        //this.controls.noZoom = false;
        //this.controls.noPan = false;

        this.controls.staticMoving = true;
        this.controls.dynamicDampingFactor = 0.3;

        this.controls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];

        if (Modernizr.touch){
            $('#side_controls_container').hide();
            $('#bottom_controls_container').hide();
            $('#scene_container').width(90+'%');
            $('#center_container').height(83+'%');
        } else {
            new ControlManager( this.controls );
        }
    },

    startMarkerCreationMode: function(layer) {
        var self = this;

        this.domEventForMarkerCreation = new THREEx.DomEvent(this.camera, this.container[0]);

        this.meshesBindedForMarkerCreation = _.pluck(_.filter(self.instances,function(instance) { return instance.mesh != null }), 'mesh');

        var numbersOfMeshes = this.meshesBindedForMarkerCreation.length;
        for (var j = 0; j<numbersOfMeshes; j++) {
            self.domEventForMarkerCreation.bind(this.meshesBindedForMarkerCreation[j], 'click', function(e) {
                var intersectPoint = e.target.intersectPoint;
                layer.createMarker("Nouveau marker", "description", intersectPoint.x, intersectPoint.y, intersectPoint.z);
            });
        }

    },

    stopMarkerCreationMode: function() {
        var numbersOfMeshes = this.meshesBindedForMarkerCreation.length;
        for (var j = 0; j<numbersOfMeshes; j++) {
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
        var ambient = new THREE.AmbientLight( 0x101030 );
        this.scene.add( ambient );

        var dirLight = new THREE.DirectionalLight( 0xffffff );
        dirLight.position.set( 200, 200, 1000 ).normalize();
        this.camera.add( dirLight );
        this.camera.add( dirLight.target );
    },

    initAxes: function() {
        var axes = new THREE.AxisHelper();
        axes.position.set( -1000, 0, 0 );
        axes.scale.x = axes.scale.y = axes.scale.z = 2;
        this.scene.add( axes );

        var arrow = new THREE.ArrowHelper( new THREE.Vector3( 0, 1, 0 ), new THREE.Vector3( 0, 0, 0 ), 50 );
        arrow.position.set( 200, 0, 400 );
        this.scene.add( arrow );
    },

    initStats: function() {
        this.stats = new Stats();
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.bottom = '0px';
        document.body.appendChild( this.stats.domElement );
    },

    initRenderer: function() {
        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setSize( this.container.width(), this.container.height() );
        this.container.append( this.renderer.domElement );
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
        this.controls.update();
        this.stats.update();
    },

    render: function() {
        this.updateInstances();
        this.scene.updateMatrixWorld();
        this.renderer.render( this.scene, this.camera );
    },

    updateInstances: function() {

        var numbersOfInstances = this.instances.length;

        for (var j = 0; j<numbersOfInstances; j++) {
            this.instances[j].update(this.camera);
        }

    }

}