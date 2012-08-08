function SceneManager(options) {

    var options = options || {};

    var defaultsOptions = {
        scoreQuality: 40000,
        typeLoader: 'json',
        typeMaterial: 'face'
    }

    _.defaults(options, defaultsOptions);
    _.extend(this, options);

    this.loader = (this.typeLoader == 'binary') ? new THREE.BinaryLoader() : new THREE.JSONLoader();
    this.material = (this.typeMaterial == 'face') ? new THREE.MeshFaceMaterial() : (this.typeMaterial == 'lambert') ? new THREE.MeshLambertMaterial() : new THREE.MeshNormalMaterial();

    this.instances = [];
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
        this.animate();
    },

    initScene: function() {
        this.container = $('div#container');
        this.scene = new THREE.Scene();
    },

    initCamera : function() {
        this.camera = new THREE.PerspectiveCamera( 45, window.innerWidth / window.innerHeight, 1, 50000 );
        this.camera.position.set(0, 10, 10000);
        this.scene.add( this.camera );
    },

    initControls: function() {
        this.controls = new THREE.TrackballControls( this.camera );

        this.controls.rotateSpeed = 4.0;
        this.controls.zoomSpeed = 5;
        this.controls.panSpeed = 2;

        this.controls.noZoom = false;
        this.controls.noPan = false;

        this.controls.staticMoving = true;
        this.controls.dynamicDampingFactor = 0.3;
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
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        this.container.append( this.renderer.domElement );
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
        this.camera.lookAt( this.scene.position );
        var rand = Math.floor(Math.random()*3);
        if (rand == 0)
            this.updateInstances();
        this.scene.updateMatrixWorld();
        this.renderer.render( this.scene, this.camera );
    },

    updateInstances: function() {

        var numbersOfInstances = this.instances.length;

        for (var j = 0; j<numbersOfInstances; j++) {

            var instance = this.instances[j];

            if (instance.idle && instance.part.idle) {

                instance.idle = false;

                var score = instance.getScore(this.camera.position);

                if (score > this.scoreQuality) {
                    if (instance.onScene) {
                        this.scene.remove(instance.getMeshToRemove());
                        instance.onRemove();
                        instance.onScene = false;
                        instance.idle = true;
                    } else {
                        instance.idle = true;
                    }
                } else {
                    if (!instance.onScene) {
                        (function(pInstance, pScene) {
                            instance.getMeshForLoading(function(mesh) {
                                pScene.add(mesh);
                                pInstance.onAdd();
                                pInstance.onScene = true;
                                pInstance.idle = true;
                            });
                        })(instance, this.scene);
                    } else {
                        instance.idle = true;
                    }
                }
            }
        }
    }

}