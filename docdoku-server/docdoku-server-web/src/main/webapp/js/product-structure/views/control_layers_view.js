/*global sceneManager*/
define(
    [
        "text!templates/control_layers.html",
        "i18n!localization/nls/product-structure-strings"
    ],function(template,i18n){

    var ControlLayersView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }

    });

    return ControlLayersView;

});