/*global sceneManager*/
define(
    [
        "text!templates/layers_list.html",
        "i18n!localization/nls/product-structure-strings"
    ],function(template,i18n){

    var LayersListView = Backbone.View.extend({

        template : Mustache.compile(template),
        className:"side_control_group",
        events:{
        },

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }

    });

    return LayersListView;

});