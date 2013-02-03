THREE.PointerLockControlsCustom = function ( camera , domElement ) {

    var scope = this;

    // pitchObject is the euler rotation x around the x axis of the object
    var pitchObject = new THREE.Object3D();
    pitchObject.add( camera );

    // yawObject is the euler rotation y around the y axis of the object
    var yawObject = new THREE.Object3D();
    //yawObject.position.y = 10;
    yawObject.add( pitchObject );

    // rollObject is the euler rotation z around the z axis of the object
    var rollObject = new THREE.Object3D();
    rollObject.add( yawObject );

    var moveForward = false;
    var moveBackward = false;
    var moveLeft = false;
    var moveRight = false;
    var moveUp = false;
    var moveDown = false;

    var isOnObject = false;

    var velocity = new THREE.Vector3();

    var moveSpeed = 1;

    var PI_2 = Math.PI / 2;

    if ( domElement !== document ) {
        // allow focus
        domElement.setAttribute( 'tabindex', -1 );
    }

    var onMouseMove = function ( event ) {

        if ( scope.enabled === false ) return;

        event.preventDefault();

        var movementX = event.movementX || event.mozMovementX || event.webkitMovementX || 0;
        var movementY = event.movementY || event.mozMovementY || event.webkitMovementY || 0;
        var movementZ = event.movementZ || event.mozMovementZ || event.webkitMovementZ || 0;

        yawObject.rotation.y -= movementX * 0.002;
        pitchObject.rotation.x -= movementY * 0.002;
        rollObject.rotation.z -= movementZ * 0.002;

        pitchObject.rotation.x = Math.max( - PI_2, Math.min( PI_2, pitchObject.rotation.x ) );

    };

    var onKeyDown = function ( event ) {

        event.preventDefault();

        switch ( event.keyCode ) {

            case 38: /*up*/
            case 90: /*Z*/
                moveForward = true;
                break;

            case 37: /*left*/
            case 81: /*Q*/
                moveLeft = true;
                break;

            case 40: /*down*/
            case 83: /*S*/
                moveBackward = true;
                break;

            case 39: /*right*/
            case 68: /*D*/
                moveRight = true;
                break;

            case 82: /*R*/
                moveUp = true;
                break;

            case 70: /*F*/
                moveDown = true;
                break;
        }

    };

    var onKeyUp = function ( event ) {

        switch( event.keyCode ) {

            case 38: /*up*/
            case 90: /*Z*/
                moveForward = false;
                break;

            case 37: /*left*/
            case 81: /*Q*/
                moveLeft = false;
                break;

            case 40: /*down*/
            case 83: /*S*/
                moveBackward = false;
                break;

            case 39: /*right*/
            case 68: /*D*/
                moveRight = false;
                break;

            case 82: /*R*/
                moveUp = false;
                break;

            case 70: /*F*/
                moveDown = false;
                break;

        }

    };

    this.destroyControl = function() {
        domElement.removeEventListener( 'mousemove', onMouseMove, false );
        domElement.removeEventListener( 'keydown', onKeyDown, false );
        domElement.removeEventListener( 'keyup', onKeyUp, false );
    };

    domElement.addEventListener( 'mousemove', onMouseMove, false );
    domElement.addEventListener( 'keydown', onKeyDown, false );
    domElement.addEventListener( 'keyup', onKeyUp, false );

    this.enabled = false;

    this.getObject = function () {

        //return yawObject;
        return rollObject;

    };

    this.isOnObject = function ( boolean ) {
        isOnObject = boolean;
    };

    this.update = function ( delta ) {

        if ( scope.enabled === false ) return;

        delta *= 0.1;

        velocity.x += ( - velocity.x ) * 0.08 * delta;
        velocity.y += ( - velocity.y ) * 0.08 * delta;
        velocity.z += ( - velocity.z ) * 0.08 * delta;

        //velocity.y -= 0.25 * delta;

        if ( moveForward ) velocity.z -= moveSpeed * delta;
        if ( moveBackward ) velocity.z += moveSpeed * delta;

        if ( moveUp ) velocity.y -= moveSpeed * delta;
        if ( moveDown ) velocity.y += moveSpeed * delta;

        if ( moveLeft ) velocity.x -= moveSpeed * delta;
        if ( moveRight ) velocity.x += moveSpeed * delta;

        if ( isOnObject === true ) {

            velocity.y = Math.max( 0, velocity.y );

        }

        yawObject.translateX( velocity.x );
        yawObject.translateY( velocity.y );
        yawObject.translateZ( velocity.z );

        if ( yawObject.position.y < 10 ) {

            velocity.y = 0;
            yawObject.position.y = 10;
        }

    };

};