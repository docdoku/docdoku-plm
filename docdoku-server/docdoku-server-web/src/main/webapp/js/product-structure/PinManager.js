/**
 * @author Florent Suc
 */

var STATE = { FULL : 0, TRANSPARENT : 1, HIDDEN : 2};
var mouse = new THREE.Vector2(),
    offset = new THREE.Vector3(),
    INTERSECTED, SELECTED,
    projector = new THREE.Projector(),
    domEvent;

PinManager = function( scene, camera, renderer, controls, container ) {
	this.scene = scene;
    this.camera = camera;
    this.pins = [];
    this.state = STATE.FULL;
    this.renderer = renderer;
    this.controls = controls;
    this.container = container;
    domEvent = new THREEx.DomEvent(camera, container);
};

PinManager.prototype = {

    addPins: function() {
        var pinMock = {
            issueTitle : "Issue 1",
            issueAuthor : "Monsieux X",
            issueDate : "17/11/2008",
            issueDesc : "Compatibility required",
            issueComment : "Changer l\'angle<br/>*****<br/>Monsieu Z - Uninstall system<br/>*****",
            issueZone : "S1234564654651231S",
            issueResponsible : "Monsieur W",
            issueCriticity : "Low"
        };
        this.addPin(1000,200,1060, pinMock);
    },

    addPin: function(x,y,z,params) {
        // set up the sphere vars
        var radius = 50,
            segments = 16,
            rings = 16;

        // create the sphere's material
        var pinMaterial =
            new THREE.MeshLambertMaterial({
                color: 0xFF0000,
                opacity: 1,
                transparent: true
            });

        // create a new mesh with
        // sphere geometry - we will cover
        // the sphereMaterial next!
        var pin = new THREE.Mesh(

            new THREE.SphereGeometry(
                radius,
                segments,
                rings),
            pinMaterial);

        pin.position.set( x , y , z );

        var self = this;
        
        domEvent.bind(pin,'click', function(){
            if (self.state != STATE.HIDDEN) {
                self.showPopup(params);
            }
        });
        //domEvent.unbind(mesh, 'click', callback);

        // add the sphere to the scene
        this.scene.add( pin );

        this.pins.push( pin );

        pin.geometry.dynamic = true;

    },

    rescalePins: function(value) {
        for (var i = this.pins.length - 1; i >= 0; i--) {
            var pin = this.pins[i];
            pin.scale.x += value;
            pin.scale.y += value;
            pin.scale.z += value;
        }
    },

    changePinState: function() {
        var pinStateControl = $('#pinState');
        var i, pin;
        switch(this.state) {
            case STATE.FULL  :
                pinStateControl.removeClass('icon-pin-full');
                pinStateControl.addClass('icon-pin-empty');
                for (i = this.pins.length - 1; i >= 0; i--) {
                    pin = this.pins[i];
                    pin.material.opacity = 0.4;
                }
                this.state = STATE.TRANSPARENT;
                break;
            case STATE.TRANSPARENT :
                pinStateControl.removeClass('icon-pin-empty');
                pinStateControl.addClass('icon-pin-dotted');
                for (i = this.pins.length - 1; i >= 0; i--) {
                    pin = this.pins[i];
                    pin.material.opacity = 0;
                }
                this.state = STATE.HIDDEN;
                break;
            case STATE.HIDDEN:
                pinStateControl.removeClass('icon-pin-dotted');
                pinStateControl.addClass('icon-pin-full');
                for (i = this.pins.length - 1; i >= 0; i--) {
                    pin = this.pins[i];
                    pin.material.opacity = 1;
                }
                this.state = STATE.FULL;
                break;
        }
    },

    bindEvent: function() {
        var self = this;
        $('#managePin .moveBtnLeft').click(function() {
            self.rescalePins(-0.5);
        });

        $('#managePin .moveBtnRight').click(function() {
            self.rescalePins(0.5);
        });
        $('#managePin .moveBtnCenter').click(function() {
            self.changePinState();
        });
    },
    
    showPopup: function(params) {
        $('#issueTitle').text(params.issueTitle);
        $('#issueAuthor').text(params.issueAuthor);
        $('#issueDate').text(params.issueDate);
        $('#issueDesc').text(params.issueDesc);
        $('#issueComment').html(params.issueComment);
        $('#issueZone').text(params.issueZone);
        $('#issueResponsible').text(params.issueResponsible);
        $('#issueCriticity').text(params.issueCriticity);
        $('#issueModal').modal('show');
    }
}