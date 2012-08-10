/**
 * @author Florent Suc
 */

ControlManager = function( control ) {
	this.controls = control;
	this.bindMapEvent();
	this.bindSpaceEvent();
	this.bindRangeEvent();
};

ControlManager.prototype = {
 
	bindMapEvent: function() {
		var self = this;
        $('#moveMap .moveBtnTop').click(function() {
            self.controls.panCameraOrientation(0);
        });

        $('#moveMap .moveBtnBottom').click(function() {
            self.controls.panCameraOrientation(2);
        });

        $('#moveMap .moveBtnLeft').click(function() {
            self.controls.panCameraOrientation(3);
        });

        $('#moveMap .moveBtnRight').click(function() {
            self.controls.panCameraOrientation(1);
        });
    },

    bindSpaceEvent: function() {
		var self = this;
        $('#moveSpace .moveBtnTop').click(function() {
            self.controls.rotateCameraOrientation(0);
        });

        $('#moveSpace .moveBtnBottom').click(function() {
            self.controls.rotateCameraOrientation(2);
        });

        $('#moveSpace .moveBtnLeft').click(function() {
            self.controls.rotateCameraOrientation(3);
        });

        $('#moveSpace .moveBtnRight').click(function() {
            self.controls.rotateCameraOrientation(1);
        });
    },

    bindRangeEvent: function() {
		var self = this;
        var range = $("#zoomRange");
        range.mouseup(function() {
            self.controls.zoomCameraValue(range.val());
            range.val(50);
        });
        
    }
};