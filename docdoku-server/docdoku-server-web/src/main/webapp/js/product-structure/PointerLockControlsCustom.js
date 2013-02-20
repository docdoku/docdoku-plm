THREE.PointerLockControlsCustom = function ( camera , domElement ) {

    var scope = this;

    // pitchObject is the euler rotation x around the x axis of the object
    var pitchObject = new THREE.Object3D();
    pitchObject.add( camera );

    // yawObject is the euler rotation y around the y axis of the object
    var yawObject = new THREE.Object3D();
    //yawObject.position.y = 10;
    yawObject.add( pitchObject );

    var moveForward = false;
    var moveBackward = false;
    var moveLeft = false;
    var moveRight = false;
    var moveUp = false;
    var moveDown = false;

    var velocity = new THREE.Vector3();

    var moveSpeed = 1;

    var keycodeObj = {
        FORWARD : 90, // Z
        LEFT : 81, // Q
        RIGHT : 68, // D
        BACKWARD : 83, // S
        UP : 32, // SPACE
        DOWN : 17 // CTRL
    };

    var PI_2 = Math.PI / 2;

    if ( domElement !== document ) {
        // Allows focus
        domElement.setAttribute( 'tabindex', -1 );
    }

    var onMouseMove = function ( event ) {

        if ( scope.enabled === false ){
            return;
        }

        event.preventDefault();

        var movementX = event.movementX || event.mozMovementX || event.webkitMovementX || 0;
        var movementY = event.movementY || event.mozMovementY || event.webkitMovementY || 0;

        // Applies rotation
        yawObject.rotation.y -= movementX * 0.002;
        pitchObject.rotation.x -= movementY * 0.002;

        // Restricts pitch rotation from -180 to 180 degrees
        pitchObject.rotation.x = Math.max( - PI_2, Math.min( PI_2, pitchObject.rotation.x ) );

    };

    var onMouseWheel = function (event) {
        event.preventDefault();
    };

    var onKeyDown = function ( event ) {

        event.preventDefault();

        switch ( event.keyCode ) {

            case keycodeObj.FORWARD:
                moveForward = true;
                break;

            case keycodeObj.LEFT:
                moveLeft = true;
                break;

            case keycodeObj.BACKWARD:
                moveBackward = true;
                break;

            case keycodeObj.RIGHT:
                moveRight = true;
                break;

            case keycodeObj.UP:
                moveUp = true;
                break;

            case keycodeObj.DOWN:
                moveDown = true;
                break;
        }

    };

    var onKeyUp = function ( event ) {

        switch( event.keyCode ) {

            case keycodeObj.FORWARD:
                moveForward = false;
                break;

            case keycodeObj.LEFT:
                moveLeft = false;
                break;

            case keycodeObj.BACKWARD:
                moveBackward = false;
                break;

            case keycodeObj.RIGHT:
                moveRight = false;
                break;

            case keycodeObj.UP:
                moveUp = false;
                break;

            case keycodeObj.DOWN:
                moveDown = false;
                break;
        }

    };

    this.destroyControl = function() {
        domElement.removeEventListener( 'mousemove', onMouseMove, false );
        domElement.removeEventListener( 'keydown', onKeyDown, false );
        domElement.removeEventListener( 'keyup', onKeyUp, false );
        domElement.removeEventListener( 'mousewheel', onMouseWheel, false );
    };

    domElement.addEventListener( 'mousemove', onMouseMove, false );
    domElement.addEventListener( 'keydown', onKeyDown, false );
    domElement.addEventListener( 'keyup', onKeyUp, false );
    domElement.addEventListener( 'mousewheel', onMouseWheel, false );

    this.enabled = false;

    this.getObject = function () {
        return yawObject;
    };

    this.update = function ( delta ) {

        if ( scope.enabled === false ){
            return;
        }

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

    };

    this.moveToPosition = function ( vector ) {
        yawObject.translateX( vector.x );
        yawObject.translateY( vector.y );
        yawObject.translateZ( vector.z );
    };

    this.getPosition = function() {
        return yawObject.position;
    };

};