/*global THREE,TWEEN,requestAnimationFrame*/
// Application option for DMU
var App = {
    SceneOptions: {
        zoomSpeed: 3,
        rotateSpeed: 3.0,
        panSpeed: 0.3,
        cameraNear: 1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: 0, y:0, z:0},
        startCameraPosition: {x: 100, y: 2500, z: 2500},
        endCameraPosition: {x: 0, y: 250, z: 250},
        defaultTargetPosition: {x: 0, y: 0, z: 0}
    }
};

window.onload = function() {
	'use strict';
    function hasWebGL() {
        try {
            return !! window.WebGLRenderingContext && !! document.createElement( 'canvas' ).getContext( 'experimental-webgl' );
        } catch( e ) {
            return false;
        }
    }

    window.localStorage.locale = 'unset';

    var errorMessage = window.WebGLRenderingContext ? [
        'Your graphics card does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br />',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' ) : [
        'Your browser does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br/>',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' );

    var container = document.getElementById('demo-scene');
    if ( ! hasWebGL() ){
        container.innerHTML = errorMessage;
    }
    // standard global variables
    var scene, camera, renderer, controls;

    // custom global variables
    var model;

    // FUNCTIONS
    function update(){
        if(model){
            model.rotation.set(0.45, model.rotation.y+0.005, model.rotation.z+0.005);
        }
        controls.update();
    }

    function animateCamera(){
        camera.position.copy(App.SceneOptions.startCameraPosition);
        new TWEEN.Tween(camera.position)
            .to(App.SceneOptions.endCameraPosition, 1000)
            .interpolation(TWEEN.Interpolation.CatmullRom)
            .easing(TWEEN.Easing.Sinusoidal.InOut)
            .start();
    }

    function render(){
        renderer.render( scene, camera );
    }

    function animate(){
        requestAnimationFrame(animate);
        update();
        TWEEN.update();
        render();
    }

    function addModelToScene( geometry ){
        var material = new THREE.MeshLambertMaterial( { color: 0xffffff, shading: THREE.FlatShading, overdraw: true } );
        model = new THREE.Mesh( geometry, material );
        model.scale.set(1,1,1);
        model.position.set(0, 0, 0);
        model.rotation.set(0.45, 0, 1.55);
        scene.add( model );
        geometry.computeBoundingBox();
        controls.target.copy(model.geometry.boundingBox.center());
        animate();
        animateCamera();
    }

    function handleResize() {
        camera.updateProjectionMatrix();
        renderer.setSize(container.clientWidth , container.clientHeight);
        controls.handleResize();
    }

    function init(){
        // SCENE
        scene = new THREE.Scene();

        // CAMERA
        camera = new THREE.PerspectiveCamera( 45, container.clientWidth/container.clientHeight,0.1, 5000);
        scene.add(camera);
        camera.position.set(0,250,200);

        // RENDERER
        renderer = new THREE.WebGLRenderer( {antialias:true, alpha:true} );
        renderer.setSize( container.clientWidth, container.clientHeight);

        // CONTROLS
        controls = new THREE.TrackballControls( camera, container );

        // LIGHT
        var light = new THREE.PointLight(0xffffff);
        light.position.set(-100,200,100);
        scene.add(light);
        var light2 = new THREE.PointLight(0xffffff);
        light2.position.set(100,-200,-100);
        scene.add(light2);

        // SKYBOX/FOG
        scene.fog = new THREE.FogExp2( 0x9999ff, 0.00025 );

        var binaryLoader = new THREE.BinaryLoader();
        binaryLoader.load( window.contextPath + '/images/pba.js', addModelToScene);

        var ambientLight = new THREE.AmbientLight(0x111111);
        scene.add(ambientLight);

        container.appendChild( renderer.domElement );
        window.addEventListener('resize', handleResize, false);

    }


    init();
};
