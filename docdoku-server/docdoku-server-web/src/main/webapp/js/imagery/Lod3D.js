$(document).ready(function() {
	
	var resultLogs = $('#resultLogs');

	$('#log').toggle(function() {
		resultLogs.text(getCamera());
	}, function() {
		resultLogs.text("");
	});

	var container, stats;

	stats = new Stats();
	stats.domElement.style.position	= 'absolute';
	stats.domElement.style.bottom	= '0px';
	stats.domElement.style.right	= '0px';
	document.body.appendChild( stats.domElement );

	var camera, scene, renderer, controls;

	var mouseX = 0, mouseY = 0;

	var windowHalfX = window.innerWidth / 2;
	var windowHalfY = window.innerHeight / 2;

	var xAxisNormalized = new THREE.Vector3(1,0,0).normalize();
	var yAxisNormalized = new THREE.Vector3(0,1,0).normalize();
	var zAxisNormalized = new THREE.Vector3(0,0,1).normalize();

	init();
	animate();

	var loadCounterInstances = 0;
	var loadCounterParts = 0;
	var globalFacesLength = 0;

	function countInstancesLoad() {
		loadCounterInstances++
	}

	function countPartsLoad() {
		loadCounterParts++
	}

	function countFaces(facesLength) {
		globalFacesLength+=facesLength;
	}

	function getLogs() {
		return loadCounterParts + " parts, " + loadCounterInstances + " instances, " + globalFacesLength + " faces"; 
	}

	function getCamera() {
		console.log(camera)
	}

	function init() {
		scene = new THREE.Scene();
		
		/*
		camera = new THREE.PerspectiveCamera( 45, window.innerWidth / window.innerHeight, 1, 50000 );
		camera.position.set(0, 10, 10000);
		scene.add(camera);
		*/


		camera = new THREE.PerspectiveCamera(35, window.innerWidth / window.innerHeight, 1, 10000 );
		camera.position.set(0, 0, 3);
		scene.add( camera );


		controls = new THREE.TrackballControlsCustom(camera,document.getElementById('container'));

		controls.rotateSpeed = 4.0;
		controls.zoomSpeed = 5;
		controls.panSpeed = 2;

		controls.staticMoving = true;
		controls.dynamicDampingFactor = 0.3;

		controls.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];

		
		var ambient = new THREE.AmbientLight( 0x101030 );
		scene.add( ambient );
		
		var dirLight = new THREE.DirectionalLight( 0xffffff );
		dirLight.position.set( 200, 200, 1000 ).normalize();
		camera.add( dirLight );
		camera.add( dirLight.target );

		var binary = false;

		var loader = (binary) ? new THREE.BinaryLoader() : new THREE.JSONLoader();

		var material = new THREE.MeshNormalMaterial();
		//var material = new THREE.MeshLambertMaterial();
		//var material = new THREE.MeshFaceMaterial();

		loader.load("../js/imagery/final2.js", function(geometry){
			countPartsLoad();
			var facesLength = geometry.faces.length;

			var mesh = new THREE.Mesh( geometry, material);
			mesh.position.set(-8,1,2.5);
			scene.add(mesh);
			countInstancesLoad();
			countFaces(facesLength);
		}, 'images');
				
		renderer = new THREE.WebGLRenderer();
		renderer.setSize( $('#container').width(), $('#container').height());
		$('#container').append( renderer.domElement );

		bindMapEvent();
		bindSpaceEvent();
		bindRangeEvent();
	}

	function animate() {
		requestAnimationFrame( animate );
		render();
		controls.update();
		stats.update();
	}

	function render() {
		renderer.render( scene, camera );
	}


	/*function mousewheel(event) {
	    camera.translateZ(event.wheelDeltaY * 0.05);
	    camera.projectionMatrix = new THREE.Matrix4().makePerspective(camera.fov, window.innerWidth / window.innerHeight, camera.near, camera.far);

	}*/

	function bindMapEvent() {
		$('#moveMap .moveBtnTop').click(function() {
			controls.panCameraOrientation(0);
		});

		$('#moveMap .moveBtnBottom').click(function() {
			controls.panCameraOrientation(2);
		});

		$('#moveMap .moveBtnLeft').click(function() {
			controls.panCameraOrientation(3);
		});

		$('#moveMap .moveBtnRight').click(function() {
			controls.panCameraOrientation(1);
		});
	}

	function bindSpaceEvent() {
		$('#moveSpace .moveBtnTop').click(function() {
			controls.rotateCameraOrientation(0);
		});

		$('#moveSpace .moveBtnBottom').click(function() {
			controls.rotateCameraOrientation(2);
		});

		$('#moveSpace .moveBtnLeft').click(function() {
			controls.rotateCameraOrientation(3);
		});

		$('#moveSpace .moveBtnRight').click(function() {
			controls.rotateCameraOrientation(1);
		});
	}

	function bindRangeEvent() {
		var range = $("#zoomRange");
		range.mouseup(function() {
			controls.zoomCameraValue(range.val());
			range.val(50);
		});
		
	}

});