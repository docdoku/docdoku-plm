define (
    [
        "text!templates/control_transform.html",
        "i18n!localization/nls/product-structure-strings"
    ],function(template,i18n) {

    var PartMetadataView = Backbone.View.extend({

        template: Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click #transform_mode_view_btn > button": "transformView",
            "click button#cancel_transformation": "cancelTransformation"
        },

        initialize: function() {
        },

        setMesh:function(mesh){
            if (App.sceneManager.transformControlsEnabled()){
                App.sceneManager.deleteTransformControls(this.mesh);
                App.sceneManager.setTransformControls(mesh);
            }
            this.$("button").removeAttr("disabled");
            this.mesh = mesh;
            return this;
        },

        reset:function(){

            // TransformControls enabled
            if (App.sceneManager.transformControlsEnabled()){
                var mode = App.sceneManager.getTransformControlsMode();
                this.$("button#"+mode).addClass("active");
            } // A mesh is selected
            else if (this.mesh !== undefined && this.mesh != null) {

            } //  No mesh selected
            else {
                this.$("button").attr("disabled","disabled");
            }
        },

        render: function() {
            this.$el.html(this.template({mesh:this.mesh, i18n:i18n}));
            this.reset();
            return this;
        },

        transformView:function(e){
            var modeSelected = e.currentTarget.id;
            //$('#transform_mode_view_btn').addClass("active");
            //console.log(e.currentTarget.id);
            var transformControlsMode = App.sceneManager.getTransformControlsMode();

            App.sceneManager.setTransformControls(this.mesh, modeSelected);

            /*
            if (modeSelected == transformControlsMode ) {
                // leave Transform mode
                Backbone.Events.trigger("click button#tracking_mode_view_btn");
            } else {
                App.sceneManager.setTransformControls(this.mesh, modeSelected);
            }*/
        },

        cancelTransformation:function(){
            //$('#transform_mode_view_btn').removeClass("active");
            App.sceneManager.cancelTransformation(this.mesh);
        }
        
    });

    return PartMetadataView;
});