define (["text!templates/part_instance.html","i18n!localization/nls/product-structure-strings"],function(template,i18n) {
    var PartMetadataView = Backbone.View.extend({

        tagName:'div',

        id:"part_instance_container",

        template: Mustache.compile(template),

        events:{
            "click button#focus":"focus"
        },

        className:"side_control_group",

        initialize: function() {

        },

        setMesh:function(mesh){
            this.mesh=mesh;
            return this;
        },

        render: function() {
            this.$el.html(this.template({mesh:this.mesh, i18n:i18n}));
            return this;
        },

        reset:function(){
            this.$el.empty();
        },

        focus:function(){
            App.sceneManager.flyTo(this.mesh);
        }
        
    });

    return PartMetadataView;
});