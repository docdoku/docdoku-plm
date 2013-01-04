THREE.FirstPersonControlsCustom = function ( object, domElement ) {

    this.object = object;
    this.target = new THREE.Vector3( 0, 0, 0 );

    this.domElement = ( domElement !== undefined ) ? domElement : document;

    this.movementSpeed = 1.0;
    this.lookSpeed = 0.005;

    this.lookVertical = true;
    this.autoForward = false;
    // this.invertVertical = false;

    this.activeLook = true;

    this.heightSpeed = false;
    this.heightCoef = 1.0;
    this.heightMin = 0.0;
    this.heightMax = 1.0;

    this.constrainVertical = false;
    this.verticalMin = 0;
    this.verticalMax = Math.PI;

    this.autoSpeedFactor = 0.0;

    this.mouseX = 0;
    this.mouseY = 0;

    this.lat = 0;
    this.lon = 0;
    this.phi = 0;
    this.theta = 0;

    this.moveWheelSpeed = 50;
    this.moveForward = false;
    this.moveBackward = false;
    this.moveLeft = false;
    this.moveRight = false;
    this.freeze = true;

    this.mouseDragOn = false;

    this.viewHalfX = 0;
    this.viewHalfY = 0;

    this.zoomWheel = 0;

    if ( this.domElement !== document ) {

        this.domElement.setAttribute( 'tabindex', -1 );

    }

    //

    this.handleResize = function () {

        if ( this.domElement === document ) {

            this.viewHalfX = window.innerWidth / 2;
            this.viewHalfY = window.innerHeight / 2;

        } else {

            this.viewHalfX = this.domElement.offsetWidth / 2;
            this.viewHalfY = this.domElement.offsetHeight / 2;

        }

    };

    this.onMouseDown = function ( event ) {

        if ( this.domElement !== document ) {

            this.domElement.focus();

        }

        event.preventDefault();

        // Click second mouse button
        if ( this.activeLook && event.button == 2 ) {

            this.mouseDragOn = true;
            this.freeze = false;

            this.domElement.addEventListener( 'mouseup', bind( this, this.onMouseUp ), false );

        }

    };

    this.onMouseUp = function ( event ) {

        event.preventDefault();
        event.stopPropagation();

        // Release second mouse button
        if ( this.activeLook && event.button == 2 ) {

            this.mouseDragOn = false;
            this.freeze = true;

            this.domElement.removeEventListener( 'mousedown', bind( this, this.onMouseDown ), false );
        }

    };

    this.onMouseMove = function ( event ) {

        if ( this.domElement === document ) {

            this.mouseX = event.pageX - this.viewHalfX;
            this.mouseY = event.pageY - this.viewHalfY;

        } else {
            var offvar = $('div#container').offset();
            this.mouseX = event.pageX - offvar.left - this.viewHalfX;
            this.mouseY = event.pageY - offvar.top - this.viewHalfY;

        }

    };

    this.onMouseWheel = function ( event ) {

        if ( this.domElement !== document ) {

            this.domElement.focus();

        }

        event.preventDefault();
        event.stopPropagation();

        if (event.wheelDelta >= 0) {
            this.zoomWheel++;
        } else {
            this.zoomWheel--;
        }

    };

    this.onKeyDown = function ( event ) {

        //event.preventDefault();

        switch ( event.keyCode ) {

            case 38: /*up*/
            case 90: /*Z*/ this.moveForward = true; this.freeze = false; break;

            case 37: /*left*/
            case 81: /*Q*/ this.moveLeft = true; this.freeze = false; break;

            case 40: /*down*/
            case 83: /*S*/ this.moveBackward = true; this.freeze = false; break;

            case 39: /*right*/
            case 68: /*D*/ this.moveRight = true; this.freeze = false; break;

            case 82: /*R*/ this.moveUp = true; this.freeze = false; break;
            case 70: /*F*/ this.moveDown = true; this.freeze = false; break;

            // case 88: /*X*/ this.freeze = !this.freeze; break;

        }

    };

    this.onKeyUp = function ( event ) {

        switch( event.keyCode ) {

            case 38: /*up*/
            case 90: /*Z*/ this.moveForward = false; this.freeze = true; break;

            case 37: /*left*/
            case 81: /*Q*/ this.moveLeft = false; this.freeze = true; break;

            case 40: /*down*/
            case 83: /*S*/ this.moveBackward = false; this.freeze = true; break;

            case 39: /*right*/
            case 68: /*D*/ this.moveRight = false; this.freeze = true; break;

            case 82: /*R*/ this.moveUp = false; this.freeze = true; break;
            case 70: /*F*/ this.moveDown = false; this.freeze = true; break;

        }

    };

    this.update = function( delta ) {
        var actualMoveSpeed = 0;

        // Manages mousewheel forward and backward movement
        if(this.zoomWheel<0) {
            while(this.zoomWheel<0) {
                this.object.translateZ( this.moveWheelSpeed );
                this.zoomWheel++;
            }
        } else {
            while(this.zoomWheel>0) {
                this.object.translateZ( - this.moveWheelSpeed );
                this.zoomWheel--;
            }
        }

        // TODO: Manages pan

        // center the mouse when forward etc...


        if ( this.freeze ) {

            return;

        } else {

            if ( this.heightSpeed ) {

                var y = THREE.Math.clamp( this.object.position.y, this.heightMin, this.heightMax );
                var heightDelta = y - this.heightMin;

                this.autoSpeedFactor = delta * ( heightDelta * this.heightCoef );

            } else {

                this.autoSpeedFactor = 0.0;

            }

            actualMoveSpeed = delta * this.movementSpeed;

            if ( this.moveForward || ( this.autoForward && !this.moveBackward ) ) this.object.translateZ( - ( actualMoveSpeed + this.autoSpeedFactor ) );
            if ( this.moveBackward ) this.object.translateZ( actualMoveSpeed );

            if ( this.moveLeft ) this.object.translateX( - actualMoveSpeed );
            if ( this.moveRight ) this.object.translateX( actualMoveSpeed );

            if ( this.moveUp ) this.object.translateY( actualMoveSpeed );
            if ( this.moveDown ) this.object.translateY( - actualMoveSpeed );

            var actualLookSpeed = delta * this.lookSpeed;

            if ( !this.activeLook ) {

                actualLookSpeed = 0;

            }


            this.lon += this.mouseX * actualLookSpeed;
            if( this.lookVertical ) this.lat -= this.mouseY * actualLookSpeed; // * this.invertVertical?-1:1;

            this.lat = Math.max( - 85, Math.min( 85, this.lat ) );
            this.phi = ( 90 - this.lat ) * Math.PI / 180;
            this.theta = this.lon * Math.PI / 180;

            var targetPosition = this.target,
                position = this.object.position;

            targetPosition.x = position.x + 100 * Math.sin( this.phi ) * Math.cos( this.theta );
            targetPosition.y = position.y + 100 * Math.cos( this.phi );
            targetPosition.z = position.z + 100 * Math.sin( this.phi ) * Math.sin( this.theta );

        }

        var verticalLookRatio = 1;

        if ( this.constrainVertical ) {

            verticalLookRatio = Math.PI / ( this.verticalMax - this.verticalMin );

        }

        this.lon += this.mouseX * actualLookSpeed;
        if( this.lookVertical ) this.lat -= this.mouseY * actualLookSpeed * verticalLookRatio;

        this.lat = Math.max( - 85, Math.min( 85, this.lat ) );
        this.phi = ( 90 - this.lat ) * Math.PI / 180;

        this.theta = this.lon * Math.PI / 180;

        if ( this.constrainVertical ) {

            this.phi = THREE.Math.mapLinear( this.phi, 0, Math.PI, this.verticalMin, this.verticalMax );

        }

        // Done twice, why ?
        var targetPosition = this.target,
            position = this.object.position;

        targetPosition.x = position.x + 100 * Math.sin( this.phi ) * Math.cos( this.theta );
        targetPosition.y = position.y + 100 * Math.cos( this.phi );
        targetPosition.z = position.z + 100 * Math.sin( this.phi ) * Math.sin( this.theta );

        this.object.lookAt( targetPosition );

    };


    this.domElement.addEventListener( 'contextmenu', function ( event ) { event.preventDefault(); }, false );
    this.domElement.addEventListener( 'mousedown', bind( this, this.onMouseDown ), false );
    this.domElement.addEventListener( 'mousemove', bind( this, this.onMouseMove ), false );
    this.domElement.addEventListener( 'keydown', bind( this, this.onKeyDown ), false );
    this.domElement.addEventListener( 'keyup', bind( this, this.onKeyUp ), false );
    this.domElement.addEventListener( 'DOMMouseScroll', bind(this, this.onMouseWheel), false );
    this.domElement.addEventListener( 'mousewheel', bind(this, this.onMouseWheel), false );

    function bind( scope, fn ) {

        return function () {

            fn.apply( scope, arguments );

        };

    };

    this.handleResize();

};