$(document).ready(function() {
    var urlStructure = "parts/structure.json";
    new SceneManager(urlStructure).init();
});

function SceneManager(urlParts, options) {
    this.urlParts = urlParts;

    var options = options || {};

    var defaultsOptions = {
        partLowFilenameKey: 'binaryFilenameReduce50',
        partHighFilenameKey: 'binaryFilenameLow',
        computeVertexForLow: true,
        scoreQuality: 8000,
        scoreCoeffStd: 0.3,
        scoreCoeffDefault: 1,
        typeLoader: 'binary',
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
        var self = this;
        this.loadParts(function(parts) {
            self.processParts(parts);
        });
        this.animate();
    },

    initScene: function() {
        this.container = document.createElement( 'div' );
        document.body.appendChild( this.container );
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
        this.stats.domElement.style.position	= 'absolute';
        this.stats.domElement.style.bottom	= '0px';
        document.body.appendChild( this.stats.domElement );
    },

    initRenderer: function() {
        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        this.container.appendChild( this.renderer.domElement );
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
        this.updateInstances();
        this.scene.updateMatrixWorld();
        this.renderer.render( this.scene, this.camera );
    },

    loadParts: function(callback) {
        $.getJSON(this.urlParts, function(parts) {
            callback(parts);
        });
    },

    processParts: function(parts) {

        var numberOfParts = parts.length;

        for (var i = 0; i<numberOfParts; i++) {

            var partRaw = parts[i];

            var filenameLow = partRaw[this.partLowFilenameKey];
            var filenameHigh = partRaw[this.partHighFilenameKey];

            if (filenameLow && filenameHigh) {

                if (!partRaw.isHolesAndFasteners && !partRaw.isStandardPart) {
                    var part = new Part(this.loader, filenameLow, filenameHigh, partRaw.isStandardPart ? this.scoreCoeffStd : this.scoreCoeffDefault);
                    var instancesRaw = partRaw.instances;
                    for (var j = 0 ; j < instancesRaw.length; j++) {
                        var instanceRaw = instancesRaw[j];
                        var instance = new Instance(this.material, part, instanceRaw.tx*10, instanceRaw.ty*10, instanceRaw.tz*10, instanceRaw.rx, instanceRaw.ry, instanceRaw.rz);
                        this.instances.push(instance);
                    }
                }

            }

        }

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

function Instance(material, part, tx, ty, tz, rx, ry, rz) {

    this.position = {
        x: tx,
        y: ty,
        z: tz
    };

    this.rotation = {
        x: rx,
        y: ry,
        z: rz
    };

    this.part = part;
    this.mesh = null;
    this.onScene = false;
    this.idle = true;
    this.material = material;

}

Instance.prototype = {

    getScore: function(cameraPosition) {
        return this.part.scoreCoeff * this.getDistance(cameraPosition);
    },

    getDistance: function(cameraPosition) {
        return Math.sqrt(Math.pow(cameraPosition.x - this.position.x, 2) + Math.pow(cameraPosition.y - this.position.y, 2) + Math.pow(cameraPosition.z - this.position.z, 2));
    },

    getMeshForLoading: function(callback) {

        var self = this;
        this.part.getGeometry(function(geometry) {
            var mesh = new THREE.Mesh(geometry, self.material);
            mesh.position.set(self.position.x, self.position.y, self.position.z);
            VisualizationUtils.rotateAroundWorldAxis(mesh, self.rotation.x, self.rotation.y, self.rotation.z);
            mesh.doubleSided = false;
            self.mesh = mesh;
            callback(self.mesh);
        });

    },

    getMeshToRemove: function() {
        return this.mesh;
    },

    onAdd: function() {
        this.part.onAddInstanceOnScene();
    },

    onRemove: function() {
        this.mesh = null;
        this.part.onRemoveInstanceFromScene();
    }

}

function Part(loader, filenameLow, filenameHigh, scoreCoeff) {
    this.geometry = null;
    this.filenameLow = filenameLow;
    this.filenameHigh = filenameHigh;
    this.instancesOnScene = 0;
    this.idle = true;
    this.loader = loader;
    this.scoreCoeff = scoreCoeff;
}

Part.prototype = {

    getGeometry: function(callback) {
        if (this.geometry == null) {
            var self = this;
            this.idle = false;
            this.loader.load(this.filenameLow, function(geometry) {
                geometry.computeVertexNormals();
                self.geometry = geometry;
                self.idle = true;
                callback(self.geometry);
            }, 'images');
        } else {
            callback(this.geometry);
        }

    },

    onAddInstanceOnScene: function() {
        this.instancesOnScene++;
    },

    onRemoveInstanceFromScene: function() {
        if (--this.instancesOnScene == 0) {
            this.clear();
        }
    },

    clear: function() {
        this.geometry = null;
    }

}