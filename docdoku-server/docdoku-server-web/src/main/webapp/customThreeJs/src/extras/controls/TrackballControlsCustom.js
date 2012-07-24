/**
 * @author Eberhard Graether / http://egraether.com/
 */

THREE.TrackballControlsCustom = function ( object, domElement ) {

	THREE.EventTarget.call( this );

	var _this = this,
	STATE = { NONE : -1, PAN : 0, ZOOM : 1, ROTATE : 2},
	PAN = { UP: 0, RIGHT : 1, DOWN : 2, LEFT : 3},
	ROTATE = { UP: 0, RIGHT : 1, DOWN : 2, LEFT : 3};

	this.object = object;
	this.domElement = ( domElement !== undefined ) ? domElement : document;

	// API

	this.enabled = true;

	this.screen = { width: window.innerWidth, height: window.innerHeight, offsetLeft: 0, offsetTop: 0 };
	this.radius = ( this.screen.width + this.screen.height ) / 4;

	this.rotateSpeed = 1.0;
	this.zoomSpeed = 1.2;
	this.panSpeed = 0.3;

	this.noRotate = false;
	this.noZoom = false;
	this.noPan = false;

	this.staticMoving = false;
	this.dynamicDampingFactor = 0.2;

	this.minDistance = 0;
	this.maxDistance = Infinity;

	this.keys = [ 65 /*A*/, 83 /*S*/, 68 /*D*/ ];

	// internals

	this.target = new THREE.Vector3();

	var lastPosition = new THREE.Vector3();

	var _keyPressed = false,
	_state = STATE.NONE,

	_eye = new THREE.Vector3(),

	_rotateStart = new THREE.Vector3(),
	_rotateEnd = new THREE.Vector3(),

	_zoomStart = new THREE.Vector2(),
	_zoomEnd = new THREE.Vector2(),

	_panStart = new THREE.Vector2(),
	_panEnd = new THREE.Vector2();

	// events

	var changeEvent = { type: 'change' };


	// methods

	this.handleEvent = function ( event ) {

		if ( typeof this[ event.type ] == 'function' ) {

			this[ event.type ]( event );

		}

	};

	this.getMouseOnScreen = function ( clientX, clientY ) {

		return new THREE.Vector2(
			( clientX - _this.screen.offsetLeft ) / _this.radius * 0.5,
			( clientY - _this.screen.offsetTop ) / _this.radius * 0.5
		);

	};

	this.getMouseProjectionOnBall = function ( clientX, clientY ) {

		var mouseOnBall = new THREE.Vector3(
			( clientX - _this.screen.width * 0.5 - _this.screen.offsetLeft ) / _this.radius,
			( _this.screen.height * 0.5 + _this.screen.offsetTop - clientY ) / _this.radius,
			0.0
		);

		var length = mouseOnBall.length();

		if ( length > 1.0 ) {

			mouseOnBall.normalize();

		} else {

			mouseOnBall.z = Math.sqrt( 1.0 - length * length );

		}

		_eye.copy( _this.object.position ).subSelf( _this.target );

		var projection = _this.object.up.clone().setLength( mouseOnBall.y );
		projection.addSelf( _this.object.up.clone().crossSelf( _eye ).setLength( mouseOnBall.x ) );
		projection.addSelf( _eye.setLength( mouseOnBall.z ) );

		return projection;

	};

	this.rotateCamera = function () {

		var angle = Math.acos( _rotateStart.dot( _rotateEnd ) / _rotateStart.length() / _rotateEnd.length() );

		if ( angle ) {
			console.log(_rotateStart);
			console.log(_rotateEnd);
			console.log("angle w/out cos : " + _rotateStart.dot( _rotateEnd ) / _rotateStart.length() / _rotateEnd.length());
			console.log("angle w/ cos" + angle);

			var axis = ( new THREE.Vector3() ).cross( _rotateStart, _rotateEnd ).normalize(),
				quaternion = new THREE.Quaternion();

			console.log(axis);

			angle *= _this.rotateSpeed;

			quaternion.setFromAxisAngle( axis, -angle );

			quaternion.multiplyVector3( _eye );
			quaternion.multiplyVector3( _this.object.up );

			quaternion.multiplyVector3( _rotateEnd );

			if ( _this.staticMoving ) {

				_rotateStart = _rotateEnd;

			} else {

				quaternion.setFromAxisAngle( axis, angle * ( _this.dynamicDampingFactor - 1.0 ) );
				quaternion.multiplyVector3( _rotateStart );

			}

		}

	};

	this.rotateCameraOrientation = function (orientation) {

		var unityValue = 1;
		var axis;

		switch(orientation) {
			case ROTATE.UP:
				axis = (new THREE.Vector3(-unityValue,0,0)).normalize();
				break;
			case ROTATE.DOWN:
				axis = (new THREE.Vector3(unityValue,0,0)).normalize();
				break;
			case ROTATE.LEFT:
				axis = (new THREE.Vector3(0,-unityValue,0)).normalize();
				break;
			case ROTATE.RIGHT:
				axis = (new THREE.Vector3(0,unityValue,0)).normalize();
				break;
		}

		// 360 degrees in 12 times. (30 degrees each time)
		var angle = Math.acos(Math.sqrt(3)/2);

		var quaternion = new THREE.Quaternion();

		quaternion.setFromAxisAngle( axis, -angle );

		quaternion.multiplyVector3( _eye );
		quaternion.multiplyVector3( _this.object.up );

		quaternion.multiplyVector3( _rotateEnd );

		if ( _this.staticMoving ) {

			_rotateStart = _rotateEnd;

		} else {

			quaternion.setFromAxisAngle( axis, angle * ( _this.dynamicDampingFactor - 1.0 ) );
			quaternion.multiplyVector3( _rotateStart );

		}

		_this.manualUpdate();


	};

	this.zoomCamera = function() {
		var factor = 1.0 + ( _zoomEnd.y - _zoomStart.y ) * _this.zoomSpeed;
		this.zoomCameraOperation(factor);
	};

	this.zoomCameraWheel = function(wheelDeltaY) {
		var factor = 1.0 + ( wheelDeltaY ) * _this.zoomSpeed;
		this.zoomCameraOperation(factor);
	};

	this.zoomCameraValue = function (value) {

		var factor;
		factor = this.getZoomingFactor(value);

		this.zoomCameraOperation(factor);
	};

	this.zoomCameraOperation = function (factor) {

		if ( factor !== 1.0 && factor > 0.0 ) {

			console.log(_eye);
			_eye.multiplyScalar( factor );
			console.log(factor);
			console.log(_eye);

			if ( _this.staticMoving ) {

				_zoomStart = _zoomEnd;

			} else {

				_zoomStart.y += ( _zoomEnd.y - _zoomStart.y ) * this.dynamicDampingFactor;

			}

		}

		this.manualUpdate();

	};

	this.getZoomingFactor = function (value) {
		if(value < 10) {
			return(1.5);
		} else if (value < 20) {
			return(1.4);
		} else if (value < 30) {
			return(1.3);
		} else if (value < 40) {
			return(1.2);
		} else if (value < 50) {
			return(1.1);
		} else if (value < 60) {
			return(0.9);
		} else if (value < 70) {
			return(0.8);
		} else if (value < 80) {
			return(0.7);
		} else if (value < 90) {
			return(0.6);
		} else if (value <= 100) {
			return(0.5);
		}
	};

	this.panCamera = function () {
		var mouseChange = _panEnd.clone().subSelf( _panStart );
		this.panCameraOperation(mouseChange);
	};

	this.panCameraOrientation = function (orientation) {
		var mouseChange;
		var unityValue = 0.01;

		switch(orientation) {
			case PAN.UP:
				mouseChange = new THREE.Vector2(0,-unityValue);
				break;
			case PAN.DOWN:
				mouseChange = new THREE.Vector2(0,unityValue);
				break;
			case PAN.LEFT:
				mouseChange = new THREE.Vector2(-unityValue,0);
				break;
			case PAN.RIGHT:
				mouseChange = new THREE.Vector2(unityValue,0);
				break;
		}
		this.panCameraOperation(mouseChange);
	};

	this.panCameraOperation = function(mouseChange) {
		if ( mouseChange.lengthSq() ) {

			mouseChange.multiplyScalar( _eye.length() * _this.panSpeed );

			var pan = _eye.clone().crossSelf( _this.object.up ).setLength( mouseChange.x );
			pan.addSelf( _this.object.up.clone().setLength( mouseChange.y ) );

			_this.object.position.addSelf( pan );
			_this.target.addSelf( pan );

			if ( _this.staticMoving ) {

				_panStart = _panEnd;

			} else {

				_panStart.addSelf( mouseChange.sub( _panEnd, _panStart ).multiplyScalar( _this.dynamicDampingFactor ) );

			}

		}
	};

	this.checkDistances = function () {

		if ( !_this.noZoom || !_this.noPan ) {

			if ( _this.object.position.lengthSq() > _this.maxDistance * _this.maxDistance ) {

				_this.object.position.setLength( _this.maxDistance );

			}

			if ( _eye.lengthSq() < _this.minDistance * _this.minDistance ) {

				_this.object.position.add( _this.target, _eye.setLength( _this.minDistance ) );

			}

		}

	};

	this.update = function () {

		_eye.copy( _this.object.position ).subSelf( _this.target );

		if ( !_this.noRotate ) {

			_this.rotateCamera();

		}
		
		if ( !_this.noZoom ) {

			_this.zoomCamera();

		}

		if ( !_this.noPan ) {

			_this.panCamera();

		}

		_this.object.position.add( _this.target, _eye );

		_this.checkDistances();

		_this.object.lookAt( _this.target );

		if ( lastPosition.distanceTo( _this.object.position ) > 0 ) {
			
			_this.dispatchEvent( changeEvent );

			lastPosition.copy( _this.object.position );

		}

	};

	this.manualUpdate = function () {
		_this.object.position.add( _this.target, _eye );

		_this.checkDistances();

		_this.object.lookAt( _this.target );

		if ( lastPosition.distanceTo( _this.object.position ) > 0 ) {
			
			_this.dispatchEvent( changeEvent );

			lastPosition.copy( _this.object.position );

		}
	}

	// listeners

	function keydown( event ) {

		if ( ! _this.enabled ) return;

		if ( _state !== STATE.NONE ) {

			return;

		} else if ( event.keyCode === _this.keys[ STATE.ROTATE ] && !_this.noRotate ) {

			_state = STATE.ROTATE;

		} else if ( event.keyCode === _this.keys[ STATE.ZOOM ] && !_this.noZoom ) {

			_state = STATE.ZOOM;

		} else if ( event.keyCode === _this.keys[ STATE.PAN ] && !_this.noPan ) {

			_state = STATE.PAN;

		}

		if ( _state !== STATE.NONE ) {

			_keyPressed = true;

		}

	}

	function keyup( event ) {

		if ( ! _this.enabled ) return;

		if ( _state !== STATE.NONE ) {

			_state = STATE.NONE;

		}

	}

	function mousedown( event ) {

		console.log('mousedown');

		if ( ! _this.enabled ) return;

		event.preventDefault();
		event.stopPropagation();

		if ( _state === STATE.NONE ) {

			_state = event.button;

			if ( _state === STATE.ROTATE && !_this.noRotate ) {

				_rotateStart = _rotateEnd = _this.getMouseProjectionOnBall( event.clientX, event.clientY );

			} else if ( _state === STATE.ZOOM && !_this.noZoom ) {

				_zoomStart = _zoomEnd = _this.getMouseOnScreen( event.clientX, event.clientY );

			} else if ( !this.noPan ) {

				_panStart = _panEnd = _this.getMouseOnScreen( event.clientX, event.clientY );

			}

		}

	}

	function mousemove( event ) {

		if ( ! _this.enabled ) return;

		if ( _keyPressed ) {

			_rotateStart = _rotateEnd = _this.getMouseProjectionOnBall( event.clientX, event.clientY );
			_zoomStart = _zoomEnd = _this.getMouseOnScreen( event.clientX, event.clientY );
			_panStart = _panEnd = _this.getMouseOnScreen( event.clientX, event.clientY );

			_keyPressed = false;

		}

		if ( _state === STATE.NONE ) {

			return;

		} else if ( _state === STATE.ROTATE && !_this.noRotate ) {

			_rotateEnd = _this.getMouseProjectionOnBall( event.clientX, event.clientY );

		} else if ( _state === STATE.ZOOM && !_this.noZoom ) {

			_zoomEnd = _this.getMouseOnScreen( event.clientX, event.clientY );

		} else if ( _state === STATE.PAN && !_this.noPan ) {

			_panEnd = _this.getMouseOnScreen( event.clientX, event.clientY );

		}

	}

	function mouseup( event ) {

		if ( ! _this.enabled ) return;

		event.preventDefault();
		event.stopPropagation();

		_state = STATE.NONE;

	}

	function mousewheel(event) {
		_this.zoomCameraWheel(-event.wheelDeltaY * 0.0001);
		event.preventDefault();
	}

	this.domElement.addEventListener( 'contextmenu', function ( event ) { event.preventDefault(); }, false );

	this.domElement.addEventListener( 'mousemove', mousemove, false );
	this.domElement.addEventListener( 'mousedown', mousedown, false );
	this.domElement.addEventListener( 'mouseup', mouseup, false );

	this.domElement.addEventListener('DOMMouseScroll', mousewheel, false);
	this.domElement.addEventListener('mousewheel', mousewheel, false);

	window.addEventListener( 'keydown', keydown, false );
	window.addEventListener( 'keyup', keyup, false );
};
