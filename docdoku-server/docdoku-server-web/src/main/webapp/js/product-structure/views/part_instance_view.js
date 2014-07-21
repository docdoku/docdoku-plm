/*global App*/
define (["text!templates/part_instance.html","i18n!localization/nls/product-structure-strings"],function(template,i18n) {
    var PartMetadataView = Backbone.View.extend({

        tagName:'div',

        id:"part_instance_container",

        template: Mustache.compile(template),

        events:{
            "click button#fly_to":"fly_to",
            "click button#look_at":"look_at",
            "click #transform_mode_view_btn > button": "transformView",
            "click button#cancel_transformation": "cancelTransformation"
        },

        className:"side_control_group",

        initialize: function() {

        },

        setMesh:function(mesh){
            if (App.sceneManager.transformControlsEnabled()){
                App.sceneManager.deleteTransformControls(this.mesh);
                App.sceneManager.setTransformControls(mesh);

            }
            this.mesh=mesh;
            return this;
        },

        render: function() {
            this.$el.html(this.template({mesh:this.mesh, i18n:i18n}));
            if (App.sceneManager.transformControlsEnabled()){
                var mode = App.sceneManager.getTransformControlsMode();
                this.$("button#"+mode).addClass("active");

            }
            return this;
        },

        reset:function(){
            if (App.sceneManager.transformControlsEnabled()){
                //App.sceneManager.deleteTransformControls();
            } else{
                this.$el.empty();
            }
        },

        fly_to:function(){
            App.sceneManager.flyTo(this.mesh);
        },

        look_at:function(){
            App.sceneManager.lookAt(this.mesh);
        },

        transformView:function(e){
            //$('#transform_mode_view_btn').addClass("active");
            console.log(e.currentTarget.id); // :/
            App.sceneManager.setTransformControls(this.mesh,e.currentTarget.id);
        },

        cancelTransformation:function(){
            //$('#transform_mode_view_btn').removeClass("active");
            App.sceneManager.cancelTransformation(this.mesh);
        }
        
    });

    return PartMetadataView;
});