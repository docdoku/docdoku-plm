/**
 * @author mrdoob / http://mrdoob.com/
 */

THREE.PointerLockControls = function ( camera ) {

    var keyCodes = {
        FORWARD : 90, // Z
        FORWARD_2 : 38, // Up
        LEFT : 81, // Q
        LEFT_2 : 37, // Left
        RIGHT : 68, // D
        RIGHT_2 : 39, // Right
        BACKWARD : 83, // S
        BACKWARD_2 : 40, // Down
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

    var isOnObject = false;
    var canJump = false;

    var prevTime = performance.now();

    var velocity = new THREE.Vector3();

    var PI_2 = Math.PI / 2;

    var onMouseMove = function ( event ) {

        if ( scope.enabled === false ){return;}

        var movementX = event.movementX || event.mozMovementX || event.webkitMovementX || 0;
        var movementY = event.movementY || event.mozMovementY || event.webkitMovementY || 0;

        yawObject.rotation.y -= movementX * 0.002;
        pitchObject.rotation.x -= movementY * 0.002;

        pitchObject.rotation.x = Math.max( - PI_2, Math.min( PI_2, pitchObject.rotation.x ) );

    };

    var onKeyDown = function ( event ) {

        if ( scope.enabled === false ){return;}

        event.preventDefault();

        switch ( event.keyCode ) {
            case keyCodes.FORWARD:
            case keyCodes.FORWARD_2:
                moveForward = true;
                break;

            case keyCodes.LEFT:
            case keyCodes.LEFT_2:
                moveLeft = true; break;

            case keyCodes.BACKWARD:
            case keyCodes.BACKWARD_2:
                moveBackward = true;
                break;

            case keyCodes.RIGHT:
            case keyCodes.RIGHT_2:
                moveRight = true;
                break;

            case keyCodes.UP:
                moveUp = true;
                if ( canJump === true ) velocity.y += 350;
                canJump = false;
                break;

            case keyCodes.DOWN:
                moveDown = true;
                break;
        }
    };

    var onKeyUp = function ( event ) {

        if ( scope.enabled === false ){return;}

        switch( event.keyCode ) {
            case keyCodes.FORWARD:
            case keyCodes.FORWARD_2:
                moveForward = false;
                break;

            case keyCodes.LEFT:
            case keyCodes.LEFT_2:
                moveLeft = false;
                break;

            case keyCodes.BACKWARD:
            case keyCodes.BACKWARD_2:
                moveBackward = false;
                break;

            case keyCodes.RIGHT:
            case keyCodes.RIGHT_2:
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

    this.unbindEvents = function(){
        document.removeEventListener( 'mousemove', onMouseMove, false );
        document.removeEventListener( 'keydown', onKeyDown, false );
        document.removeEventListener( 'keyup', onKeyUp, false );
    };

    this.bindEvents = function(){
        document.addEventListener( 'mousemove', onMouseMove, false );
        document.addEventListener( 'keydown', onKeyDown, false );
        document.addEventListener( 'keyup', onKeyUp, false );
    };

    this.enabled = false;

    this.getObject = function () {
        return yawObject;
    };

    this.isOnObject = function ( boolean ) {
        isOnObject = boolean;
        canJump = boolean;
    };

    this.getTarget = function(){
        var target = scope.getDirection(scope.target).multiplyScalar(1000);
        target.x += yawObject.position.x;
        target.y += yawObject.position.y;
        target.z += yawObject.position.z;
        return target;
    };

    this.getCamPos = function(){
        return yawObject.position;
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

    this.update = function () {

        if ( scope.enabled === false ) return;

        var time = performance.now();
        //var delta = ( time - prevTime ) / 1000;
        delta *= 0.1;

        velocity.x -= velocity.x * 0.08 * delta;
        //velocity.y -= 9.8 * 100.0 * delta; // 100.0 = mass
        velocity.y -= velocity.y * 0.08 * delta;
        velocity.z -= velocity.z * 0.08 * delta;

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
/*
        if ( isOnObject === true ) {
            velocity.y = Math.max( 0, velocity.y );
        }

        yawObject.translateX( velocity.x * delta );
        yawObject.translateY( velocity.y * delta );
        yawObject.translateZ( velocity.z * delta );

        if ( yawObject.position.y < 10 ) {
            velocity.y = 0;
            yawObject.position.y = 10;
            canJump = true;
        }
*/

        yawObject.translateX( velocity.x );
		yawObject.translateY( velocity.y );
		yawObject.translateZ( velocity.z );

        scope.dispatchEvent( changeEvent );
        prevTime = time;

    };

    this.moveToPosition = function ( vector ) {
        yawObject.translateX( vector.x );
        yawObject.translateY( vector.y );
        yawObject.translateZ( vector.z );
    };

    this.resetCamera = function(camera){
        pitchObject.add( camera );
    };

};

THREE.PointerLockControls.prototype = Object.create( THREE.EventDispatcher.prototype );