/*global sceneManager*/
define(
    [
        "text!templates/control_materials.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var ControlMaterialsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click button#materialsOff": "materialsOff",
            "click button#materialsOn": "materialsOn"
        },

        initialize:function(){
        },

        materialsOff:function(){
            _(sceneManager.instances).each(function(instance){
                if(instance.mesh != null){
                   _(instance.mesh.material.materials).each(function(material){
                       console.log(material)
                       material.wireframe = true;
                   })
                }
            });
        },

        materialsOn:function(){
            _(sceneManager.instances).each(function(instance){
                if(instance.mesh != null){
                    _(instance.mesh.material.materials).each(function(material){
                        console.log(material)
                        material.wireframe = false;
                    })
                }
            });
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }


    });

    return ControlMaterialsView;

});