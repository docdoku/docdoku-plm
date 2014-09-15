/*global requestAnimationFrame*/
// Application option for DMU
var App = {
    SceneOptions: {
        zoomSpeed: 3,
        rotateSpeed: 3.0,
        panSpeed: 0.3,
        cameraNear: 1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100},
        defaultTargetPosition: {x: 0, y: 0, z: 0}
    }
};

window.onload = function() {

    function hasWebGL() {
        try {
            return !! window.WebGLRenderingContext && !! document.createElement( 'canvas' ).getContext( 'experimental-webgl' );
        } catch( e ) {
            return false;
        }
    }

    var errorMessage = window.WebGLRenderingContext ? [
        'Your graphics card does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br />',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' ) : [
        'Your browser does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br/>',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' );

    var info_panel = document.getElementById("demo-scene");
    if ( ! hasWebGL() ){
        info_panel.innerHTML = errorMessage;
    }
    // standard global variables
    var container, scene, camera, renderer, controls;

    // custom global variables
    var model;

    // FUNCTIONS

    function addModelToScene( geometry )
    {
        var material = new THREE.MeshLambertMaterial( { color: 0xffffff, shading: THREE.FlatShading, overdraw: true } );
        model = new THREE.Mesh( geometry, material );
        model.scale.set(1,1,1);
        model.position.set(0, 0, 0);
        model.rotation.set(0.45, 0, 1.55);
        scene.add( model );
    }

    function init()
    {
        // SCENE
        scene = new THREE.Scene();

        // CAMERA
        var SCREEN_WIDTH = 490, SCREEN_HEIGHT = 276;
        var VIEW_ANGLE = 45, ASPECT = SCREEN_WIDTH / SCREEN_HEIGHT, NEAR = 0.1, FAR = 20000;
        camera = new THREE.PerspectiveCamera( VIEW_ANGLE, ASPECT, NEAR, FAR);
        scene.add(camera);
        camera.position.set(0,250,200);
        camera.lookAt(scene.position);

        // RENDERER
        renderer = new THREE.WebGLRenderer( {antialias:true, alpha:true} );
        renderer.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        container = document.createElement( 'div' );
        info_panel.appendChild( container );
        container.appendChild( renderer.domElement );

        // CONTROLS
        controls = new THREE.TrackballControls( camera, info_panel );


        // LIGHT
        var light = new THREE.PointLight(0xffffff);
        light.position.set(-100,200,100);
        scene.add(light);
        var light2 = new THREE.PointLight(0xffffff);
        light2.position.set(100,-200,-100);
        scene.add(light2);

        // SKYBOX/FOG
        scene.fog = new THREE.FogExp2( 0x9999ff, 0.00025 );

        // RESIZER

        window.addEventListener('resize', handleResize, false);

        var $container = $('div#demo-scene');

        function handleResize() {
            ASPECT = $container.innerWidth() / $container.innerHeight();
            camera.updateProjectionMatrix();
            renderer.setSize($container.innerWidth(), $container.innerHeight());
            controls.handleResize();
        }

        var binaryLoader = new THREE.BinaryLoader();
        binaryLoader.load("images/pba.js", addModelToScene);

        var ambientLight = new THREE.AmbientLight(0x111111);
        scene.add(ambientLight);

    }

    function update()
    {
        if(model){
            model.rotation.set(0.45, model.rotation.y+0.005, model.rotation.z+0.005);
        }
        controls.update();
    }

    function render()
    {
        renderer.render( scene, camera );
    }

    function animate()
    {
        requestAnimationFrame(animate);
        render();
        update();
    }

    init();
    animate();
};