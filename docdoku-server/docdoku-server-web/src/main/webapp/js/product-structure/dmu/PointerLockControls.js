/**
 * @author mrdoob / http://mrdoob.com/
 */

THREE.PointerLockControls = function ( camera ) {

    var keyCodes = {
        FORWARD : 90, // Z
        LEFT : 81, // Q
        RIGHT : 68, // D
        BACKWARD : 83, // S
        UP : 32, // SPACE
        DOWN : 17 // CTRL
    };

	var scope = this;
    var moveSpeed = 1;

    var changeEvent = { type: 'change' };

	camera.rotation.set( 0, 0, 0 );

	var pitchObject = new THREE.Object3D();
	pitchObject.add( camera );

	var yawObject = new THREE.Object3D();
	yawObject.position.y = 10;
	yawObject.add( pitchObject );

	var moveForward = false;
	var moveBackward = false;
	var moveLeft = false;
	var moveRight = false;
    var moveUp = false;
    var moveDown = false;

	var velocity = new THREE.Vector3();

	var PI_2 = Math.PI / 2;

	var onMouseMove = function ( event ) {

		if ( scope.enabled === false ) return;

        event.preventDefault();

		var movementX = event.movementX || event.mozMovementX || event.webkitMovementX || 0;
		var movementY = event.movementY || event.mozMovementY || event.webkitMovementY || 0;

		yawObject.rotation.y -= movementX * 0.002;
		pitchObject.rotation.x -= movementY * 0.002;

		pitchObject.rotation.x = Math.max( - PI_2, Math.min( PI_2, pitchObject.rotation.x ) );

	};

	var onKeyDown = function ( event ) {

        if ( scope.enabled === false ) return;

        event.preventDefault();

		switch ( event.keyCode ) {

            case keyCodes.FORWARD:
                moveForward = true;
                break;

            case keyCodes.LEFT:
                moveLeft = true;
                break;

            case keyCodes.BACKWARD:
                moveBackward = true;
                break;

            case keyCodes.RIGHT:
                moveRight = true;
                break;

            case keyCodes.UP:
                moveUp = true;
                break;

            case keyCodes.DOWN:
                moveDown = true;
                break;

		}
	};

	var onKeyUp = function ( event ) {

        if ( scope.enabled === false ) return;

        event.preventDefault();

		switch( event.keyCode ) {

            case keyCodes.FORWARD:
                moveForward = false;
                break;

            case keyCodes.LEFT:
                moveLeft = false;
                break;

            case keyCodes.BACKWARD:
                moveBackward = false;
                break;

            case keyCodes.RIGHT:
                moveRight = false;
                break;

            case keyCodes.UP:
                moveUp = false;
                break;

            case keyCodes.DOWN:
                moveDown = false;
                break;

		}

	};

	document.addEventListener( 'mousemove', onMouseMove, false );
	document.addEventListener( 'keydown', onKeyDown, false );
	document.addEventListener( 'keyup', onKeyUp, false );

	this.enabled = false;

	this.getObject = function () {
		return yawObject;
	};

	this.getDirection = function() {

		// assumes the camera itself is not rotated

		var direction = new THREE.Vector3( 0, 0, -1 );
		var rotation = new THREE.Euler( 0, 0, 0, "YXZ" );

		return function( v ) {

			rotation.set( pitchObject.rotation.x, yawObject.rotation.y, 0 );

			v.copy( direction ).applyEuler( rotation );

			return v;

		}

	}();

	this.update = function ( delta ) {

		if ( scope.enabled === false ) return;

		delta *= 0.1;

        velocity.x += ( - velocity.x ) * 0.08 * delta;
        velocity.y += ( - velocity.y ) * 0.08 * delta;
        velocity.z += ( - velocity.z ) * 0.08 * delta;


        if ( moveForward ) {
            velocity.z -= moveSpeed * delta;
        }
        if ( moveBackward ){
            velocity.z += moveSpeed * delta;
        }

        if ( moveUp ){
            velocity.y += moveSpeed * delta;
        }
        if ( moveDown ){
            velocity.y -= moveSpeed * delta;
        }

        if ( moveLeft ){
            velocity.x -= moveSpeed * delta;
        }
        if ( moveRight ){
            velocity.x += moveSpeed * delta;
        }

		yawObject.translateX( velocity.x );
		yawObject.translateY( velocity.y ); 
		yawObject.translateZ( velocity.z );

        scope.dispatchEvent( changeEvent );

	};

    this.moveToPosition = function ( vector ) {
        yawObject.translateX( vector.x );
        yawObject.translateY( vector.y );
        yawObject.translateZ( vector.z );
    };

    this.resetCamera = function(camera){
        pitchObject.add(camera);
    };

    this.getTarget = function(){
        return yawObject.position;
    }
};

THREE.PointerLockControls.prototype = Object.create( THREE.EventDispatcher.prototype );
