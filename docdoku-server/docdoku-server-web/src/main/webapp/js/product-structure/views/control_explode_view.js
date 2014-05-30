/*global App*/
define(
    [
        "text!templates/control_explode.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var ControlOptionsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "change input#slider-explode":"explode"
        },

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));

            return this;
        },

        explode:function(e){
            App.sceneManager.explodeScene(e.target.value);
        }

    });

    return ControlOptionsView;

});