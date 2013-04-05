define([
    "i18n!localization/nls/document-management-strings",
    "text!templates/document/document_lifecycle_task_signing.html"

], function(i18n, template) {

    var LifecycleTaskSigningView = Backbone.View.extend({

        tagName: 'div',

        events: {
            "click .lifecycle-task-signing-link-a": "openSigningCanvas",
            "mousedown .lifecycle-activities-canvas": "canvasMouseDown",
            "mousemove .lifecycle-activities-canvas": "canvasMouseMove",
            "mouseup .lifecycle-activities-canvas": "canvasMouseUp",
            "mouseleave .lifecycle-activities-canvas": "canvasMouseLeave"
        },

        initialize: function() {
            this.moves = new Array();
            this.pressed;
        },

        render: function() {
            this.$el.html(Mustache.render(template, {i18n: i18n}));
            this.bindDomElements();
            this.initSigningPopover();
            this.initCanvas();
            return this;
        },

        bindDomElements: function() {
            this.$signingLink = this.$(".lifecycle-task-signing-link");
            this.$signingPopover = this.$(".lifecycle-task-signing-popover");
        },

        initSigningPopover: function() {
            var self = this;
            this.$signingLink.popover({
                html: true,
                placement: "top",
                title: i18n.SIGN_TASK,
                content: function() {
                    return self.$signingPopover.html();
                }
            });
        },

        initCanvas: function() {

        },

        openSigningCanvas: function() {
            this.$signingLink.popover('show');
            this.canvas = this.$(".lifecycle-activities-canvas").get(0);
            this.context = this.canvas.getContext("2d");
            this.canvas.width = $(".popover-content").width();
            this.canvas.height = $(".popover-content").height();
        },

        canvasMouseDown: function(e) {
            console.log("x" +(e.pageX - this.canvas.getBoundingClientRect().left));
            console.log("y" +(e.pageY -  this.canvas.getBoundingClientRect().top));
            this.pressed = true;
            this.moves.push([e.pageX - this.canvas.getBoundingClientRect().left,
                e.pageY - this.canvas.getBoundingClientRect().top,
                false]);
            this.redraw();
        },

        canvasMouseMove: function(e) {
            if (this.pressed) {
                this.moves.push([e.pageX - this.canvas.getBoundingClientRect().left,
                    e.pageY - this.canvas.getBoundingClientRect().top,
                    true]);
                this.redraw();
            }
        },

        canvasMouseUp: function() {
            this.pressed = false;
        },

        canvasMouseLeave: function() {
            this.pressed = false;
        },

        redraw: function() {
            //this.canvas.width = this.canvas.width; // Limpia el lienzo

            this.context.strokeStyle = "#000000";
            this.context.lineJoin = "round";
            this.context.lineWidth = 3;

            //this.context.fillRect(0,0,150,75);

            for (var i = 0; i < this.moves.length; i++) {
                this.context.beginPath();
                if (this.moves[i][2] && i) {
                    this.context.moveTo(this.moves[i - 1][0], this.moves[i - 1][1]);
                } else {
                    this.context.moveTo(this.moves[i][0], this.moves[i][1]);
                }
                this.context.lineTo(this.moves[i][0], this.moves[i][1]);
                this.context.closePath();
                this.context.stroke();
            }
        }

    });
    return LifecycleTaskSigningView;
});