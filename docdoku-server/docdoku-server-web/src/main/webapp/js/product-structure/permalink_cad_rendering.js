var sceneManager={
    wireframe:false
};
define(["LoaderManager"],function(LoaderManager){

    function CadRenderer(el,fileName, width, height){
        this.$container =  $(el);
        this.loader = new LoaderManager();
        this.fileName = fileName;
        this.isPaused = false ;
        this.width = width;
        this.height = height;
    }

    CadRenderer.prototype = {

        init:function(){

            this.scene = new THREE.Scene();


            this.renderer = new THREE.WebGLRenderer();
            this.renderer.setSize(this.width, this.height);
            this.$container.append(this.renderer.domElement);

            this.defaultCameraPosition = new THREE.Vector3(-1000, 800, 1100);
            this.cameraPosition = new THREE.Vector3(0, 10, 1000);

            this.camera = new THREE.PerspectiveCamera(45, this.width / this.height, 1, 50000);

            var dirLight = new THREE.DirectionalLight(0xffffff);
            dirLight.position.set(200, 200, 1000).normalize();
            this.camera.add(dirLight);
            this.camera.add(dirLight.target);

            var axes = new THREE.AxisHelper(100);
            axes.position.set(-1000, 0, 0);
            this.scene.add(axes);

            var arrow = new THREE.ArrowHelper(new THREE.Vector3(0, 1, 0), new THREE.Vector3(0, 0, 0), 100);
            arrow.position.set(200, 0, 400);
            this.scene.add(arrow);


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
            //this.scene.add(this.grid);
            var self = this ;
            this.loader.parseFile(this.fileName, "", false, function(mesh){
                if ( mesh instanceof THREE.Mesh ) {
                    THREE.GeometryUtils.center( mesh.geometry );
                }
                self.scene.add(mesh);
                self.camera.lookAt(mesh.position);
            });
            this.animate();
        },

        animate: function() {
            var self = this;

            if (!this.isPaused) {

                window.requestAnimationFrame(function() {
                    self.animate();
                });

                this.cameraPosition = this.camera.position;
                this.controls.update();
            }

            this.render();
        },

        render: function() {
            this.scene.updateMatrixWorld();
            this.renderer.render(this.scene, this.camera);
        }

    };

    return CadRenderer;

});
